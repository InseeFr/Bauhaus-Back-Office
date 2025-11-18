package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DDIRepositoryImpl implements DDIRepository {
    static final Logger logger = LoggerFactory.getLogger(DDIRepositoryImpl.class);

    private final RestTemplate restTemplate;
    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ObjectMapper objectMapper;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private Ddi4Response cachedDdi4Response;
    private String cachedAuthToken;

    public DDIRepositoryImpl(
            RestTemplate restTemplate,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ObjectMapper objectMapper,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter
            ) {
        this.restTemplate = restTemplate;
        this.instanceConfiguration = instanceConfiguration;
        this.objectMapper = objectMapper;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
    }

    /**
     * Retrieves authentication token from cache or fetches a new one
     * @param forceRefresh if true, forces a new token request even if cache is available
     * @return the authentication token
     */
    private String getAuthToken(boolean forceRefresh) {
        if (!forceRefresh && cachedAuthToken != null) {
            logger.debug("Using cached authentication token");
            return cachedAuthToken;
        }

        logger.info("Authenticating to Colectica API");

        String tokenUrl = instanceConfiguration.baseServerUrl() + "/token/createtoken";

        AuthenticationRequest authRequest = new AuthenticationRequest(
            instanceConfiguration.username(),
            instanceConfiguration.password()
        );

        // Create headers with Content-Type for authentication request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Create HTTP entity with headers and body
        HttpEntity<AuthenticationRequest> requestEntity = new HttpEntity<>(authRequest, headers);

        AuthenticationResponse authResponse = restTemplate.postForObject(
            tokenUrl,
            requestEntity,
            AuthenticationResponse.class
        );

        if (authResponse == null || authResponse.accessToken() == null) {
            logger.error("Failed to retrieve authentication token");
            throw new RuntimeException("Authentication failed: unable to retrieve access token");
        }

        cachedAuthToken = authResponse.accessToken();
        return cachedAuthToken;
    }

    /**
     * Generic method to execute API calls with authentication and automatic retry on auth failure
     * @param apiCall Function that takes a token and performs the API call
     * @param <T> Return type of the API call
     * @return Result of the API call
     */
    private <T> T executeWithAuth(Function<String, T> apiCall) {
        try {
            // Try with cached or new token
            String token = getAuthToken(false);
            return apiCall.apply(token);
        } catch (HttpClientErrorException e) {
            // If authentication failed (401 Unauthorized or 403 Forbidden), refresh token and retry once
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("Authentication failed with cached token, refreshing and retrying...");
                cachedAuthToken = null; // Invalidate cache
                String newToken = getAuthToken(true);
                return apiCall.apply(newToken);
            }
            // Re-throw if it's not an authentication error
            throw e;
        }
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Getting physical instances from Colectica API via HTTP (primary instance)");

        return executeWithAuth(token -> {
            // Set up the request with authorization header
            String url = instanceConfiguration.baseApiUrl() + "_query";

            // Create request body with itemTypes from configuration
            QueryRequest requestBody = new QueryRequest(instanceConfiguration.itemTypes());

            // Create headers with Bearer token and Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            // Create HTTP entity with headers and body
            HttpEntity<QueryRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the request with authentication
            ColecticaResponse response = restTemplate.postForObject(url, requestEntity, ColecticaResponse.class);

            return response.results().stream()
                    .map(item -> {
                        String id = item.identifier();
                        String label = extractLabelFromItem(item);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date date = null;
                        try {
                            date = formatter.parse(item.versionDate());
                        } catch (ParseException | NullPointerException _) {
                            logger.debug("Impossible to parse {}", item.versionDate());
                        }
                        String agency = item.agencyId();
                        return new PartialPhysicalInstance(id, label, date, agency);
                    })
                    .toList();
        });
    }
    
    private String extractLabelFromItem(ColecticaItem item) {
        // Extract value from ItemName or Label (both can have language variants)
        String label = extractLabelFromLanguageMap(item.itemName());
        if (label == null || label.trim().isEmpty()) {
            label = extractLabelFromLanguageMap(item.label());
        }
        if (label == null || label.trim().isEmpty()) {
            label = item.identifier(); // Fallback to ID if no value found
        }
        return label;
    }
    
    private String extractLabelFromLanguageMap(Map<String, String> languageMap) {
        if (languageMap == null) {
            return null;
        }

        // Try French first, then English, then first available
        String label = languageMap.get("fr-FR");
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.get("en");
        }
        if (label == null || label.trim().isEmpty()) {
            label = languageMap.values().stream().findFirst().orElse(null);
        }
        return label;
    }

    /**
     * Parse FragmentInstance XML and extract each Fragment as a separate Ddi3Item
     * The FragmentInstance contains multiple Fragment elements (PhysicalInstance, DataRelationship, etc.)
     */
    private List<Ddi3Response.Ddi3Item> parseFragmentInstanceToItems(String fragmentInstanceXml, String agencyId, String id) {
        try {
            // Parse the XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(fragmentInstanceXml)));

            List<Ddi3Response.Ddi3Item> items = new ArrayList<>();

            // Get all Fragment elements
            NodeList fragmentNodes = doc.getElementsByTagNameNS("ddi:instance:3_3", "Fragment");

            logger.debug("Found {} Fragment elements in FragmentInstance", fragmentNodes.getLength());

            for (int i = 0; i < fragmentNodes.getLength(); i++) {
                Element fragmentElement = (Element) fragmentNodes.item(i);

                // Determine the item type based on the content of the Fragment
                String itemType = determineItemType(fragmentElement);

                // Skip fragments with unsupported types (e.g., CodeList, Category)
                if (itemType == null) {
                    logger.debug("Skipping Fragment {} - unsupported type", i);
                    continue;
                }

                logger.debug("Processing Fragment {} with itemType: {}", i, itemType);

                // Convert this Fragment element back to XML string
                String fragmentXml = elementToString(fragmentElement);

                // Create a Ddi3Item for this Fragment
                Ddi3Response.Ddi3Item ddi3Item = new Ddi3Response.Ddi3Item(
                    itemType,
                    agencyId,
                    "1", // version
                    id,
                    fragmentXml,
                    null, // versionDate
                    null, // versionResponsibility
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    "dc337820-af3a-4c0b-82f9-cf02535cde83" // itemFormat
                );

                items.add(ddi3Item);
            }

            return items;
        } catch (Exception e) {
            logger.error("Error parsing FragmentInstance XML", e);
            throw new RuntimeException("Failed to parse FragmentInstance XML", e);
        }
    }

    /**
     * Determine the item type based on the content of a Fragment element
     * Returns null if the Fragment type is not supported
     */
    private String determineItemType(Element fragmentElement) {
        // Check for PhysicalInstance
        if (fragmentElement.getElementsByTagNameNS("ddi:physicalinstance:3_3", "PhysicalInstance").getLength() > 0) {
            return "a51e85bb-6259-4488-8df2-f08cb43485f8"; // PhysicalInstance type UUID
        }
        // Check for DataRelationship
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "DataRelationship").getLength() > 0) {
            return "f39ff278-8500-45fe-a850-3906da2d242b"; // DataRelationship type UUID
        }
        // Check for Variable
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "Variable").getLength() > 0) {
            return "683889c6-f74b-4d5e-92ed-908c0a42bb2d"; // Variable type UUID
        }
        // Return null for unsupported types (CodeList, Category, etc.)
        // These will be skipped during processing
        return null;
    }

    /**
     * Convert a DOM Element to XML String
     */
    private String elementToString(Element element) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }

    @Override
    public Ddi4Response getPhysicalInstance(String agencyId, String id) {
        if (cachedDdi4Response != null && cachedDdi4Response.physicalInstance() != null
                && !cachedDdi4Response.physicalInstance().isEmpty()
                && cachedDdi4Response.physicalInstance().get(0).id().equals(id)
                && cachedDdi4Response.physicalInstance().get(0).agency().equals(agencyId)) {
            logger.info("Returning cached DDI4 Physical Instance for agencyId: {}, id: {}", agencyId, id);
            return cachedDdi4Response;
        }

        logger.info("Fetching DDI4 Physical Instance from Colectica API for agencyId: {}, id: {}", agencyId, id);

        return executeWithAuth(token -> {
            try {
                // Fetch the full DDI set (PhysicalInstance + DataRelationship) using the ddiset endpoint
                // Format: /api/v1/ddiset/{agencyId}/{identifier}
                String ddisetUrl = instanceConfiguration.baseApiUrl() + "ddiset/"
                        + agencyId + "/"
                        + id;

                logger.info("Fetching full DDI set from: {}", ddisetUrl);

                // Create headers with Bearer token
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);

                // The response from Colectica ddiset endpoint contains XML with PhysicalInstance and DataRelationship
                String ddisetXml = restTemplate.exchange(
                        ddisetUrl,
                        HttpMethod.GET,
                        getRequestEntity,
                        String.class
                ).getBody();

                if (ddisetXml == null || ddisetXml.isEmpty()) {
                    logger.error("Received empty response from Colectica API for ddiset URL: {}", ddisetUrl);
                    return null;
                }

                logger.info("Received response from ddiset endpoint. Length: {}, First 200 chars: {}",
                    ddisetXml.length(),
                    ddisetXml.substring(0, Math.min(200, ddisetXml.length())));

                // Clean the XML - remove all leading invisible/control characters until we hit '<'
                // This handles BOM, zero-width spaces, and other invisible characters
                int startIndex = 0;
                while (startIndex < ddisetXml.length() && ddisetXml.charAt(startIndex) != '<') {
                    char c = ddisetXml.charAt(startIndex);
                    if (c == '\uFEFF' || Character.isWhitespace(c) || Character.isISOControl(c) || !Character.isDefined(c)) {
                        startIndex++;
                    } else {
                        // Found a non-whitespace, non-control character that's not '<'
                        logger.warn("Unexpected character at position {}: {} (code: {})", startIndex, c, (int)c);
                        startIndex++;
                    }
                }

                if (startIndex > 0) {
                    logger.info("Removed {} leading characters from XML (BOM, whitespace, or control characters)", startIndex);
                    ddisetXml = ddisetXml.substring(startIndex);
                }

                // Final trim for any trailing whitespace
                ddisetXml = ddisetXml.trim();

                logger.debug("Cleaned XML starts with: {}", ddisetXml.substring(0, Math.min(100, ddisetXml.length())));

                logger.debug("Received DDI set XML from Colectica for Physical Instance and DataRelationship");

                // Parse the FragmentInstance XML and extract each Fragment
                // The ddiset endpoint returns a FragmentInstance containing multiple Fragment elements
                List<Ddi3Response.Ddi3Item> ddi3Items = parseFragmentInstanceToItems(ddisetXml, agencyId, id);

                Ddi3Response ddi3Response = new Ddi3Response(
                    null,  // options
                    ddi3Items
                );

                // Use the converter service to convert DDI3 to DDI4
                logger.info("Converting DDI3 to DDI4 using converter service");
                cachedDdi4Response = ddi3ToDdi4Converter.convertDdi3ToDdi4(ddi3Response, "ddi:4.0");

                logger.info("Successfully converted Physical Instance to DDI4 format");
                return cachedDdi4Response;

            } catch (Exception e) {
                logger.error("Error processing Colectica API response for agencyId: {}, id: {}", agencyId, id, e);
                throw new RuntimeException("Failed to process DDI response", e);
            }
        });
    }

    @Override
    public void updatePhysicalInstance(String id, UpdatePhysicalInstanceRequest request) {
        if (cachedDdi4Response != null) {
            logger.info("Updating cached DDI4 Physical Instance data");
            
            var physicalInstances = cachedDdi4Response.physicalInstance();
            var dataRelationships = cachedDdi4Response.dataRelationship();
            
            // Update Physical Instance value if present
            if (physicalInstances != null && !physicalInstances.isEmpty() && request.physicalInstanceLabel() != null) {
                var currentPI = physicalInstances.get(0);
                var updatedTitle = new Title(
                    new StringValue(
                        currentPI.citation().title().string().xmlLang(),
                        request.physicalInstanceLabel()
                    )
                );
                var updatedCitation = new Citation(updatedTitle);
                var updatedPI = new Ddi4PhysicalInstance(
                    currentPI.isUniversallyUnique(),
                    currentPI.versionDate(),
                    currentPI.urn(),
                    currentPI.agency(),
                    currentPI.id(),
                    currentPI.version(),
                    updatedCitation,
                    currentPI.dataRelationshipReference()
                );
                physicalInstances = List.of(updatedPI);
            }
            
            // Update DataRelationship name if present
            if (dataRelationships != null && !dataRelationships.isEmpty() && request.dataRelationshipName() != null) {
                var currentDR = dataRelationships.get(0);
                var updatedName = new DataRelationshipName(
                    new StringValue(
                        currentDR.dataRelationshipName().string().xmlLang(),
                        request.dataRelationshipName()
                    )
                );
                var updatedDR = new Ddi4DataRelationship(
                    currentDR.isUniversallyUnique(),
                    currentDR.versionDate(),
                    currentDR.urn(),
                    currentDR.agency(),
                    currentDR.id(),
                    currentDR.version(),
                    updatedName,
                    currentDR.logicalRecord()
                );
                dataRelationships = List.of(updatedDR);
            }
            
            // Update cached response
            cachedDdi4Response = new Ddi4Response(
                cachedDdi4Response.schema(),
                cachedDdi4Response.topLevelReference(),
                physicalInstances,
                dataRelationships,
                cachedDdi4Response.variable(),
                cachedDdi4Response.codeList(),
                cachedDdi4Response.category()
            );
        }
    }
}