package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.UpdateItemStateRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi4ToLifecycle33;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColecticaGroupRepository extends AbstractColecticaItemRepository implements GroupRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaGroupRepository.class);
    private static final String GROUP_ITEM_TYPE = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

    private final DDIRepository ddiRepository;

    public ColecticaGroupRepository(
            ColecticaClient colecticaClient,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            Ddi4ToLifecycle33 ddi4ToLifecycle33,
            DDIRepository ddiRepository) {
        super(colecticaClient, instanceConfiguration, ddi4ToLifecycle33);
        this.ddiRepository = ddiRepository;
    }

    @Override
    public void createOrUpdate(Ddi4Group group) {
        logger.info(
                "Creating/updating group in Colectica: id={}, agency={}, urn={}",
                group.id(),
                group.agency(),
                group.urn());
        try {
            createOrUpdateItem(GROUP_ITEM_TYPE, group);
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
    public void deprecate(Collection<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            logger.info("No group id provided to deprecate");
            return;
        }
        List<UpdateItemStateRequest.ItemIdentifier> ids = ddiRepository.getGroups().stream()
                .filter(group -> groupIds.contains(group.id()))
                .map(group -> {
                    String agency = group.agency() != null ? group.agency() : instanceConfiguration.defaultAgencyId();
                    return new UpdateItemStateRequest.ItemIdentifier(agency, group.id(), 1);
                })
                .toList();
        if (ids.isEmpty()) {
            logger.info(
                    "None of the {} requested group id(s) exist in Colectica: nothing to deprecate", groupIds.size());
            return;
        }
        colecticaClient.updateItemState(new UpdateItemStateRequest(ids, true, true));
        logger.info("Deprecated {} group(s) from Colectica", ids.size());
    }
}
