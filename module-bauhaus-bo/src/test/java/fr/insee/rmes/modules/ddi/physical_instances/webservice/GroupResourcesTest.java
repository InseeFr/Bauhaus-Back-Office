package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StringValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StudyUnitReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Title;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupResourcesTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupResources groupResources;

    @Test
    void getGroups_shouldReturn200WithList() {
        List<PartialGroup> groups = List.of(
                new PartialGroup("group-1", "Group 1", new Date(), "fr.insee", List.of()),
                new PartialGroup("group-2", "Group 2", new Date(), "fr.insee", List.of())
        );
        when(groupService.getAll()).thenReturn(groups);

        ResponseEntity<List<PartialGroup>> response = groupResources.getGroups();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("group-1");
        verify(groupService).getAll();
    }

    @Test
    void getGroups_shouldReturn500OnError() {
        when(groupService.getAll()).thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialGroup>> response = groupResources.getGroups();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createOrUpdateGroup_shouldReturn201() {
        Ddi4Group group = new Ddi4Group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(new Title(new StringValue("fr-FR", "Test Group"))),
                List.of(new StudyUnitReference("fr.insee", "su-id", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries"
        );

        ResponseEntity<Void> response = groupResources.createOrUpdateGroup(group);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(groupService).createOrUpdate(group);
    }

    @Test
    void createOrUpdateGroup_shouldReturn500OnError() {
        Ddi4Group group = new Ddi4Group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(new Title(new StringValue("fr-FR", "Test Group"))),
                List.of(),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries"
        );

        doThrow(new RuntimeException("Colectica error")).when(groupService).createOrUpdate(group);

        ResponseEntity<Void> response = groupResources.createOrUpdateGroup(group);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
