package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Item;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi3XmlWriter;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Base class for Colectica item repositories.
 * <p>
 * Provides the common REST call logic for creating/updating items via the Colectica API.
 * Subclasses only need to supply the item type UUID and the DDI3 XML transformation.
 */
public abstract class AbstractColecticaItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(AbstractColecticaItemRepository.class);

    protected final RestClient restClient;
    protected final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    protected final ColecticaAuthenticator authenticator;
    protected final Ddi3XmlWriter ddi3XmlWriter;

    protected AbstractColecticaItemRepository(
            RestClient restClient,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi3XmlWriter ddi3XmlWriter
    ) {
        this.restClient = restClient;
        this.instanceConfiguration = instanceConfiguration;
        this.authenticator = authenticator;
        this.ddi3XmlWriter = ddi3XmlWriter;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates or updates an item in Colectica via POST /api/v1/item with RegisterOrReplace.
     *
     * @param itemTypeUuid the Colectica item type UUID
     * @param item         the DDI4 item providing metadata (agency, id, version, etc.)
     * @param ddi3Xml      the DDI3 XML fragment for this item
     */
    protected void createOrUpdateItem(String itemTypeUuid, Ddi4Item item, String ddi3Xml) {
        logger.info("DDI3 XML for item id={}: {}", item.id(), ddi3Xml);
        authenticator.executeWithAuth(token -> {
            // Use itemFormat as-is (uppercase) to match the Colectica SDK official examples
            String itemFormat = instanceConfiguration.itemFormat();

            ColecticaItemResponse colecticaItem = new ColecticaItemResponse(
                    itemTypeUuid,
                    item.agency(),
                    Integer.parseInt(item.version()),
                    item.id(),
                    ddi3Xml,
                    item.versionDate(),
                    instanceConfiguration.versionResponsibility(),
                    false,
                    false,
                    false,
                    itemFormat
            );

            ColecticaCreateItemRequest createRequest = new ColecticaCreateItemRequest(List.of(colecticaItem));

            // Log full JSON payload for diagnostic purposes
            try {
                String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createRequest);
                logger.info("Full JSON payload for item id={}: {}", item.id(), jsonPayload);
            } catch (JsonProcessingException e) {
                logger.warn("Could not serialize request for logging", e);
            }

            String url = instanceConfiguration.baseApiUrl() + "item";

            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(createRequest)
                    .retrieve()
                    .body(String.class);

            logger.info("Successfully created/updated item: type={}, id={}, response={}", itemTypeUuid, item.id(), response);
            return null;
        });
    }

    /**
     * Generates a deterministic UUID (v3, name-based MD5) from a URI.
     * The same URI always produces the same UUID, ensuring that
     * {@code RegisterOrReplace} updates existing items instead of creating duplicates.
     */
    public static String generateDeterministicUuid(String uri) {
        return UUID.nameUUIDFromBytes(uri.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
