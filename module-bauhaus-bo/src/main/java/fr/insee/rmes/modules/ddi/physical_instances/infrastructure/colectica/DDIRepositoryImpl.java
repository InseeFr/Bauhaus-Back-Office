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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Repository
public class DDIRepositoryImpl implements DDIRepository {
    static final Logger logger = LoggerFactory.getLogger(DDIRepositoryImpl.class);

    private final RestTemplate restTemplate;
    private final ColecticaConfiguration colecticaConfiguration;
    private final ObjectMapper objectMapper;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private Ddi4Response cachedDdi4Response;
    private String cachedAuthToken;

    public DDIRepositoryImpl(
            RestTemplate restTemplate,
            ColecticaConfiguration colecticaConfiguration,
            ObjectMapper objectMapper,
            fr.insee.rmes.domain.port.clientside.DDI3toDDI4ConverterService ddi3ToDdi4Converter
            ) {
        this.restTemplate = restTemplate;
        this.colecticaConfiguration = colecticaConfiguration;
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

        String tokenUrl = colecticaConfiguration.baseServerUrl() + "/token/createtoken";

        AuthenticationRequest authRequest = new AuthenticationRequest(
            colecticaConfiguration.username(),
            colecticaConfiguration.password()
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
        logger.info("Getting physical instances from Colectica API via HTTP");

        return executeWithAuth(token -> {
            // Set up the request with authorization header
            String url = colecticaConfiguration.baseApiUrl() + "_query";

            // Create request body with itemTypes from configuration
            QueryRequest requestBody = new QueryRequest(colecticaConfiguration.itemTypes());

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
                        return new PartialPhysicalInstance(id, label, date);
                    })
                    .toList();
        });
    }
    
    private String extractLabelFromItem(ColecticaItem item) {
        // Extract label from ItemName or Label (both can have language variants)
        String label = extractLabelFromLanguageMap(item.itemName());
        if (label == null || label.trim().isEmpty()) {
            label = extractLabelFromLanguageMap(item.label());
        }
        if (label == null || label.trim().isEmpty()) {
            label = item.identifier(); // Fallback to ID if no label found
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

    @Override
    public Ddi4Response getPhysicalInstance(String id) {
        if (cachedDdi4Response != null && cachedDdi4Response.physicalInstance() != null
                && !cachedDdi4Response.physicalInstance().isEmpty()
                && cachedDdi4Response.physicalInstance().get(0).id().equals(id)) {
            logger.info("Returning cached DDI4 Physical Instance for id: {}", id);
            return cachedDdi4Response;
        }

        logger.info("Fetching DDI4 Physical Instance from Colectica API for id: {}", id);

        return executeWithAuth(token -> {
            try {
                // First, query all Physical Instances to find the one with matching ID
                String queryUrl = colecticaConfiguration.baseApiUrl() + "_query";

                // Create query request with item types
                QueryRequest requestBody = new QueryRequest(colecticaConfiguration.itemTypes());

                // Create headers with Bearer token
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                // Create HTTP entity with headers and body
                HttpEntity<QueryRequest> requestEntity = new HttpEntity<>(requestBody, headers);

                // Make the POST request to query Physical Instances
                ColecticaResponse colecticaResponse = restTemplate.postForObject(
                        queryUrl,
                        requestEntity,
                        ColecticaResponse.class
                );

                if (colecticaResponse == null || colecticaResponse.results() == null || colecticaResponse.results().isEmpty()) {
                    logger.error("No Physical Instances found in Colectica");
                    return null;
                }

                // Find the item with matching identifier
                ColecticaItem matchingItem = colecticaResponse.results().stream()
                        .filter(item -> id.equals(item.identifier()))
                        .findFirst()
                        .orElse(null);

                if (matchingItem == null) {
                    logger.error("No Physical Instance found with id: {}", id);
                    return null;
                }

                logger.info("Found Physical Instance with id: {}, agencyId: {}, version: {}",
                        matchingItem.identifier(), matchingItem.agencyId(), matchingItem.version());

                // Now fetch the full DDI4 item details using the item endpoint
                // Format: /api/v1/item/{agencyId}/{identifier}/{version}
                String itemUrl = colecticaConfiguration.baseApiUrl() + "item/"
                        + matchingItem.agencyId() + "/"
                        + matchingItem.identifier() + "/"
                        + matchingItem.version();

                logger.info("Fetching full item details from: {}", itemUrl);

                HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);

                // The response from Colectica contains XML in the "Item" field
                ColecticaItemResponse itemResponse = restTemplate.exchange(
                        itemUrl,
                        HttpMethod.GET,
                        getRequestEntity,
                        ColecticaItemResponse.class
                ).getBody();

                if (itemResponse == null || itemResponse.item() == null || itemResponse.item().isEmpty()) {
                    logger.error("Received empty response from Colectica API for item URL: {}", itemUrl);
                    return null;
                }

                logger.debug("Received DDI3 XML from Colectica for Physical Instance");

                // Create a Ddi3Response from the Colectica response
                Ddi3Response.Ddi3Item ddi3Item = new Ddi3Response.Ddi3Item(
                    itemResponse.itemType(),
                    itemResponse.agencyId(),
                    String.valueOf(itemResponse.version()),
                    itemResponse.identifier(),
                    itemResponse.item(),  // XML content
                    itemResponse.versionDate(),
                    itemResponse.versionResponsibility(),
                    itemResponse.isPublished(),
                    itemResponse.isDeprecated(),
                    itemResponse.isProvisional(),
                    itemResponse.itemFormat()
                );

                Ddi3Response ddi3Response = new Ddi3Response(
                    null,  // options
                    List.of(ddi3Item)
                );

                // Use the converter service to convert DDI3 to DDI4
                logger.info("Converting DDI3 to DDI4 using converter service");
                cachedDdi4Response = ddi3ToDdi4Converter.convertDdi3ToDdi4(ddi3Response, "ddi:4.0");

                logger.info("Successfully converted Physical Instance to DDI4 format");
                return cachedDdi4Response;

            } catch (Exception e) {
                logger.error("Error processing Colectica API response for id: {}", id, e);
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
            
            // Update Physical Instance label if present
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