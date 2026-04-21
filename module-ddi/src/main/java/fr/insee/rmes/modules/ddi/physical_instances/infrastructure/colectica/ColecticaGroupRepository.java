package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi3XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Colectica adapter for Group persistence.
 * <p>
 * Transforms {@link Ddi4Group} to DDI3 XML via {@link Ddi3XmlWriter},
 * then delegates the REST call to the parent class.
 */
public class ColecticaGroupRepository extends AbstractColecticaItemRepository implements GroupRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaGroupRepository.class);
    private static final String GROUP_ITEM_TYPE = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

    private final DDIRepository ddiRepository;

    public ColecticaGroupRepository(
            RestTemplate restTemplate,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi3XmlWriter ddi3XmlWriter,
            DDIRepository ddiRepository
    ) {
        super(restTemplate, instanceConfiguration, authenticator, ddi3XmlWriter);
        this.ddiRepository = ddiRepository;
    }

    @Override
    public void createOrUpdate(Ddi4Group group) {
        logger.info("Creating/updating group in Colectica: id={}, agency={}, urn={}", group.id(), group.agency(), group.urn());
        try {
            String ddi3Xml = ddi3XmlWriter.buildGroupXml(group);
            logger.info("Generated DDI3 XML for group id={}: {}", group.id(), ddi3Xml);
            createOrUpdateItem(GROUP_ITEM_TYPE, group, ddi3Xml);
            logger.info("Group successfully sent to Colectica: id={}", group.id());
        } catch (RuntimeException e) {
            logger.error("Unexpected error creating group in Colectica: id={}", group.id(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating group in Colectica: id={}", group.id(), e);
            throw new RuntimeException("Failed to create group: " + group.id(), e);
        }
    }

    @Override
    public List<PartialGroup> getAll() {
        logger.info("Getting all groups from Colectica");
        return ddiRepository.getGroups();
    }

    @Override
    public void deprecateAll() {
        logger.info("Deprecating all groups from Colectica");

        List<PartialGroup> groups = ddiRepository.getGroups();
        if (groups.isEmpty()) {
            logger.info("No groups found to deprecate");
            return;
        }

        authenticator.executeWithAuth(token -> {
            String url = instanceConfiguration.baseApiUrl() + "item/_updateState";

            List<Map<String, Object>> ids = groups.stream()
                    .map(group -> {
                        String agency = group.agency() != null ? group.agency() : instanceConfiguration.defaultAgencyId();
                        return Map.<String, Object>of(
                                "agencyId", agency,
                                "identifier", group.id(),
                                "version", 1
                        );
                    })
                    .toList();

            Map<String, Object> requestBody = Map.of(
                    "ids", ids,
                    "state", true,
                    "applyToAllVersions", true
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            restTemplate.postForObject(url, requestEntity, String.class);

            logger.info("Deprecated {} group(s) from Colectica", groups.size());
            return null;
        });
    }
}
