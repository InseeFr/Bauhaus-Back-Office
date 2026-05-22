package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.springframework.web.client.RestClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalColecticaGroupInitConfigurationTest {

    @Mock
    private GroupService groupService;

    @Mock
    private StudyUnitService studyUnitService;

    @Mock
    private DDIService ddiService;

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private ColecticaAuthenticator colecticaAuthenticator;

    @Mock
    private RestClient restClient;

    private Ddi4Response piResponse(String agency, String id) {
        PhysicalInstance pi = new PhysicalInstance();
        pi.setURN("urn:ddi:%s:%s:1".formatted(agency, id));
        pi.setAgency(agency);
        pi.setID(id);
        pi.setVersion("1");
        pi.putAdditionalProperty("@isUniversallyUnique", "true");
        pi.putAdditionalProperty("@versionDate", "2026-01-01T00:00:00Z");
        return new Ddi4Response(null, null, List.of(pi), null, null, null, null);
    }

    private ColecticaConfiguration createColecticaConfig() {
        var instanceConfig = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                "http://localhost:8082", "/api/v1/", null,
                "bauhaus", "DC337820-AF3A-4C0B-82F9-CF02535CDE83",
                "token", null, null, "fr.insee"
        );
        return new ColecticaConfiguration(List.of("fr-FR"), instanceConfig, null);
    }

    @Test
    void shouldDeleteAllGroupsThenCreateStudyUnitsThenGroups() throws Exception {
        // Given: SPARQL returns 1 series with 2 operations
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation")
                .put("operationId", "op1")
                .put("operationIri", "http://id.insee.fr/operations/operation/op1")
                .put("operationLabel", "Enquête innovation 2020"));
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation")
                .put("operationId", "op2")
                .put("operationIri", "http://id.insee.fr/operations/operation/op2")
                .put("operationLabel", "Enquête innovation 2021"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(sparqlResults);
        when(ddiService.createPhysicalInstance(any()))
                .thenReturn(piResponse("fr.insee", "pi-uuid-1"))
                .thenReturn(piResponse("fr.insee", "pi-uuid-2"));

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryGestion,
                createColecticaConfig(), colecticaAuthenticator, restClient,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: deprecate first, then study units, then group
        InOrder inOrder = inOrder(groupService, studyUnitService);
        inOrder.verify(groupService).deprecateAll();

        // Verify study units created with StudyUnit model
        ArgumentCaptor<StudyUnit> suCaptor = ArgumentCaptor.forClass(StudyUnit.class);
        verify(studyUnitService, times(2)).createOrUpdate(suCaptor.capture());
        List<StudyUnit> studyUnits = suCaptor.getAllValues();
        assertThat(citationOf(studyUnits.get(0)).title().get(0).getAtValue()).isEqualTo("Enquête innovation 2020 Study Unit");
        assertThat((String) studyUnits.get(0).getAdditionalProperties().get("operationIri")).isEqualTo("http://id.insee.fr/operations/operation/op1");
        assertThat(citationOf(studyUnits.get(1)).title().get(0).getAtValue()).isEqualTo("Enquête innovation 2021 Study Unit");

        // Verify group created with Group model
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupService).createOrUpdate(groupCaptor.capture());
        Group createdGroup = groupCaptor.getValue();
        assertThat(citationOf(createdGroup).title().get(0).getAtValue()).isEqualTo("Enquête innovation Group");
        assertThat((List<String>) createdGroup.getAdditionalProperties().get("seriesIris")).containsExactly("http://id.insee.fr/operations/serie/s1001");
        assertThat((String) createdGroup.getAdditionalProperties().get("typeOfGroup")).isEqualTo("insee:StatisticalOperationSeries");
        assertThat((List<?>) createdGroup.getAdditionalProperties().get("StudyUnitReference")).hasSize(2);
        assertThat(createdGroup.getAgency()).isEqualTo("fr.insee");
    }

    @Test
    void shouldHandleSeriesWithoutOperations() throws Exception {
        // Given: 1 series with no operations
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryGestion,
                createColecticaConfig(), colecticaAuthenticator, restClient,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: group created with empty StudyUnitReferences, no study units
        verify(groupService).deprecateAll();
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupService).createOrUpdate(groupCaptor.capture());
        assertThat(citationOf(groupCaptor.getValue()).title().get(0).getAtValue()).isEqualTo("Enquête innovation Group");
        assertThat((List<?>) groupCaptor.getValue().getAdditionalProperties().get("StudyUnitReference")).isEmpty();
        verify(studyUnitService, never()).createOrUpdate(any());
    }

    @Test
    void shouldContinueWhenOneStudyUnitCreationFails() throws Exception {
        // Given
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation")
                .put("operationId", "op1")
                .put("operationIri", "http://id.insee.fr/operations/operation/op1")
                .put("operationLabel", "Enquête innovation 2020"));
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation")
                .put("operationId", "op2")
                .put("operationIri", "http://id.insee.fr/operations/operation/op2")
                .put("operationLabel", "Enquête innovation 2021"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        when(ddiService.createPhysicalInstance(any()))
                .thenReturn(piResponse("fr.insee", "pi-uuid-1"));

        // First study unit creation fails
        doThrow(new RuntimeException("API error"))
                .doNothing()
                .when(studyUnitService).createOrUpdate(any());

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryGestion,
                createColecticaConfig(), colecticaAuthenticator, restClient,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: second study unit and group are still created
        verify(studyUnitService, times(2)).createOrUpdate(any());
        verify(groupService).createOrUpdate(any());
    }

    @Test
    void querySeriesAndOperations_shouldGroupOperationsBySeries() throws Exception {
        // Given: 2 series, first with 2 ops, second with 1 op
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001").put("seriesIri", "iri:s1001").put("seriesLabel", "Série A")
                .put("operationId", "op1").put("operationIri", "iri:op1").put("operationLabel", "Opération 1"));
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001").put("seriesIri", "iri:s1001").put("seriesLabel", "Série A")
                .put("operationId", "op2").put("operationIri", "iri:op2").put("operationLabel", "Opération 2"));
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s2001").put("seriesIri", "iri:s2001").put("seriesLabel", "Série B")
                .put("operationId", "op3").put("operationIri", "iri:op3").put("operationLabel", "Opération 3"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        var result = config.querySeriesAndOperations(repositoryGestion, "http://rdf.insee.fr/graphes/operations");

        // 2 series
        var s1001 = result.stream().filter(s -> s.seriesId().equals("s1001")).findFirst().orElseThrow();
        var s2001 = result.stream().filter(s -> s.seriesId().equals("s2001")).findFirst().orElseThrow();

        assertThat(s1001.operations()).hasSize(2);
        assertThat(s1001.seriesLabel()).isEqualTo("Série A");
        assertThat(s2001.operations()).hasSize(1);
        assertThat(s2001.seriesLabel()).isEqualTo("Série B");
    }
    private static Citation citationOf(Group group) {
        return (Citation) group.getAdditionalProperties().get("Citation");
    }

    private static Citation citationOf(StudyUnit studyUnit) {
        return (Citation) studyUnit.getAdditionalProperties().get("Citation");
    }
}
