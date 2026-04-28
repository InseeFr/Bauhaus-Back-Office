package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.springframework.web.client.RestTemplate;
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
    private RestTemplate restTemplate;

    private Ddi4Response piResponse(String agency, String id) {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance("true", "2026-01-01T00:00:00Z",
                "urn:ddi:%s:%s:1".formatted(agency, id), agency, id, "1", null, null, null);
        return new Ddi4Response(null, null, List.of(pi), null, null, null, null);
    }

    private ColecticaConfiguration createColecticaConfig() {
        var instanceConfig = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                "http://localhost:8082", "/api/v1/", null,
                "bauhaus", "DC337820-AF3A-4C0B-82F9-CF02535CDE83",
                "token", null, null, "fr.insee"
        );
        return new ColecticaConfiguration(List.of("fr-FR"), instanceConfig, null, null);
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
                createColecticaConfig(), colecticaAuthenticator, restTemplate,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: deprecate first, then study units, then group
        InOrder inOrder = inOrder(groupService, studyUnitService);
        inOrder.verify(groupService).deprecateAll();

        // Verify study units created with Ddi4StudyUnit model
        ArgumentCaptor<Ddi4StudyUnit> suCaptor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(studyUnitService, times(2)).createOrUpdate(suCaptor.capture());
        List<Ddi4StudyUnit> studyUnits = suCaptor.getAllValues();
        assertThat(studyUnits.get(0).citation().title().string().text()).isEqualTo("Enquête innovation 2020 Study Unit");
        assertThat(studyUnits.get(0).operationIri()).isEqualTo("http://id.insee.fr/operations/operation/op1");
        assertThat(studyUnits.get(1).citation().title().string().text()).isEqualTo("Enquête innovation 2021 Study Unit");

        // Verify group created with Ddi4Group model
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(groupService).createOrUpdate(groupCaptor.capture());
        Ddi4Group createdGroup = groupCaptor.getValue();
        assertThat(createdGroup.citation().title().string().text()).isEqualTo("Enquête innovation Group");
        assertThat(createdGroup.seriesIris()).containsExactly("http://id.insee.fr/operations/serie/s1001");
        assertThat(createdGroup.typeOfGroup()).isEqualTo("insee:StatisticalOperationSeries");
        assertThat(createdGroup.studyUnitReference()).hasSize(2);
        assertThat(createdGroup.agency()).isEqualTo("fr.insee");
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
                createColecticaConfig(), colecticaAuthenticator, restTemplate,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: group created with empty StudyUnitReferences, no study units
        verify(groupService).deprecateAll();
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(groupService).createOrUpdate(groupCaptor.capture());
        assertThat(groupCaptor.getValue().citation().title().string().text()).isEqualTo("Enquête innovation Group");
        assertThat(groupCaptor.getValue().studyUnitReference()).isEmpty();
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
                createColecticaConfig(), colecticaAuthenticator, restTemplate,
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
}
