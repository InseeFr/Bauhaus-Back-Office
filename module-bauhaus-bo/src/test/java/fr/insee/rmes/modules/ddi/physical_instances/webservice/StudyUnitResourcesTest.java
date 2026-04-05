package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StringValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Title;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
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
class StudyUnitResourcesTest {

    @Mock
    private StudyUnitService studyUnitService;

    @InjectMocks
    private StudyUnitResources studyUnitResources;

    @Test
    void getStudyUnits_shouldReturn200WithList() {
        List<PartialStudyUnit> studyUnits = List.of(
                new PartialStudyUnit("su-1", "StudyUnit 1", new Date(), "fr.insee"),
                new PartialStudyUnit("su-2", "StudyUnit 2", new Date(), "fr.insee")
        );
        when(studyUnitService.getAll()).thenReturn(studyUnits);

        ResponseEntity<List<PartialStudyUnit>> response = studyUnitResources.getStudyUnits();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("su-1");
        verify(studyUnitService).getAll();
    }

    @Test
    void getStudyUnits_shouldReturn500OnError() {
        when(studyUnitService.getAll()).thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<PartialStudyUnit>> response = studyUnitResources.getStudyUnits();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createOrUpdateStudyUnit_shouldReturn201() {
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(new Title(new StringValue("fr-FR", "Test StudyUnit"))),
                "http://id.insee.fr/operations/operation/op1",
                null
        );

        ResponseEntity<Void> response = studyUnitResources.createOrUpdateStudyUnit(studyUnit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(studyUnitService).createOrUpdate(studyUnit);
    }

    @Test
    void createOrUpdateStudyUnit_shouldReturn500OnError() {
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(new Title(new StringValue("fr-FR", "Test StudyUnit"))),
                "http://id.insee.fr/operations/operation/op1",
                null
        );

        doThrow(new RuntimeException("Colectica error")).when(studyUnitService).createOrUpdate(studyUnit);

        ResponseEntity<Void> response = studyUnitResources.createOrUpdateStudyUnit(studyUnit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
