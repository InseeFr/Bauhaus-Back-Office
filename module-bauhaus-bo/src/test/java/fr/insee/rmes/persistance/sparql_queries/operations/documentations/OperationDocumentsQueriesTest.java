package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertQueryBuiltFromTemplate;
import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertRmesExceptionPropagated;
import static fr.insee.rmes.persistance.sparql_queries.SparqlQueryNormalizer.normalize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OperationDocumentsQueriesTest {

    private static final String DOCUMENTS_FOLDER = "operations/documentations/documents/";
    private static final String GET_DOCUMENT_TEMPLATE = "getDocumentQuery.ftlh";

    private OperationDocumentsQueries operationDocumentsQueries;

    @BeforeEach
    void setUp() {
        operationDocumentsQueries = new OperationDocumentsQueries(
                BauhausUriPropertiesStub.stub(),
                new BauhausLanguagesProperties("fr", "en"),
                GraphsPropertiesStub.stub());
    }

    @Test
    void shouldGetDocumentUri() throws RmesException {
        assertQueryBuiltFromTemplate(
                DOCUMENTS_FOLDER,
                "getDocumentUriFromUrlQuery.ftlh",
                "SELECT ?uri WHERE { ?uri foaf:page 'test.pdf' }",
                () -> operationDocumentsQueries.getDocumentUriQuery("Test.PDF"),
                map -> "\"test.pdf\"".equals(map.get(Constants.URL)));
    }

    @Test
    void shouldGetDocumentsForSimsRubric() throws RmesException {
        assertQueryBuiltFromTemplate(
                DOCUMENTS_FOLDER,
                GET_DOCUMENT_TEMPLATE,
                "SELECT ?document WHERE { ?document ?p ?o }",
                () -> operationDocumentsQueries.getDocumentsForSimsRubricQuery(
                        "sims123", "rubric456", "http://bauhaus/codes/langue/fr"),
                map -> map.get(Constants.ID) == null
                        && "\"sims123\"".equals(map.get(Constants.ID_SIMS))
                        && "\"rubric456\"".equals(map.get("idRubric"))
                        && "<http://bauhaus/codes/langue/fr>".equals(map.get("LANG")));
    }

    @Test
    void shouldGetDocumentsForSims() throws RmesException {
        String expectedType = BauhausUriPropertiesStub.stub().documentsBaseUri();
        assertQueryBuiltFromTemplate(
                DOCUMENTS_FOLDER,
                GET_DOCUMENT_TEMPLATE,
                "SELECT ?document WHERE { ?document ?p ?o }",
                () -> operationDocumentsQueries.getDocumentsForSimsQuery("sims123"),
                map -> isSims123QueryOfType(map, expectedType));
    }

    @Test
    void shouldGetLinksForSims() throws RmesException {
        String expectedType = BauhausUriPropertiesStub.stub().linksBaseUri();
        assertQueryBuiltFromTemplate(
                DOCUMENTS_FOLDER,
                GET_DOCUMENT_TEMPLATE,
                "SELECT ?link WHERE { ?link ?p ?o }",
                () -> operationDocumentsQueries.getLinksForSimsQuery("sims123"),
                map -> isSims123QueryOfType(map, expectedType));
    }

    /** Paramètres d'une requête sur tous les documents (ou liens) du SIMS sims123, sans rubrique. */
    private static boolean isSims123QueryOfType(Map<String, Object> map, String expectedType) {
        return map.get(Constants.ID) == null
                && "\"sims123\"".equals(map.get(Constants.ID_SIMS))
                && map.get("idRubric") == null
                && ("\"" + expectedType + "\"").equals(map.get("type"));
    }

    @Test
    void shouldGetLastDocumentID() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker
                    .when(() -> FreeMarkerUtils.buildRequest(
                            eq(DOCUMENTS_FOLDER), eq("lastDocumentIdQuery.ftlh"), isNull()))
                    .thenReturn("SELECT ?lastId WHERE { ?doc dcterms:identifier ?lastId }");

            String result = operationDocumentsQueries.lastDocumentID();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?doc dcterms:identifier ?lastId }", result);
            mockedFreeMarker.verify(
                    () -> FreeMarkerUtils.buildRequest(eq(DOCUMENTS_FOLDER), eq("lastDocumentIdQuery.ftlh"), isNull()));
        }
    }

    @Test
    void shouldGetLastLinkID() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker
                    .when(() ->
                            FreeMarkerUtils.buildRequest(eq(DOCUMENTS_FOLDER), eq("lastLinkIdQuery.ftlh"), isNull()))
                    .thenReturn("SELECT ?lastId WHERE { ?link dcterms:identifier ?lastId }");

            String result = operationDocumentsQueries.lastLinkID();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?link dcterms:identifier ?lastId }", result);
            mockedFreeMarker.verify(
                    () -> FreeMarkerUtils.buildRequest(eq(DOCUMENTS_FOLDER), eq("lastLinkIdQuery.ftlh"), isNull()));
        }
    }

    @Test
    void shouldGetDocumentsUriAndUrlForSims() throws RmesException {
        assertQueryBuiltFromTemplate(
                DOCUMENTS_FOLDER,
                "getDocumentsUriAndUrlForSims.ftlh",
                "SELECT ?uri ?url WHERE { ?uri foaf:page ?url }",
                () -> operationDocumentsQueries.getDocumentsUriAndUrlForSims("sims123"),
                map -> ("<" + GraphsPropertiesStub.stub().documentationsGraph() + "/sims123>")
                        .equals(map.get("DOCUMENTATION_GRAPH_IRI")));
    }

    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        assertRmesExceptionPropagated(
                DOCUMENTS_FOLDER,
                GET_DOCUMENT_TEMPLATE,
                () -> operationDocumentsQueries.getDocumentsForSimsQuery("test"));
    }

    @Test
    void getDocumentPredicatesAndObjects_returnsExpectedSparql() throws RmesException {
        IRI document = SimpleValueFactory.getInstance().createIRI("http://bauhaus/documents/document/1000");

        String sparql = normalize(operationDocumentsQueries.getDocumentPredicatesAndObjects(document));

        assertEquals(normalize("""
                select ?predicat ?obj FROM <http://rdf.insee.fr/graphes/qualite/documents>
                WHERE {?document ?predicat ?obj .
                FILTER (?document = <http://bauhaus/documents/document/1000>)
                }
                """), sparql);
    }
}
