package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for DDI Group items.
 * <p>
 * Inherits common {@code createOrUpdate} logic from {@link AbstractDdiItemService}
 * and adds the Group-specific {@code deprecate(...)} and {@code getAll()} operations.
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
        return groupRepository.getAll().stream()
                .sorted(LabelComparators.byLabelDescending(PartialGroup::label))
                .toList();
    }

    @Override
    public void deprecate(Collection<String> groupIds) {
        logger.info("Deprecating {} targeted group(s)", groupIds != null ? groupIds.size() : 0);
        groupRepository.deprecate(groupIds);
    }

    @Override
    protected String itemTypeName() {
        return "group";
    }
}
