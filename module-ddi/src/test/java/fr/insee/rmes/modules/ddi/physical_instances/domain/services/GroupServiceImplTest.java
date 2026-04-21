package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StringValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StudyUnitReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Title;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    private GroupServiceImpl groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupServiceImpl(groupRepository);
    }

    @Test
    void createOrUpdate_shouldDelegateToRepository() {
        Ddi4Group group = new Ddi4Group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(new Title(new StringValue("fr-FR", "Test Group"))),
                List.of(new StudyUnitReference("fr.insee", "su-id", "1", "StudyUnit")),
                "http://id.insee.fr/operations/serie/s1001",
                "insee:StatisticalOperationSeries"
        );

        groupService.createOrUpdate(group);

        verify(groupRepository).createOrUpdate(group);
    }

    @Test
    void getAll_shouldDelegateToRepository() {
        List<PartialGroup> expected = List.of(
                new PartialGroup("g1", "Group 1", new Date(), "fr.insee")
        );
        when(groupRepository.getAll()).thenReturn(expected);

        List<PartialGroup> result = groupService.getAll();

        assertThat(result).isEqualTo(expected);
        verify(groupRepository).getAll();
    }

    @Test
    void deprecateAll_shouldDelegateToRepository() {
        groupService.deprecateAll();

        verify(groupRepository).deprecateAll();
    }
}
