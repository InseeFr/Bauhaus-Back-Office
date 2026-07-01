package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.colectica.client.ColecticaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.AbstractColecticaItemRepository.generateDeterministicUuid;
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
    private RepositoryPublicationReader repositoryPublicationReader;

    @Mock
    private ColecticaClient colecticaClient;

    private Ddi4Response piResponse(String agency, String id) {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,CogsDate.ofDateTime("2026-01-01T00:00:00Z"),
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
    void shouldDeprecateOnlyManipulatedGroupsAndStudyUnitsThenCreate() throws Exception {
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

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);
        when(ddiService.createPhysicalInstance(any()))
                .thenReturn(piResponse("fr.insee", "pi-uuid-1"))
                .thenReturn(piResponse("fr.insee", "pi-uuid-2"));

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryPublicationReader,
                createColecticaConfig(), colecticaClient,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: deprecation targets EVERY variant id of the manipulated series/operations, before creating
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        Set<String> expectedGroupIds = Set.copyOf(
                LocalColecticaGroupInitConfiguration.variantUuids("http://id.insee.fr/operations/serie/s1001"));
        Set<String> expectedStudyUnitIds = new HashSet<>();
        expectedStudyUnitIds.addAll(LocalColecticaGroupInitConfiguration.variantUuids("http://id.insee.fr/operations/operation/op1"));
        expectedStudyUnitIds.addAll(LocalColecticaGroupInitConfiguration.variantUuids("http://id.insee.fr/operations/operation/op2"));

        InOrder inOrder = inOrder(groupService, studyUnitService);
        inOrder.verify(groupService).deprecate(expectedGroupIds);
        inOrder.verify(studyUnitService).deprecate(expectedStudyUnitIds);
        inOrder.verify(studyUnitService, times(2 * variants)).createOrUpdate(any());
        inOrder.verify(groupService, times(variants)).createOrUpdate(any());

        // 5 study unit variants per operation, distinct labels, in the (non-alphabetical) creation order
        ArgumentCaptor<Ddi4StudyUnit> suCaptor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(studyUnitService, times(2 * variants)).createOrUpdate(suCaptor.capture());
        List<Ddi4StudyUnit> studyUnits = suCaptor.getAllValues();
        assertThat(studyUnits).hasSize(2 * variants);
        assertThat(studyUnits.subList(0, variants))
                .allSatisfy(su -> assertThat(su.operationIri()).isEqualTo("http://id.insee.fr/operations/operation/op1"))
                .extracting(su -> su.citation().title().get(0).value())
                .containsExactly(
                        "Enquête innovation 2020 Zoulou Study Unit",
                        "Enquête innovation 2020 Alpha Study Unit",
                        "Enquête innovation 2020 Mike Study Unit",
                        "Enquête innovation 2020 Bravo Study Unit",
                        "Enquête innovation 2020 Yankee Study Unit");

        // 5 group variants for the series, distinct labels, in the (non-alphabetical) creation order
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(groupService, times(variants)).createOrUpdate(groupCaptor.capture());
        List<Ddi4Group> groups = groupCaptor.getAllValues();
        assertThat(groups)
                .extracting(g -> g.citation().title().get(0).value())
                .containsExactly(
                        "Enquête innovation Zoulou Group",
                        "Enquête innovation Alpha Group",
                        "Enquête innovation Mike Group",
                        "Enquête innovation Bravo Group",
                        "Enquête innovation Yankee Group");
        assertThat(groups).allSatisfy(group -> {
            assertThat(group.seriesIris()).containsExactly("http://id.insee.fr/operations/serie/s1001");
            assertThat(group.typeOfGroup()).isEqualTo("insee:StatisticalOperationSeries");
            assertThat(group.studyUnitReference()).hasSize(2); // one ref per operation, same variant
            assertThat(group.agency()).isEqualTo("fr.insee");
        });
    }

    @Test
    void shouldCreateOneEmptyCodeListSchemeAndLogicalProductPerGroupVariant() throws Exception {
        // Given: 1 series, no operations (keeps the test focused on the Group -> LogicalProduct ->
        // CodeListScheme chain, independent of study units / physical instances)
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation"));

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryPublicationReader,
                createColecticaConfig(), colecticaClient,
                "http://rdf.insee.fr/graphes/", "operations");

        // When
        runner.run();

        // Then: one CodeListScheme and one LogicalProduct per group variant
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        ArgumentCaptor<Ddi4CodeListScheme> schemeCaptor = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(ddiService, times(variants)).createCodeListScheme(schemeCaptor.capture());
        verify(ddiService, times(variants)).createLogicalProduct(lpCaptor.capture());
        verify(groupService, times(variants)).createOrUpdate(groupCaptor.capture());

        // Each CodeListScheme is created empty (no code list referenced yet)
        assertThat(schemeCaptor.getAllValues()).allSatisfy(scheme -> {
            assertThat(scheme.agency()).isEqualTo("fr.insee");
            assertThat(scheme.codeListReference()).isNullOrEmpty();
        });

        // Per variant the chain is wired together: the i-th LogicalProduct references the i-th
        // CodeListScheme, and the i-th Group references the i-th LogicalProduct.
        List<Ddi4CodeListScheme> schemes = schemeCaptor.getAllValues();
        List<Ddi4LogicalProduct> logicalProducts = lpCaptor.getAllValues();
        List<Ddi4Group> groups = groupCaptor.getAllValues();
        for (int variant = 0; variant < variants; variant++) {
            Ddi4LogicalProduct lp = logicalProducts.get(variant);
            assertThat(lp.codeListSchemeReference()).hasSize(1);
            assertThat(lp.codeListSchemeReference().get(0).id()).isEqualTo(schemes.get(variant).id());
            assertThat(lp.codeListSchemeReference().get(0).type()).isEqualTo("CodeListScheme");

            assertThat(groups.get(variant).logicalProductReference()).hasSize(1);
            assertThat(groups.get(variant).logicalProductReference().get(0).id()).isEqualTo(lp.id());
            assertThat(groups.get(variant).logicalProductReference().get(0).type()).isEqualTo("LogicalProduct");
        }

        // Creation order, per variant: CodeListScheme, then LogicalProduct, then the Group referencing it
        InOrder inOrder = inOrder(ddiService, groupService);
        inOrder.verify(ddiService).createCodeListScheme(any());
        inOrder.verify(ddiService).createLogicalProduct(any());
        inOrder.verify(groupService).createOrUpdate(any());
    }

    @Test
    void shouldCreateOneEmptyCategorySchemePerGroupVariantFiledInTheGroupLogicalProduct() throws Exception {
        // Given: 1 series, no operations (keeps the test focused on the Group -> LogicalProduct ->
        // CategoryScheme chain; without operations no study-unit LogicalProduct is created, so every
        // captured LogicalProduct is a group LogicalProduct)
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation"));

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryPublicationReader,
                createColecticaConfig(), colecticaClient,
                "http://rdf.insee.fr/graphes/", "operations");

        // When
        runner.run();

        // Then: one CategoryScheme per group variant, each created empty and filed in the group LogicalProduct
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        ArgumentCaptor<Ddi4CategoryScheme> categoryCaptor = ArgumentCaptor.forClass(Ddi4CategoryScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        verify(ddiService, times(variants)).createCategoryScheme(categoryCaptor.capture());
        verify(ddiService, times(variants)).createLogicalProduct(lpCaptor.capture());

        assertThat(categoryCaptor.getAllValues()).allSatisfy(scheme -> {
            assertThat(scheme.agency()).isEqualTo("fr.insee");
            assertThat(scheme.categoryReference()).isNullOrEmpty();
        });

        // The i-th group LogicalProduct references the i-th CategoryScheme, alongside its CodeListScheme
        List<Ddi4CategoryScheme> categorySchemes = categoryCaptor.getAllValues();
        List<Ddi4LogicalProduct> logicalProducts = lpCaptor.getAllValues();
        for (int variant = 0; variant < variants; variant++) {
            Ddi4LogicalProduct lp = logicalProducts.get(variant);
            assertThat(lp.codeListSchemeReference()).hasSize(1);
            assertThat(lp.categorySchemeReference()).hasSize(1);
            assertThat(lp.categorySchemeReference().get(0).id()).isEqualTo(categorySchemes.get(variant).id());
            assertThat(lp.categorySchemeReference().get(0).type()).isEqualTo("CategoryScheme");
        }

        // Per variant, the CategoryScheme is created before the group LogicalProduct that files it
        InOrder inOrder = inOrder(ddiService);
        inOrder.verify(ddiService).createCategoryScheme(any());
        inOrder.verify(ddiService).createLogicalProduct(any());
    }

    @Test
    void shouldCreateOneVariableSchemeAndStudyUnitLogicalProductPerSeriesReferencedByEachStudyUnit() throws Exception {
        // Given: 1 series with 1 operation
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation")
                .put("operationId", "op1")
                .put("operationIri", "http://id.insee.fr/operations/operation/op1")
                .put("operationLabel", "Enquête innovation 2020"));

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);
        when(ddiService.createPhysicalInstance(any()))
                .thenReturn(piResponse("fr.insee", "pi-uuid-1"));

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryPublicationReader,
                createColecticaConfig(), colecticaClient,
                "http://rdf.insee.fr/graphes/", "operations");

        // When
        runner.run();

        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();

        // Exactly ONE VariableScheme for the series (not per variant), created empty
        ArgumentCaptor<Ddi4VariableScheme> vsCaptor = ArgumentCaptor.forClass(Ddi4VariableScheme.class);
        verify(ddiService, times(1)).createVariableScheme(vsCaptor.capture());
        Ddi4VariableScheme variableScheme = vsCaptor.getValue();
        assertThat(variableScheme.agency()).isEqualTo("fr.insee");
        assertThat(variableScheme.variableReference()).isNullOrEmpty();

        // Exactly ONE study-unit LogicalProduct for the series, filing that VariableScheme
        // (distinguishable from the group LogicalProducts by its variableSchemeReference)
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        verify(ddiService, atLeastOnce()).createLogicalProduct(lpCaptor.capture());
        List<Ddi4LogicalProduct> studyUnitLps = lpCaptor.getAllValues().stream()
                .filter(lp -> lp.variableSchemeReference() != null && !lp.variableSchemeReference().isEmpty())
                .toList();
        assertThat(studyUnitLps).hasSize(1);
        Ddi4LogicalProduct studyUnitLp = studyUnitLps.get(0);
        assertThat(studyUnitLp.variableSchemeReference().get(0).id()).isEqualTo(variableScheme.id());
        assertThat(studyUnitLp.variableSchemeReference().get(0).type()).isEqualTo("VariableScheme");
        assertThat(studyUnitLp.codeListSchemeReference()).isNullOrEmpty();

        // Every study unit (all variants of the operation) files that per-series study-unit LogicalProduct
        ArgumentCaptor<Ddi4StudyUnit> suCaptor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(studyUnitService, times(variants)).createOrUpdate(suCaptor.capture());
        assertThat(suCaptor.getAllValues()).allSatisfy(su -> {
            assertThat(su.logicalProductReferences()).hasSize(1);
            assertThat(su.logicalProductReferences().get(0).id()).isEqualTo(studyUnitLp.id());
            assertThat(su.logicalProductReferences().get(0).type()).isEqualTo("LogicalProduct");
        });

        // Ordering: the VariableScheme and its LogicalProduct exist before the study units that reference them
        InOrder inOrder = inOrder(ddiService, studyUnitService);
        inOrder.verify(ddiService).createVariableScheme(any());
        inOrder.verify(ddiService).createLogicalProduct(any());
        inOrder.verify(studyUnitService, atLeastOnce()).createOrUpdate(any());
    }

    @Test
    void shouldHandleSeriesWithoutOperations() throws Exception {
        // Given: 1 series with no operations
        JSONArray sparqlResults = new JSONArray();
        sparqlResults.put(new JSONObject()
                .put("seriesId", "s1001")
                .put("seriesIri", "http://id.insee.fr/operations/serie/s1001")
                .put("seriesLabel", "Enquête innovation"));

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryPublicationReader,
                createColecticaConfig(), colecticaClient,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: all group variants of the manipulated series are deprecated (no operations => empty
        // study unit set), each group variant created with empty StudyUnitReferences, no study units
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        Set<String> expectedGroupIds = Set.copyOf(
                LocalColecticaGroupInitConfiguration.variantUuids("http://id.insee.fr/operations/serie/s1001"));
        verify(groupService).deprecate(expectedGroupIds);
        verify(studyUnitService).deprecate(Set.of());
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(groupService, times(variants)).createOrUpdate(groupCaptor.capture());
        assertThat(groupCaptor.getAllValues())
                .extracting(g -> g.citation().title().get(0).value())
                .containsExactly(
                        "Enquête innovation Zoulou Group",
                        "Enquête innovation Alpha Group",
                        "Enquête innovation Mike Group",
                        "Enquête innovation Bravo Group",
                        "Enquête innovation Yankee Group");
        assertThat(groupCaptor.getAllValues()).allSatisfy(g -> assertThat(g.studyUnitReference()).isEmpty());
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

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        when(ddiService.createPhysicalInstance(any()))
                .thenReturn(piResponse("fr.insee", "pi-uuid-1"));

        // First study unit creation fails
        doThrow(new RuntimeException("API error"))
                .doNothing()
                .when(studyUnitService).createOrUpdate(any());

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaGroups(
                groupService, studyUnitService, ddiService, repositoryPublicationReader,
                createColecticaConfig(), colecticaClient,
                "http://rdf.insee.fr/graphes/", "operations"
        );

        // When
        runner.run();

        // Then: the remaining study unit variants and all group variants are still created
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        verify(studyUnitService, times(2 * variants)).createOrUpdate(any());
        verify(groupService, times(variants)).createOrUpdate(any());
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

        when(repositoryPublicationReader.getResponseAsArray(anyString())).thenReturn(sparqlResults);

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        var result = config.querySeriesAndOperations(repositoryPublicationReader, "http://rdf.insee.fr/graphes/operations");

        // 2 series
        var s1001 = result.stream().filter(s -> s.seriesId().equals("s1001")).findFirst().orElseThrow();
        var s2001 = result.stream().filter(s -> s.seriesId().equals("s2001")).findFirst().orElseThrow();

        assertThat(s1001.operations()).hasSize(2);
        assertThat(s1001.seriesLabel()).isEqualTo("Série A");
        assertThat(s2001.operations()).hasSize(1);
        assertThat(s2001.seriesLabel()).isEqualTo("Série B");
    }

    @Test
    void buildDdiAssociations_mapsEachSeriesToGroupAndEachOperationToStudyUnit() {
        var series = new LocalColecticaGroupInitConfiguration.SeriesWithOperations(
                "s1001", "http://id.insee.fr/operations/serie/s1001", "Série A",
                List.of(
                        new LocalColecticaGroupInitConfiguration.OperationInfo("op1", "http://id.insee.fr/operations/operation/op1", "Opération 1"),
                        new LocalColecticaGroupInitConfiguration.OperationInfo("op2", "http://id.insee.fr/operations/operation/op2", "Opération 2")));

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        var associations = config.buildDdiAssociations(List.of(series));

        assertThat(associations).hasSize(1);
        var association = associations.get(0);
        assertThat(association.series().seriesId()).isEqualTo("s1001");
        assertThat(association.groupId())
                .isEqualTo(generateDeterministicUuid("http://id.insee.fr/operations/serie/s1001"));
        assertThat(association.operations()).hasSize(2);
        assertThat(association.operations().get(0).operation().operationId()).isEqualTo("op1");
        assertThat(association.operations().get(0).studyUnitId())
                .isEqualTo(generateDeterministicUuid("http://id.insee.fr/operations/operation/op1"));
        assertThat(association.operations().get(1).studyUnitId())
                .isEqualTo(generateDeterministicUuid("http://id.insee.fr/operations/operation/op2"));
    }
}
