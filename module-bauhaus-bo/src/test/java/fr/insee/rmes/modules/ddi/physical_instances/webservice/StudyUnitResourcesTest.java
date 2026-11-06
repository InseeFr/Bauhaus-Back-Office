package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class StudyUnitResourcesTest {

    @Mock
    private StudyUnitService studyUnitService;

    @Mock
    private DDIService ddiService;

    @InjectMocks
    private StudyUnitResources studyUnitResources;

    @Test
    void getStudyUnits_shouldReturn200WithList() {
        List<PartialStudyUnit> studyUnits = List.of(
                new PartialStudyUnit("su-1", "StudyUnit 1", new Date(), "fr.insee"),
                new PartialStudyUnit("su-2", "StudyUnit 2", new Date(), "fr.insee"));
        when(studyUnitService.getAll()).thenReturn(studyUnits);

        ResponseEntity<List<PartialStudyUnit>> response = studyUnitResources.getStudyUnits();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo("su-1");
        verify(studyUnitService).getAll();
    }

    @Test
    void createOrUpdateStudyUnit_shouldReturn201() {
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1",
                "fr.insee",
                "su-id",
                "1",
                new Citation(LangStrings.of("fr-FR", "Test StudyUnit")),
                "http://id.insee.fr/operations/operation/op1",
                null);

        ResponseEntity<Void> response = studyUnitResources.createOrUpdateStudyUnit(studyUnit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(studyUnitService).createOrUpdate(studyUnit);
    }

    @Test
    void getStudyUnitVariables_shouldReturn200WithTheVariablesOfTheStudyUnitVariableScheme() {
        Ddi4Variable variable = new Ddi4Variable(
                Ddi4Variable.TYPE,
                null,
                "urn:ddi:fr.insee:var-1:2",
                "fr.insee",
                "var-1",
                "2",
                null,
                LangStrings.of("fr-FR", "SEXE"),
                LangStrings.of("fr-FR", "Sexe"),
                null,
                null,
                null);
        Ddi4Response variables =
                new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, List.of(variable), null, null, null);
        when(ddiService.getStudyUnitVariables("fr.insee", "su-1")).thenReturn(variables);

        ResponseEntity<Ddi4Response> response = studyUnitResources.getStudyUnitVariables("fr.insee", "su-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(variables);
    }

    @Test
    void getStudyUnitVariableUsages_shouldReturn200WithTheVariablesOfEachPhysicalInstance() {
        List<CodeListVariableUsage> usages = List.of(new CodeListVariableUsage(
                "fr.insee", "su-1", null, "fr.insee", "pi-1", "Fichier 2024", "fr.insee", "var-1", "Sexe"));
        when(ddiService.getStudyUnitVariableUsages("fr.insee", "su-1")).thenReturn(usages);

        ResponseEntity<List<CodeListVariableUsage>> response =
                studyUnitResources.getStudyUnitVariableUsages("fr.insee", "su-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(usages);
    }
}
