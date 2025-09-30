package fr.insee.rmes.persistance.sparql_queries.structures;

import fr.insee.rmes.Config;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class StructureQueriesTest {

    @BeforeEach
    void setUp() {
        Config config = new ConfigStub();
        StructureQueries.setConfig(config);
    }

    @Test
    void shouldGetStructures() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getStructures.ftlh"), any(Map.class)))
                    .thenReturn("SELECT * WHERE { ?s ?p ?o }");

            String result = StructureQueries.getStructures();

            assertNotNull(result);
            assertEquals("SELECT * WHERE { ?s ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getStructures.ftlh"), any(Map.class)));
        }
    }

    @Test
    void shouldGetValidationStatus() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getValidationStatus.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?status WHERE { ?s ?p ?status }");

            String result = StructureQueries.getValidationStatus("123");

            assertNotNull(result);
            assertEquals("SELECT ?status WHERE { ?s ?p ?status }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getValidationStatus.ftlh"), 
                    argThat(params -> "123".equals(((Map<String, Object>) params).get("id")))));
        }
    }

    @Test
    void shouldGetStructuresAttachments() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getAttachment.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?attachment WHERE { ?s ?p ?attachment }");

            String result = StructureQueries.getStructuresAttachments("struct123", "comp456");

            assertNotNull(result);
            assertEquals("SELECT ?attachment WHERE { ?s ?p ?attachment }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getAttachment.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "struct123".equals(map.get("STRUCTURE_ID")) && "comp456".equals(map.get("COMPONENT_SPECIFICATION_ID"));
                    })));
        }
    }

    @Test
    void shouldGetComponentsForStructure() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getComponentsForAStructure.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?component WHERE { ?s ?p ?component }");

            String result = StructureQueries.getComponentsForStructure("123");

            assertNotNull(result);
            assertEquals("SELECT ?component WHERE { ?s ?p ?component }", result);
        }
    }

    @Test
    void shouldGetStructureById() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getStructure.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?structure WHERE { ?s ?p ?structure }");

            String result = StructureQueries.getStructureById("123");

            assertNotNull(result);
            assertEquals("SELECT ?structure WHERE { ?s ?p ?structure }", result);
        }
    }

    @Test
    void shouldCheckUnicityMutualizedComponent() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("checkUnicityMutualizedComponent.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s ?p ?o }");

            String result = StructureQueries.checkUnicityMutualizedComponent("comp123", "concept456", "codeList789", "DIMENSION");

            assertNotNull(result);
            assertEquals("ASK { ?s ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("checkUnicityMutualizedComponent.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "comp123".equals(map.get("COMPONENT_ID")) && 
                               (INSEE.STRUCTURE_CONCEPT + "concept456").equals(map.get("CONCEPT_URI")) &&
                               "codeList789".equals(map.get("CODE_LIST_URI")) &&
                               "DIMENSION".equals(map.get("TYPE"));
                    })));
        }
    }

    @Test
    void shouldCheckUnicityStructure() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("checkUnicityStructure.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s ?p ?o }");

            String[] ids = {"comp1", "comp2", "comp3"};
            String result = StructureQueries.checkUnicityStructure("struct123", ids);

            assertNotNull(result);
            assertEquals("ASK { ?s ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("checkUnicityStructure.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return Integer.valueOf(3).equals(map.get("NB_COMPONENT")) &&
                               "struct123".equals(map.get("STRUCTURE_ID")) &&
                               ids == map.get("IDS");
                    })));
        }
    }

    @Test
    void shouldGetComponentsWithAllTypesTrue() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getMutualizedComponents.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?component WHERE { ?component ?p ?o }");

            String result = StructureQueries.getComponents(true, true, true);

            assertNotNull(result);
            assertEquals("SELECT ?component WHERE { ?component ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getMutualizedComponents.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        String types = (String) map.get("TYPES");
                        return types.contains("qb:AttributeProperty") && 
                               types.contains("qb:DimensionProperty") && 
                               types.contains("qb:MeasureProperty");
                    })));
        }
    }

    @Test
    void shouldGetComponentsWithOnlyAttributes() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getMutualizedComponents.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?component WHERE { ?component ?p ?o }");

            String result = StructureQueries.getComponents(true, false, false);

            assertNotNull(result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getMutualizedComponents.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        String types = (String) map.get("TYPES");
                        return "qb:AttributeProperty".equals(types);
                    })));
        }
    }

    @Test
    void shouldGetComponent() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getMutualizedComponent.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?component WHERE { ?component ?p ?o }");

            String result = StructureQueries.getComponent("123");

            assertNotNull(result);
            assertEquals("SELECT ?component WHERE { ?component ?p ?o }", result);
        }
    }

    @Test
    void shouldGetStructuresForComponent() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getStructuresForMutualizedComponent.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?structure WHERE { ?structure ?p ?o }");

            String result = StructureQueries.getStructuresForComponent("123");

            assertNotNull(result);
            assertEquals("SELECT ?structure WHERE { ?structure ?p ?o }", result);
        }
    }

    @Test
    void shouldGetComponentType() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getComponentType.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?type WHERE { ?component rdf:type ?type }");

            String result = StructureQueries.getComponentType("123");

            assertNotNull(result);
            assertEquals("SELECT ?type WHERE { ?component rdf:type ?type }", result);
        }
    }

    @Test
    void shouldGetLastId() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getLastIdByType.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?lastId WHERE { ?s ?p ?lastId }");

            String result = StructureQueries.lastId("namespace", "DIMENSION");

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?s ?p ?lastId }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getLastIdByType.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "namespace".equals(map.get("NAMESPACE")) && "DIMENSION".equals(map.get("TYPE"));
                    })));
        }
    }

    @Test
    void shouldGetLastStructureId() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getLastIdStructure.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?lastId WHERE { ?s ?p ?lastId }");

            String result = StructureQueries.lastStructureId();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?s ?p ?lastId }", result);
        }
    }

    @Test
    void shouldGetUnValidatedComponent() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getUnValidatedComponent.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?component WHERE { ?component ?p ?o }");

            String result = StructureQueries.getUnValidatedComponent("123");

            assertNotNull(result);
            assertEquals("SELECT ?component WHERE { ?component ?p ?o }", result);
        }
    }

    @Test
    void shouldGetUriClasseOwl() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getUriClasseOwl.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?uri WHERE { ?uri rdf:type owl:Class }");

            String result = StructureQueries.getUriClasseOwl("codeList123");

            assertNotNull(result);
            assertEquals("SELECT ?uri WHERE { ?uri rdf:type owl:Class }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getUriClasseOwl.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "codeList123".equals(map.get("CODES_LIST"));
                    })));
        }
    }

    @Test
    void shouldGetContributorsByStructureUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getStructureContributorsByUriQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }");

            String result = StructureQueries.getContributorsByStructureUri("http://example.org/structure/123");

            assertNotNull(result);
            assertEquals("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getStructureContributorsByUriQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "http://example.org/structure/123".equals(map.get("URI_STRUCTURE"));
                    })));
        }
    }

    @Test
    void shouldGetContributorsByComponentUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getComponentContributorsByUriQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }");

            String result = StructureQueries.getContributorsByComponentUri("http://example.org/component/123");

            assertNotNull(result);
            assertEquals("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("structures/"), eq("getComponentContributorsByUriQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "http://example.org/component/123".equals(map.get("URI_COMPONENT"));
                    })));
        }
    }

    @Test
    void shouldGetStructureContributors() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("common/"), eq("getContributors.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }");

            IRI iri = SimpleValueFactory.getInstance().createIRI("http://example.org/structure/123");
            String result = StructureQueries.getStructureContributors(iri);

            assertNotNull(result);
            assertEquals("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("common/"), eq("getContributors.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return iri.equals(map.get("IRI")) && "dc:contributor".equals(map.get("PREDICATE"));
                    })));
        }
    }

    @Test
    void shouldGetComponentContributors() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("common/"), eq("getContributors.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }");

            String result = StructureQueries.getComponentContributors("http://example.org/component/123");

            assertNotNull(result);
            assertEquals("SELECT ?contributor WHERE { ?s dc:contributor ?contributor }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("common/"), eq("getContributors.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "http://example.org/component/123".equals(map.get("IRI")) && "dc:contributor".equals(map.get("PREDICATE"));
                    })));
        }
    }
}