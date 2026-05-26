package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Item;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi4ToLifecycle33;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaItemResponse;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Base class for Colectica item repositories.
 * Builds the DDI3 XML payload internally from the DDI4 domain object via
 * {@link Ddi4ToLifecycle33} + {@code fragment.xmlText(...)}.
 */
public abstract class AbstractColecticaItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(AbstractColecticaItemRepository.class);

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_GROUP_NS = "ddi:group:3_3";
    private static final String DDI_STUDY_UNIT_NS = "ddi:studyunit:3_3";

    protected final RestClient restClient;
    protected final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    protected final ColecticaAuthenticator authenticator;
    protected final Ddi4ToLifecycle33 ddi4ToLifecycle33;

    protected AbstractColecticaItemRepository(
            RestClient restClient,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi4ToLifecycle33 ddi4ToLifecycle33
    ) {
        this.restClient = restClient;
        this.instanceConfiguration = instanceConfiguration;
        this.authenticator = authenticator;
        this.ddi4ToLifecycle33 = ddi4ToLifecycle33;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected void createOrUpdateItem(String itemTypeUuid, Ddi4Item item) {
        String ddi3Xml = serializeToDdi3Xml(item);
        logger.info("DDI3 XML for item id={}: {}", item.id(), ddi3Xml);
        authenticator.executeWithAuth(token -> {
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

    private String serializeToDdi3Xml(Ddi4Item item) {
        FragmentDocument fragment;
        String contentNamespace;
        if (item instanceof Ddi4Group g) {
            fragment = ddi4ToLifecycle33.toGroup(g);
            contentNamespace = DDI_GROUP_NS;
        } else if (item instanceof Ddi4StudyUnit su) {
            fragment = ddi4ToLifecycle33.toStudyUnit(su);
            contentNamespace = DDI_STUDY_UNIT_NS;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported Ddi4Item subtype for Colectica serialization: " + item.getClass().getName());
        }
        return fragment.xmlText(fragmentXmlOptions(contentNamespace));
    }

    private static XmlOptions fragmentXmlOptions(String contentNs) {
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "");
        prefixes.put(contentNs, "");
        prefixes.put(DDI_REUSABLE_NS, "r");
        XmlOptions options = new XmlOptions();
        options.setSaveSuggestedPrefixes(prefixes);
        return options;
    }

    public static String generateDeterministicUuid(String uri) {
        return UUID.nameUUIDFromBytes(uri.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
