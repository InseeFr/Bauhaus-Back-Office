package fr.insee.rmes.persistance.sparql_queries.structures;

import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.ANY_PARAMS;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertQueryBuiltFromTemplate;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.callWithStubbedTemplate;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StructureQueriesTest {

    private static final String STRUCTURES_FOLDER = "structures/";
    private static final String COMMON_FOLDER = "common/";
    private static final String MUTUALIZED_COMPONENTS_TEMPLATE = "getMutualizedComponents.ftlh";
    private static final String COMPONENT_QUERY = "SELECT ?component WHERE { ?component ?p ?o }";
    private static final String CONTRIBUTORS_QUERY = "SELECT ?contributor WHERE { ?s dc:contributor ?contributor }";

    private StructureQueries structureQueries;

    @BeforeEach
    void setUp() {
        structureQueries =
                new StructureQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    }

    @Test
    void shouldGetStructures() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getStructures.ftlh",
                "SELECT * WHERE { ?s ?p ?o }",
                structureQueries::getStructures,
                ANY_PARAMS);
    }

    @Test
    void shouldGetValidationStatus() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getValidationStatus.ftlh",
                "SELECT ?status WHERE { ?s ?p ?status }",
                () -> structureQueries.getValidationStatus("123"),
                map -> "\"123\"".equals(map.get("id")));
    }

    @Test
    void shouldGetStructuresAttachments() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getAttachment.ftlh",
                "SELECT ?attachment WHERE { ?s ?p ?attachment }",
                () -> structureQueries.getStructuresAttachments("struct123", "comp456"),
                map -> "\"struct123\"".equals(map.get("STRUCTURE_ID"))
                        && "\"comp456\"".equals(map.get("COMPONENT_SPECIFICATION_ID")));
    }

    @Test
    void shouldGetComponentsForStructure() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getComponentsForAStructure.ftlh",
                "SELECT ?component WHERE { ?s ?p ?component }",
                () -> structureQueries.getComponentsForStructure("123"));
    }

    @Test
    void shouldGetStructureById() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getStructure.ftlh",
                "SELECT ?structure WHERE { ?s ?p ?structure }",
                () -> structureQueries.getStructureById("123"));
    }

    @Test
    void shouldCheckUnicityMutualizedComponent() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "checkUnicityMutualizedComponent.ftlh",
                "ASK { ?s ?p ?o }",
                () -> structureQueries.checkUnicityMutualizedComponent(
                        "comp123",
                        "concept456",
                        "http://bauhaus/codes/nomenclature",
                        "http://purl.org/linked-data/cube#DimensionProperty"),
                map -> "\"comp123\"".equals(map.get("COMPONENT_ID"))
                        && ("<" + INSEE.STRUCTURE_CONCEPT + "concept456>").equals(map.get("CONCEPT_URI"))
                        && "<http://bauhaus/codes/nomenclature>".equals(map.get("CODE_LIST_URI"))
                        && "<http://purl.org/linked-data/cube#DimensionProperty>".equals(map.get("TYPE")));
    }

    @Test
    void shouldCheckUnicityStructure() throws RmesException {
        String[] ids = {"comp1", "comp2", "comp3"};
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "checkUnicityStructure.ftlh",
                "ASK { ?s ?p ?o }",
                () -> structureQueries.checkUnicityStructure("struct123", ids),
                map -> Integer.valueOf(3).equals(map.get("NB_COMPONENT"))
                        && "\"struct123\"".equals(map.get("STRUCTURE_ID"))
                        && List.of("\"comp1\"", "\"comp2\"", "\"comp3\"").equals(map.get("IDS")));
    }

    @Test
    void shouldGetComponentsWithAllTypesTrue() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                MUTUALIZED_COMPONENTS_TEMPLATE,
                COMPONENT_QUERY,
                () -> structureQueries.getComponents(true, true, true),
                map -> {
                    String types = (String) map.get("TYPES");
                    return types.contains("qb:AttributeProperty")
                            && types.contains("qb:DimensionProperty")
                            && types.contains("qb:MeasureProperty");
                });
    }

    @Test
    void shouldGetComponentsWithOnlyAttributes() throws RmesException {
        String result = callWithStubbedTemplate(
                STRUCTURES_FOLDER,
                MUTUALIZED_COMPONENTS_TEMPLATE,
                COMPONENT_QUERY,
                () -> structureQueries.getComponents(true, false, false),
                map -> "qb:AttributeProperty".equals(map.get("TYPES")));

        assertNotNull(result);
    }

    @Test
    void shouldGetComponent() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getMutualizedComponent.ftlh",
                COMPONENT_QUERY,
                () -> structureQueries.getComponent("123"));
    }

    @Test
    void shouldGetStructuresForComponent() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getStructuresForMutualizedComponent.ftlh",
                "SELECT ?structure WHERE { ?structure ?p ?o }",
                () -> structureQueries.getStructuresForComponent("123"));
    }

    @Test
    void shouldGetComponentType() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getComponentType.ftlh",
                "SELECT ?type WHERE { ?component rdf:type ?type }",
                () -> structureQueries.getComponentType("123"));
    }

    @Test
    void shouldGetLastId() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getLastIdByType.ftlh",
                "SELECT ?lastId WHERE { ?s ?p ?lastId }",
                () -> structureQueries.lastId("d", "http://purl.org/linked-data/cube#DimensionProperty"),
                map -> "\"^d\"".equals(map.get("ID_PREFIX_PATTERN"))
                        && "<http://purl.org/linked-data/cube#DimensionProperty>".equals(map.get("TYPE")));
    }

    @Test
    void shouldGetLastStructureId() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getLastIdStructure.ftlh",
                "SELECT ?lastId WHERE { ?s ?p ?lastId }",
                structureQueries::lastStructureId);
    }

    @Test
    void shouldGetUnValidatedComponent() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getUnValidatedComponent.ftlh",
                COMPONENT_QUERY,
                () -> structureQueries.getUnValidatedComponent("123"));
    }

    @Test
    void shouldGetUriClasseOwl() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getUriClasseOwl.ftlh",
                "SELECT ?uri WHERE { ?uri rdf:type owl:Class }",
                () -> structureQueries.getUriClasseOwl("http://bauhaus/codes/nomenclature"),
                map -> "<http://bauhaus/codes/nomenclature>".equals(map.get("CODES_LIST")));
    }

    @Test
    void shouldGetContributorsByStructureUri() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getStructureContributorsByUriQuery.ftlh",
                CONTRIBUTORS_QUERY,
                () -> structureQueries.getContributorsByStructureUri("http://example.org/structure/123"),
                map -> "<http://example.org/structure/123>".equals(map.get("URI_STRUCTURE")));
    }

    @Test
    void shouldGetContributorsByComponentUri() throws RmesException {
        assertQueryBuiltFromTemplate(
                STRUCTURES_FOLDER,
                "getComponentContributorsByUriQuery.ftlh",
                CONTRIBUTORS_QUERY,
                () -> structureQueries.getContributorsByComponentUri("http://example.org/component/123"),
                map -> "<http://example.org/component/123>".equals(map.get("URI_COMPONENT")));
    }

    @Test
    void shouldGetStructureContributors() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://example.org/structure/123");
        assertQueryBuiltFromTemplate(
                COMMON_FOLDER,
                "getContributors.ftlh",
                CONTRIBUTORS_QUERY,
                () -> structureQueries.getStructureContributors(iri),
                map -> ("<" + iri + ">").equals(map.get("IRI")) && "dc:contributor".equals(map.get("PREDICATE")));
    }

    @Test
    void shouldGetComponentContributors() throws RmesException {
        assertQueryBuiltFromTemplate(
                COMMON_FOLDER,
                "getContributors.ftlh",
                CONTRIBUTORS_QUERY,
                () -> structureQueries.getComponentContributors("http://example.org/component/123"),
                map -> "<http://example.org/component/123>".equals(map.get("IRI"))
                        && "dc:contributor".equals(map.get("PREDICATE")));
    }
}
