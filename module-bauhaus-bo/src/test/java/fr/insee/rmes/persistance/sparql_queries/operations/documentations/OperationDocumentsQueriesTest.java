package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class OperationDocumentsQueriesTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = new ConfigStub();
        OperationDocumentsQueries.setConfig(config);
    }

    @Test
    void shouldCheckLabelUnicity() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), any(Map.class)))
                    .thenReturn("ASK { ?s foaf:name 'Test Document'@en }");

            String result = OperationDocumentsQueries.checkLabelUnicity("doc123", "Test Document", "en");

            assertNotNull(result);
            assertEquals("ASK { ?s foaf:name 'Test Document'@en }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/"), eq("checkFamilyPrefLabelUnicity.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "doc123".equals(map.get("ID")) &&
                               "Test Document".equals(map.get("LABEL")) &&
                               "en".equals(map.get("LANG")) &&
                               "".equals(map.get("URI_PREFIX")) &&
                               "foaf:Document".equals(map.get("TYPE")) &&
                               config.getDocumentsGraph().equals(map.get("OPERATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldDeleteDocument() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("deleteDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("DELETE WHERE { <http://example.org/doc/123> ?p ?o }");

            IRI uri = SimpleValueFactory.getInstance().createIRI("http://example.org/doc/123");
            String result = OperationDocumentsQueries.deleteDocumentQuery(uri);

            assertNotNull(result);
            assertEquals("DELETE WHERE { <http://example.org/doc/123> ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("deleteDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return uri.equals(map.get(Constants.URI));
                    })));
        }
    }

    @Test
    void shouldGetDocumentUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentUriFromUrlQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?uri WHERE { ?uri foaf:page 'test.pdf' }");

            String result = OperationDocumentsQueries.getDocumentUriQuery("Test.PDF");

            assertNotNull(result);
            assertEquals("SELECT ?uri WHERE { ?uri foaf:page 'test.pdf' }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentUriFromUrlQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "test.pdf".equals(map.get(Constants.URL));
                    })));
        }
    }

    @Test
    void shouldGetDocumentsForSimsRubric() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?document WHERE { ?document ?p ?o }");

            String result = OperationDocumentsQueries.getDocumentsForSimsRubricQuery("sims123", "rubric456", "fr");

            assertNotNull(result);
            assertEquals("SELECT ?document WHERE { ?document ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "".equals(map.get(Constants.ID)) &&
                               "sims123".equals(map.get(Constants.ID_SIMS)) &&
                               "rubric456".equals(map.get("idRubric")) &&
                               "fr".equals(map.get("LANG"));
                    })));
        }
    }

    @Test
    void shouldGetDocumentsForSims() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?document WHERE { ?document ?p ?o }");

            String result = OperationDocumentsQueries.getDocumentsForSimsQuery("sims123");

            assertNotNull(result);
            assertEquals("SELECT ?document WHERE { ?document ?p ?o }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        String expectedType = config.getDocumentsBaseUri();
                        return "".equals(map.get(Constants.ID)) &&
                               "sims123".equals(map.get(Constants.ID_SIMS)) &&
                               "".equals(map.get("idRubric")) &&
                               (expectedType != null ? expectedType.equals(map.get("type")) : map.get("type") == null);
                    })));
        }
    }

    @Test
    void shouldGetLinksForSims() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?link WHERE { ?link ?p ?o }");

            String result = OperationDocumentsQueries.getLinksForSimsQuery("sims123");

            assertNotNull(result);
            assertEquals("SELECT ?link WHERE { ?link ?p ?o }", result);

            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"),
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        String expectedType = config.getLinksBaseUri();
                        return "".equals(map.get(Constants.ID)) &&
                               "sims123".equals(map.get(Constants.ID_SIMS)) &&
                               "".equals(map.get("idRubric")) &&
                               (expectedType != null ? expectedType.equals(map.get("type")) : map.get("type") == null);
                    })));
        }
    }

    @Test
    void shouldGetDocumentForDocument() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?document WHERE { ?document dcterms:identifier 'doc123' }");

            String result = OperationDocumentsQueries.getDocumentQuery("doc123", false);

            assertNotNull(result);
            assertEquals("SELECT ?document WHERE { ?document dcterms:identifier 'doc123' }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        String expectedType = config.getDocumentsBaseUri();
                        return "doc123".equals(map.get(Constants.ID)) &&
                               (expectedType != null ? expectedType.equals(map.get("type")) : map.get("type") == null);
                    })));
        }
    }

    @Test
    void shouldGetDocumentForLink() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?link WHERE { ?link dcterms:identifier 'link123' }");

            String result = OperationDocumentsQueries.getDocumentQuery("link123", true);

            assertNotNull(result);
            assertEquals("SELECT ?link WHERE { ?link dcterms:identifier 'link123' }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        String expectedType = config.getLinksBaseUri();
                        return "link123".equals(map.get(Constants.ID)) &&
                               (expectedType != null ? expectedType.equals(map.get("type")) : map.get("type") == null);
                    })));
        }
    }


    @Test
    void shouldGetAllDocuments() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?document WHERE { ?document a foaf:Document }");

            String result = OperationDocumentsQueries.getAllDocumentsQuery();

            assertNotNull(result);
            assertEquals("SELECT ?document WHERE { ?document a foaf:Document }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "".equals(map.get(Constants.ID)) &&
                               "".equals(map.get(Constants.ID_SIMS)) &&
                               "".equals(map.get("idRubric")) &&
                               "".equals(map.get("type"));
                    })));
        }
    }

    @Test
    void shouldGetLinksToDocument() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getLinksToDocumentQuery.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?link WHERE { ?link ?p ?document }");

            String result = OperationDocumentsQueries.getLinksToDocumentQuery("doc123");

            assertNotNull(result);
            assertEquals("SELECT ?link WHERE { ?link ?p ?document }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getLinksToDocumentQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "doc123".equals(map.get(Constants.ID));
                    })));
        }
    }

    @Test
    void shouldChangeDocumentUrl() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("changeDocumentUrlQuery.ftlh"), any(Map.class)))
                    .thenReturn("DELETE/INSERT query");

            String result = OperationDocumentsQueries.changeDocumentUrlQuery("http://example.org/doc/123", "old.pdf", "new.pdf");

            assertNotNull(result);
            assertEquals("DELETE/INSERT query", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("changeDocumentUrlQuery.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "http://example.org/doc/123".equals(map.get("iri")) &&
                               "old.pdf".equals(map.get("oldUrl")) &&
                               "new.pdf".equals(map.get("newUrl"));
                    })));
        }
    }

    @Test
    void shouldGetLastDocumentID() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("lastDocumentIdQuery.ftlh"), isNull()))
                    .thenReturn("SELECT ?lastId WHERE { ?doc dcterms:identifier ?lastId }");

            String result = OperationDocumentsQueries.lastDocumentID();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?doc dcterms:identifier ?lastId }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("lastDocumentIdQuery.ftlh"), isNull()));
        }
    }

    @Test
    void shouldGetLastLinkID() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("lastLinkIdQuery.ftlh"), isNull()))
                    .thenReturn("SELECT ?lastId WHERE { ?link dcterms:identifier ?lastId }");

            String result = OperationDocumentsQueries.lastLinkID();

            assertNotNull(result);
            assertEquals("SELECT ?lastId WHERE { ?link dcterms:identifier ?lastId }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("lastLinkIdQuery.ftlh"), isNull()));
        }
    }

    @Test
    void shouldGetDocumentsUriAndUrlForSims() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentsUriAndUrlForSims.ftlh"), any(Map.class)))
                    .thenReturn("SELECT ?uri ?url WHERE { ?uri foaf:page ?url }");

            String result = OperationDocumentsQueries.getDocumentsUriAndUrlForSims("sims123");

            assertNotNull(result);
            assertEquals("SELECT ?uri ?url WHERE { ?uri foaf:page ?url }", result);
            mockedFreeMarker.verify(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentsUriAndUrlForSims.ftlh"), 
                    argThat(params -> {
                        Map<String, Object> map = (Map<String, Object>) params;
                        return "sims123".equals(map.get(Constants.ID)) &&
                               config.getDocumentationsGraph().equals(map.get("DOCUMENTATIONS_GRAPH"));
                    })));
        }
    }

    @Test
    void shouldPropagateRmesExceptionFromFreeMarkerUtils() {
        try (MockedStatic<FreeMarkerUtils> mockedFreeMarker = mockStatic(FreeMarkerUtils.class)) {
            RmesException testException = new RmesException(500, "Test error", "Test error message");
            mockedFreeMarker.when(() -> FreeMarkerUtils.buildRequest(eq("operations/documentations/documents/"), eq("getDocumentQuery.ftlh"), any(Map.class)))
                    .thenThrow(testException);

            RmesException exception = assertThrows(RmesException.class, () -> 
                OperationDocumentsQueries.getDocumentQuery("test", false)
            );

            assertEquals(testException, exception);
        }
    }
}