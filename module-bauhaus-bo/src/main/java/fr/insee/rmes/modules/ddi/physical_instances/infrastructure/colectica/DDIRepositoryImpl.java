package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final ColecticaConfiguration colecticaConfiguration;
    private final ObjectMapper objectMapper;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private String cachedAuthToken;
    private Set<String> denyListCache;

    public DDIRepositoryImpl(
            RestTemplate restTemplate,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ObjectMapper objectMapper,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            ColecticaConfiguration colecticaConfiguration
            ) {
        this.restTemplate = restTemplate;
        this.instanceConfiguration = instanceConfiguration;
        this.objectMapper = objectMapper;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.colecticaConfiguration = colecticaConfiguration;
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
            headers.setContentType(MediaType.APPLICATION_JSON);
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
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
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

    @Override
    public List<PartialCodesList> getCodesLists() {
        logger.info("Getting codes lists from Colectica API via HTTP");

        return executeWithAuth(token -> {
            // Set up the request with authorization header
            String url = instanceConfiguration.baseApiUrl() + "_query";

            // Create request body with CodeList itemType
            QueryRequest requestBody = new QueryRequest(List.of("8b108ef8-b642-4484-9c49-f88e4bf7cf1d"));

            // Create headers with Bearer token and Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            // Create HTTP entity with headers and body
            HttpEntity<QueryRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make the request with authentication
            ColecticaResponse response = restTemplate.postForObject(url, requestEntity, ColecticaResponse.class);

            int totalCount = response.results().size();
            logger.debug("Received {} code lists from Colectica API", totalCount);

            List<PartialCodesList> result = response.results().stream()
                    .filter(item -> !isCodeListInDenyList(item.agencyId(), item.identifier()))
                    .map(item -> {
                        String id = item.identifier();
                        String label = extractLabelFromItem(item);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = null;
                        try {
                            date = formatter.parse(item.versionDate());
                        } catch (ParseException | NullPointerException _) {
                            logger.debug("Impossible to parse {}", item.versionDate());
                        }
                        String agency = item.agencyId();
                        return new PartialCodesList(id, label, date, agency);
                    })
                    .toList();

            int filteredCount = totalCount - result.size();
            if (filteredCount > 0) {
                logger.info("Filtered {} code list(s) from {} total using deny list (returned {} code lists)",
                        filteredCount, totalCount, result.size());
            } else {
                logger.debug("No code lists filtered, returning all {} code lists", result.size());
            }

            return result;
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
     * Check if a code list is in the deny list.
     *
     * <p>This method uses a cached HashSet for O(1) lookup performance.
     * The cache is lazily initialized on first access.
     *
     * <p>Code lists matching entries in the deny list (based on agencyId and id)
     * will be excluded from the results. This is useful for filtering out
     * deprecated, test, or otherwise unwanted code lists from the Colectica repository.
     *
     * <p>Configuration example in properties file:
     * <pre>
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].agency-id = fr.insee
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].id = 2a22ba00-a977-4a61-a582-99025c6b0582
     * </pre>
     *
     * @param agencyId The agency ID of the code list to check
     * @param id The ID of the code list to check
     * @return true if the code list should be filtered out, false otherwise
     * @see ColecticaConfiguration.CodeListDenyEntry
     */
    private boolean isCodeListInDenyList(String agencyId, String id) {
        if (colecticaConfiguration == null || colecticaConfiguration.codeListDenyList() == null) {
            return false;
        }

        // Lazy initialization of cache for O(1) lookups
        if (denyListCache == null) {
            denyListCache = colecticaConfiguration.codeListDenyList().stream()
                    .map(entry -> createDenyListKey(entry.agencyId(), entry.id()))
                    .collect(Collectors.toSet());
            logger.info("Initialized code list deny list cache with {} entries", denyListCache.size());
        }

        String key = createDenyListKey(agencyId, id);
        boolean isDenied = denyListCache.contains(key);

        if (isDenied) {
            logger.debug("Filtering out code list: agencyId={}, id={}", agencyId, id);
        }

        return isDenied;
    }

    /**
     * Creates a unique key for deny list lookups by combining agencyId and id.
     *
     * @param agencyId The agency ID
     * @param id The code list ID
     * @return A unique key string in format "agencyId:id"
     */
    private String createDenyListKey(String agencyId, String id) {
        return agencyId + ":" + id;
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
        // Check for CodeList
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "CodeList").getLength() > 0) {
            return "8b108ef8-b642-4484-9c49-f88e4bf7cf1d"; // CodeList type UUID
        }
        // Check for Category
        if (fragmentElement.getElementsByTagNameNS("ddi:logicalproduct:3_3", "Category").getLength() > 0) {
            return "7e47c269-bcab-40f7-a778-af7bbc4e3d00"; // Category type UUID
        }
        // Return null for unsupported types
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
                var response = ddi3ToDdi4Converter.convertDdi3ToDdi4(ddi3Response, "ddi:4.0");

                logger.info("Successfully converted Physical Instance to DDI4 format");
                return response;

            } catch (Exception e) {
                logger.error("Error processing Colectica API response for agencyId: {}, id: {}", agencyId, id, e);
                throw new RuntimeException("Failed to process DDI response", e);
            }
        });
    }

    @Override
    public void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        logger.info("Updating physical instance {}/{} in Colectica", agencyId, id);

        executeWithAuth(token -> {
            // First, fetch the current instance to get all necessary information
            Ddi4Response currentInstance = getPhysicalInstance(agencyId, id);

            if (currentInstance == null || currentInstance.physicalInstance() == null || currentInstance.physicalInstance().isEmpty()) {
                throw new RuntimeException("Physical instance not found: " + agencyId + "/" + id);
            }

            var currentPI = currentInstance.physicalInstance().get(0);
            var currentDR = currentInstance.dataRelationship() != null && !currentInstance.dataRelationship().isEmpty()
                    ? currentInstance.dataRelationship().get(0)
                    : null;

            // Get current timestamp in ISO format
            String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // Determine new version (increment by 1)
            int currentVersion = Integer.parseInt(currentPI.version());
            int newVersion = currentVersion + 1;

            // Build updated PhysicalInstance label
            String physicalInstanceLabel = request.physicalInstanceLabel() != null
                    ? request.physicalInstanceLabel()
                    : currentPI.citation().title().string().text();

            // Build updated DataRelationship name
            String dataRelationshipName = request.dataRelationshipName() != null && currentDR != null
                    ? request.dataRelationshipName()
                    : (currentDR != null ? currentDR.dataRelationshipName().string().text() : "DataRelationship");

            // Get DataRelationship ID from the reference
            String dataRelationshipId = currentPI.dataRelationshipReference() != null
                    ? currentPI.dataRelationshipReference().id()
                    : UUID.randomUUID().toString();

            // Get LogicalRecord information
            String logicalRecordId = currentDR != null && currentDR.logicalRecord() != null
                    ? currentDR.logicalRecord().id()
                    : UUID.randomUUID().toString();

            String logicalRecordLabel = currentDR != null && currentDR.logicalRecord() != null
                    ? currentDR.logicalRecord().logicalRecordName().string().text()
                    : physicalInstanceLabel;

            // Build DDI3 XML fragments with updated data
            String physicalInstanceXml = buildPhysicalInstanceXml(
                    agencyId,
                    id,
                    newVersion,
                    physicalInstanceLabel,
                    dataRelationshipId,
                    versionDate
            );

            String dataRelationshipXml = buildDataRelationshipXml(
                    agencyId,
                    dataRelationshipId,
                    newVersion,
                    dataRelationshipName,
                    logicalRecordId,
                    logicalRecordLabel,
                    versionDate
            );

            // Create Colectica items
            ColecticaItemResponse physicalInstanceItem = new ColecticaItemResponse(
                    "a51e85bb-6259-4488-8df2-f08cb43485f8", // PhysicalInstance type UUID
                    agencyId,
                    newVersion,
                    id,
                    physicalInstanceXml,
                    versionDate,
                    "bauhaus-api",
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    "dc337820-af3a-4c0b-82f9-cf02535cde83" // DDI format UUID
            );

            ColecticaItemResponse dataRelationshipItem = new ColecticaItemResponse(
                    "f39ff278-8500-45fe-a850-3906da2d242b", // DataRelationship type UUID
                    agencyId,
                    newVersion,
                    dataRelationshipId,
                    dataRelationshipXml,
                    versionDate,
                    "bauhaus-api",
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    "dc337820-af3a-4c0b-82f9-cf02535cde83" // DDI format UUID
            );

            // Create request with both items
            ColecticaCreateItemRequest updateRequest = new ColecticaCreateItemRequest(
                    List.of(physicalInstanceItem, dataRelationshipItem)
            );

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<ColecticaCreateItemRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

            logger.info("Sending update request to Colectica: {}", url);

            // POST to Colectica (same endpoint for create and update)
            restTemplate.postForObject(url, requestEntity, String.class);

            logger.info("Successfully updated physical instance with id: {}", id);

            // Clear cache to force refresh on next read

            return null;
        });
    }

    @Override
    public void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        logger.info("Updating full physical instance {}/{} with all DDI objects in Colectica", agencyId, id);

        executeWithAuth(token -> {

            // Convert DDI4 to DDI3
            Ddi3Response ddi3Response = ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Response);

            if (ddi3Response == null || ddi3Response.items() == null || ddi3Response.items().isEmpty()) {
                throw new RuntimeException("No items to save in DDI4 response");
            }

            // Convert each Ddi3Item to ColecticaItemResponse
            List<ColecticaItemResponse> colecticaItems = ddi3Response.items().stream()
                .map(ddi3Item -> new ColecticaItemResponse(
                    ddi3Item.itemType(),
                    ddi3Item.agencyId(),
                    Integer.parseInt(ddi3Item.version()),
                    ddi3Item.identifier(),
                    ddi3Item.item(),
                    ddi3Item.versionDate(),
                    ddi3Item.versionResponsibility(),
                    ddi3Item.isPublished(),
                    ddi3Item.isDeprecated(),
                    ddi3Item.isProvisional(),
                    ddi3Item.itemFormat()
                ))
                .toList();

            // Create request with all items
            ColecticaCreateItemRequest updateRequest = new ColecticaCreateItemRequest(colecticaItems);

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<ColecticaCreateItemRequest> requestEntity = new HttpEntity<>(updateRequest, headers);

            logger.info("Sending full update request to Colectica with {} items: {}", colecticaItems.size(), url);

            // POST to Colectica
            restTemplate.postForObject(url, requestEntity, String.class);

            logger.info("Successfully updated full physical instance with id: {} ({} items saved)", id, colecticaItems.size());


            return null;
        });
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        logger.info("Creating new physical instance with label: {}", request.physicalInstanceLabel());

        return executeWithAuth(token -> {
            // Generate UUIDs for physical instance and data relationship
            String physicalInstanceId = UUID.randomUUID().toString();
            String dataRelationshipId = UUID.randomUUID().toString();
            String logicalRecordId = UUID.randomUUID().toString();
            String agencyId = "fr.insee";
            int version = 1;

            // Get current timestamp in ISO format
            String versionDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // Build DDI3 XML fragments
            String physicalInstanceXml = buildPhysicalInstanceXml(
                    agencyId,
                    physicalInstanceId,
                    version,
                    request.physicalInstanceLabel(),
                    dataRelationshipId,
                    versionDate
            );

            String dataRelationshipXml = buildDataRelationshipXml(
                    agencyId,
                    dataRelationshipId,
                    version,
                    request.dataRelationshipName(),
                    logicalRecordId,
                    request.physicalInstanceLabel(),
                    versionDate
            );

            // Create Colectica items
            ColecticaItemResponse physicalInstanceItem = new ColecticaItemResponse(
                    "a51e85bb-6259-4488-8df2-f08cb43485f8", // PhysicalInstance type UUID
                    agencyId,
                    version,
                    physicalInstanceId,
                    physicalInstanceXml,
                    versionDate,
                    "bauhaus-api",
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    "dc337820-af3a-4c0b-82f9-cf02535cde83" // DDI format UUID
            );

            ColecticaItemResponse dataRelationshipItem = new ColecticaItemResponse(
                    "f39ff278-8500-45fe-a850-3906da2d242b", // DataRelationship type UUID
                    agencyId,
                    version,
                    dataRelationshipId,
                    dataRelationshipXml,
                    versionDate,
                    "bauhaus-api",
                    false, // isPublished
                    false, // isDeprecated
                    false, // isProvisional
                    "dc337820-af3a-4c0b-82f9-cf02535cde83" // DDI format UUID
            );

            // Create request with both items
            ColecticaCreateItemRequest createRequest = new ColecticaCreateItemRequest(
                    List.of(physicalInstanceItem, dataRelationshipItem)
            );

            // Send to Colectica
            String url = instanceConfiguration.baseApiUrl() + "item";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<ColecticaCreateItemRequest> requestEntity = new HttpEntity<>(createRequest, headers);

            logger.info("Sending creation request to Colectica: {}", url);

            // POST to Colectica
            restTemplate.postForObject(url, requestEntity, String.class);

            logger.info("Successfully created physical instance with id: {}", physicalInstanceId);

            // Return the created instance
            return getPhysicalInstance(agencyId, physicalInstanceId);
        });
    }

    /**
     * Build DDI3 XML fragment for PhysicalInstance
     */
    private String buildPhysicalInstanceXml(String agencyId, String id, int version,
                                           String label, String dataRelationshipId, String versionDate) {
        return String.format("""
                <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                  <PhysicalInstance isUniversallyUnique="true" versionDate="%s" xmlns="ddi:physicalinstance:3_3">
                    <r:URN>urn:ddi:%s:%s:%d</r:URN>
                    <r:Agency>%s</r:Agency>
                    <r:ID>%s</r:ID>
                    <r:Version>%d</r:Version>
                    <r:Citation>
                      <r:Title>
                        <r:String xml:lang="fr-FR">%s</r:String>
                      </r:Title>
                    </r:Citation>
                    <r:DataRelationshipReference>
                      <r:Agency>%s</r:Agency>
                      <r:ID>%s</r:ID>
                      <r:Version>%d</r:Version>
                      <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                    </r:DataRelationshipReference>
                  </PhysicalInstance>
                </Fragment>""",
                versionDate, agencyId, id, version,
                agencyId, id, version, label,
                agencyId, dataRelationshipId, version
        );
    }

    /**
     * Build DDI3 XML fragment for DataRelationship
     */
    private String buildDataRelationshipXml(String agencyId, String dataRelationshipId, int version,
                                           String dataRelationshipName, String logicalRecordId,
                                           String logicalRecordLabel, String versionDate) {
        return String.format("""
                <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                  <DataRelationship isUniversallyUnique="true" versionDate="%s" xmlns="ddi:logicalproduct:3_3">
                    <r:URN>urn:ddi:%s:%s:%d</r:URN>
                    <r:Agency>%s</r:Agency>
                    <r:ID>%s</r:ID>
                    <r:Version>%d</r:Version>
                    <DataRelationshipName>
                      <r:String xml:lang="en-US">%s</r:String>
                    </DataRelationshipName>
                    <LogicalRecord isUniversallyUnique="true">
                      <r:URN>urn:ddi:%s:%s:%d</r:URN>
                      <r:Agency>%s</r:Agency>
                      <r:ID>%s</r:ID>
                      <r:Version>%d</r:Version>
                      <LogicalRecordName>
                        <r:String xml:lang="fr">%s</r:String>
                      </LogicalRecordName>
                    </LogicalRecord>
                  </DataRelationship>
                </Fragment>""",
                versionDate, agencyId, dataRelationshipId, version,
                agencyId, dataRelationshipId, version, dataRelationshipName,
                agencyId, logicalRecordId, version,
                agencyId, logicalRecordId, version, logicalRecordLabel
        );
    }
}