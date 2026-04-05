package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Domain service for DDI Group items.
 * <p>
 * Inherits common {@code createOrUpdate} logic from {@link AbstractDdiItemService}
 * and adds the Group-specific {@code deprecateAll()} and {@code getAll()} operations.
 */
public class GroupServiceImpl extends AbstractDdiItemService<Ddi4Group> implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        super(groupRepository);
        this.groupRepository = groupRepository;
    }

    @Override
    public List<PartialGroup> getAll() {
        logger.info("Getting all groups");
        return groupRepository.getAll();
    }

    @Override
    public void deprecateAll() {
        logger.info("Deprecating all groups");
        groupRepository.deprecateAll();
    }

    @Override
    protected String itemTypeName() {
        return "group";
    }
}
