package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
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
        return new Ddi4Response(null, null, List.of(pi), null, null, null, null, null);
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
    void shouldCreateOneCodeListSchemeAndLogicalProductPerGroupVariant() throws Exception {
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

        // Each CodeListScheme files the variant's sentinel-values code list (example data, cf. #1566)
        assertThat(schemeCaptor.getAllValues()).allSatisfy(scheme -> {
            assertThat(scheme.agency()).isEqualTo("fr.insee");
            assertThat(scheme.codeListReference()).hasSize(1);
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
    void shouldCreateOneCategorySchemePerGroupVariantFiledInTheGroupLogicalProduct() throws Exception {
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

        // Each CategoryScheme files the two sentinel categories (example data, cf. #1566)
        assertThat(categoryCaptor.getAllValues()).allSatisfy(scheme -> {
            assertThat(scheme.agency()).isEqualTo("fr.insee");
            assertThat(scheme.categoryReference()).hasSize(2);
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
    void shouldCreateOneManagedRepresentationSchemePerGroupVariantFiledInTheGroupLogicalProduct() throws Exception {
        // Given: 1 series, no operations (keeps the test focused on the Group -> LogicalProduct ->
        // ManagedRepresentationScheme chain; without operations no study-unit LogicalProduct is
        // created, so every captured LogicalProduct is a group LogicalProduct)
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

        // Then: one ManagedRepresentationScheme per group variant, each filing the variant's example
        // ManagedMissingValuesRepresentation (cf. #1566) and filed in the group LogicalProduct
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        ArgumentCaptor<Ddi4ManagedRepresentationScheme> mrsCaptor = ArgumentCaptor.forClass(Ddi4ManagedRepresentationScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        verify(ddiService, times(variants)).createManagedRepresentationScheme(mrsCaptor.capture());
        verify(ddiService, times(variants)).createLogicalProduct(lpCaptor.capture());

        assertThat(mrsCaptor.getAllValues()).allSatisfy(scheme -> {
            assertThat(scheme.agency()).isEqualTo("fr.insee");
            assertThat(scheme.managedRepresentationReference()).hasSize(1);
        });

        // The i-th group LogicalProduct references the i-th ManagedRepresentationScheme, alongside
        // its CodeListScheme and CategoryScheme
        List<Ddi4ManagedRepresentationScheme> managedRepresentationSchemes = mrsCaptor.getAllValues();
        List<Ddi4LogicalProduct> logicalProducts = lpCaptor.getAllValues();
        for (int variant = 0; variant < variants; variant++) {
            Ddi4LogicalProduct lp = logicalProducts.get(variant);
            assertThat(lp.codeListSchemeReference()).hasSize(1);
            assertThat(lp.categorySchemeReference()).hasSize(1);
            assertThat(lp.managedRepresentationSchemeReference()).hasSize(1);
            assertThat(lp.managedRepresentationSchemeReference().get(0).id())
                    .isEqualTo(managedRepresentationSchemes.get(variant).id());
            assertThat(lp.managedRepresentationSchemeReference().get(0).type())
                    .isEqualTo("ManagedRepresentationScheme");
        }

        // Per variant, the ManagedRepresentationScheme is created before the group LogicalProduct
        // that files it
        InOrder inOrder = inOrder(ddiService);
        inOrder.verify(ddiService).createManagedRepresentationScheme(any());
        inOrder.verify(ddiService).createLogicalProduct(any());
    }

    @Test
    void shouldCreateSentinelValuesExamplePerGroupVariantFiledInTheGroupSchemes() throws Exception {
        // Given: 1 series, no operations. Chaque variante de groupe doit recevoir un exemple de
        // valeurs sentinelles (cf. #1566) : 2 catégories, la CodeList qui porte les codes NSP/REF,
        // et la ManagedMissingValuesRepresentation qui référence la CodeList — chacun classé dans
        // le scheme du groupe correspondant (CategoryScheme / CodeListScheme / MRS).
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

        // Then
        int variants = LocalColecticaGroupInitConfiguration.VARIANT_LABEL_WORDS.size();
        ArgumentCaptor<Ddi4Category> categoryCaptor = ArgumentCaptor.forClass(Ddi4Category.class);
        ArgumentCaptor<Ddi4CodeList> codeListCaptor = ArgumentCaptor.forClass(Ddi4CodeList.class);
        ArgumentCaptor<Ddi4ManagedMissingValuesRepresentation> mmvrCaptor =
                ArgumentCaptor.forClass(Ddi4ManagedMissingValuesRepresentation.class);
        ArgumentCaptor<Ddi4CodeListScheme> clsCaptor = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        ArgumentCaptor<Ddi4CategoryScheme> catsCaptor = ArgumentCaptor.forClass(Ddi4CategoryScheme.class);
        ArgumentCaptor<Ddi4ManagedRepresentationScheme> mrsCaptor =
                ArgumentCaptor.forClass(Ddi4ManagedRepresentationScheme.class);
        verify(ddiService, times(2 * variants)).createCategory(categoryCaptor.capture());
        verify(ddiService, times(variants)).createCodeList(codeListCaptor.capture());
        verify(ddiService, times(variants)).createManagedMissingValuesRepresentation(mmvrCaptor.capture());
        verify(ddiService, times(variants)).createCodeListScheme(clsCaptor.capture());
        verify(ddiService, times(variants)).createCategoryScheme(catsCaptor.capture());
        verify(ddiService, times(variants)).createManagedRepresentationScheme(mrsCaptor.capture());

        for (int variant = 0; variant < variants; variant++) {
            List<Ddi4Category> categories = categoryCaptor.getAllValues().subList(2 * variant, 2 * variant + 2);
            Ddi4CodeList codeList = codeListCaptor.getAllValues().get(variant);
            Ddi4ManagedMissingValuesRepresentation mmvr = mmvrCaptor.getAllValues().get(variant);

            // La CodeList sentinelle porte 2 codes (NSP, REF) pointant vers les 2 catégories
            assertThat(codeList.code()).hasSize(2);
            assertThat(codeList.code())
                    .extracting(code -> code.value().stringValue())
                    .containsExactly("NSP", "REF");
            assertThat(codeList.code())
                    .extracting(code -> code.categoryReference().id())
                    .containsExactly(categories.get(0).id(), categories.get(1).id());

            // La MMVR référence la CodeList sentinelle via son MissingCodeRepresentation, avec un label
            assertThat(mmvr.label()).isNotEmpty();
            assertThat(mmvr.missingCodeRepresentation()).hasSize(1);
            assertThat(mmvr.missingCodeRepresentation().get(0).codeListReference().id())
                    .isEqualTo(codeList.id());

            // Classements : CodeList dans le CodeListScheme, catégories dans le CategoryScheme,
            // MMVR dans le ManagedRepresentationScheme
            assertThat(clsCaptor.getAllValues().get(variant).codeListReference())
                    .extracting(Reference::id).containsExactly(codeList.id());
            assertThat(catsCaptor.getAllValues().get(variant).categoryReference())
                    .extracting(Reference::id)
                    .containsExactly(categories.get(0).id(), categories.get(1).id());
            assertThat(mrsCaptor.getAllValues().get(variant).managedRepresentationReference())
                    .extracting(Reference::id).containsExactly(mmvr.id());
            assertThat(mrsCaptor.getAllValues().get(variant).managedRepresentationReference().get(0).type())
                    .isEqualTo("ManagedMissingValuesRepresentation");
        }

        // Les enfants sont créés avant les schemes qui les référencent (pas de stubs Colectica).
        // Les deux catégories d'une variante sont créées consécutivement, d'où le times(2)
        // (la vérification InOrder de Mockito consomme le bloc consécutif entier).
        InOrder inOrder = inOrder(ddiService);
        inOrder.verify(ddiService, times(2)).createCategory(any());
        inOrder.verify(ddiService).createCodeList(any());
        inOrder.verify(ddiService).createManagedMissingValuesRepresentation(any());
        inOrder.verify(ddiService).createCodeListScheme(any());
        inOrder.verify(ddiService).createManagedRepresentationScheme(any());
    }

    @Test
    void shouldCreateOneVariableSchemeAndLogicalProductPerStudyUnit() throws Exception {
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

        // Une VariableScheme par StudyUnit (donc par variante), créée vide
        ArgumentCaptor<Ddi4VariableScheme> vsCaptor = ArgumentCaptor.forClass(Ddi4VariableScheme.class);
        verify(ddiService, times(variants)).createVariableScheme(vsCaptor.capture());
        List<Ddi4VariableScheme> variableSchemes = vsCaptor.getAllValues();
        assertThat(variableSchemes).allSatisfy(vs -> {
            assertThat(vs.agency()).isEqualTo("fr.insee");
            assertThat(vs.variableReference()).isNullOrEmpty();
        });
        assertThat(variableSchemes).extracting(Ddi4VariableScheme::id).doesNotHaveDuplicates();

        // Un LogicalProduct de study unit par StudyUnit (repérable à sa variableSchemeReference),
        // classant la VariableScheme de cette même StudyUnit
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        verify(ddiService, atLeastOnce()).createLogicalProduct(lpCaptor.capture());
        List<Ddi4LogicalProduct> studyUnitLps = lpCaptor.getAllValues().stream()
                .filter(lp -> lp.variableSchemeReference() != null && !lp.variableSchemeReference().isEmpty())
                .toList();
        assertThat(studyUnitLps).hasSize(variants);
        assertThat(studyUnitLps).extracting(Ddi4LogicalProduct::id).doesNotHaveDuplicates();
        for (int variant = 0; variant < variants; variant++) {
            Ddi4LogicalProduct studyUnitLp = studyUnitLps.get(variant);
            assertThat(studyUnitLp.variableSchemeReference()).hasSize(1);
            assertThat(studyUnitLp.variableSchemeReference().get(0).id())
                    .isEqualTo(variableSchemes.get(variant).id());
            assertThat(studyUnitLp.variableSchemeReference().get(0).type()).isEqualTo("VariableScheme");
            assertThat(studyUnitLp.codeListSchemeReference()).isNullOrEmpty();
        }

        // Chaque StudyUnit ne classe QUE son propre LogicalProduct : aucun LogicalProduct partagé
        ArgumentCaptor<Ddi4StudyUnit> suCaptor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(studyUnitService, times(variants)).createOrUpdate(suCaptor.capture());
        List<Ddi4StudyUnit> studyUnits = suCaptor.getAllValues();
        for (int variant = 0; variant < variants; variant++) {
            Ddi4StudyUnit studyUnit = studyUnits.get(variant);
            assertThat(studyUnit.logicalProductReferences()).hasSize(1);
            assertThat(studyUnit.logicalProductReferences().get(0).id())
                    .isEqualTo(studyUnitLps.get(variant).id());
            assertThat(studyUnit.logicalProductReferences().get(0).type()).isEqualTo("LogicalProduct");
        }

        // Ordre : la VariableScheme et son LogicalProduct existent avant la StudyUnit qui les référence
        InOrder inOrder = inOrder(ddiService, studyUnitService);
        inOrder.verify(ddiService).createVariableScheme(any());
        inOrder.verify(ddiService).createLogicalProduct(any());
        inOrder.verify(studyUnitService).createOrUpdate(any());
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

    @Test
    void versionedStudyUnitExample_createsAStudyUnitInTwoVersionsBothCarryingTheSameBasicPhysicalInstance() throws Exception {
        when(ddiService.createPhysicalInstance(any())).thenReturn(piResponse("fr.insee", "pi-two-versions"));

        LocalColecticaGroupInitConfiguration config = new LocalColecticaGroupInitConfiguration();
        CommandLineRunner runner = config.initColecticaVersionedStudyUnitExample(
                groupService, studyUnitService, ddiService, createColecticaConfig());

        runner.run();

        // Une seule PhysicalInstance, très basique (libellé seul).
        ArgumentCaptor<CreatePhysicalInstanceRequest> piCaptor =
                ArgumentCaptor.forClass(CreatePhysicalInstanceRequest.class);
        verify(ddiService).createPhysicalInstance(piCaptor.capture());
        assertThat(piCaptor.getValue().physicalInstanceLabel())
                .isEqualTo(LocalColecticaGroupInitConfiguration.VERSIONED_EXAMPLE_PHYSICAL_INSTANCE_LABEL);

        // La MÊME StudyUnit enregistrée en version 1 puis en version 2, les deux référençant la PI.
        ArgumentCaptor<Ddi4StudyUnit> suCaptor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(studyUnitService, times(2)).createOrUpdate(suCaptor.capture());
        List<Ddi4StudyUnit> versions = suCaptor.getAllValues();
        String expectedStudyUnitId =
                generateDeterministicUuid(LocalColecticaGroupInitConfiguration.VERSIONED_EXAMPLE_STUDY_UNIT_SEED);
        assertThat(versions).extracting(Ddi4StudyUnit::id).containsExactly(expectedStudyUnitId, expectedStudyUnitId);
        assertThat(versions).extracting(Ddi4StudyUnit::version).containsExactly("1", "2");
        assertThat(versions).allSatisfy(su -> {
            assertThat(su.urn()).isEqualTo("urn:ddi:fr.insee:%s:%s".formatted(su.id(), su.version()));
            assertThat(su.physicalInstanceReferences())
                    .extracting(Reference::id)
                    .containsExactly("pi-two-versions");
        });

        // Un groupe dédié, pour que la SU soit atteinte par la descente Group -> StudyUnit -> PI.
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        verify(groupService).createOrUpdate(groupCaptor.capture());
        assertThat(groupCaptor.getValue().studyUnitReference())
                .extracting(Reference::id)
                .containsExactly(expectedStudyUnitId);
    }
}
