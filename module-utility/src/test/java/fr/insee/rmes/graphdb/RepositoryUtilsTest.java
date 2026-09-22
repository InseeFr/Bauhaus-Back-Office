package fr.insee.rmes.graphdb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.keycloak.TokenService;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

class RepositoryUtilsTest {

    @Mock
    private TokenService tokenService;

    private RepositoryUtils repositoryUtils;
    private Repository testRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repositoryUtils = new RepositoryUtils(tokenService, RepositoryInitiator.Type.DISABLED);
        testRepository = new SailRepository(new MemoryStore());
        testRepository.init();
    }

    @Test
    void shouldInitRepositoryWithValidParameters() {
        String rdfServer = "http://localhost:8080/rdf";
        String repositoryID = "test-repo";

        Repository result = repositoryUtils.initRepository(rdfServer, repositoryID);
        assertNotNull(result);
    }

    @Test
    void shouldReturnNullForNullRdfServer() {
        Repository result = repositoryUtils.initRepository(null, "test-repo");
        assertNull(result);
    }

    @Test
    void shouldReturnNullForEmptyRdfServer() {
        Repository result = repositoryUtils.initRepository("", "test-repo");
        assertNull(result);
    }

    @Test
    void shouldGetConnectionFromRepository() throws RmesException {
        RepositoryConnection connection = repositoryUtils.getConnection(testRepository);
        assertNotNull(connection);
        connection.close();
    }

    /* `initRepository` rend `null` quand la configuration RDF est incomplète : la connexion
    doit alors échouer explicitement, pas sur un NullPointerException. */
    @Test
    void shouldThrowRmesExceptionWhenTheRepositoryIsNull() {
        assertThrows(RmesException.class, () -> repositoryUtils.getConnection(null));
    }

    @Test
    void shouldThrowRmesExceptionWhenRepositoryConnectionFails() {
        Repository mockRepo = mock(Repository.class);
        when(mockRepo.getConnection()).thenThrow(new RepositoryException("Connection failed"));
        when(mockRepo.getDataDir()).thenReturn(null);

        assertThrows(RmesException.class, () -> {
            repositoryUtils.getConnection(mockRepo);
        });
    }

    @Test
    void shouldExecuteUpdateSuccessfully() throws RmesException {
        String updateQuery = "INSERT DATA { <http://example.org/subject> <http://example.org/predicate> \"object\" . }";

        HttpStatus result = repositoryUtils.executeUpdate(updateQuery, testRepository);
        assertEquals(HttpStatus.OK, result);
    }

    @Test
    void shouldReturnExpectationFailedForNullRepository() throws RmesException {
        String updateQuery = "INSERT DATA { <http://example.org/subject> <http://example.org/predicate> \"object\" . }";

        HttpStatus result = repositoryUtils.executeUpdate(updateQuery, null);
        assertEquals(HttpStatus.EXPECTATION_FAILED, result);
    }

    /* Le corps d'une 500 part au client : il ne doit porter ni la requête SPARQL ni le
    message RDF4J, qui restent dans les logs. */
    @Test
    void shouldNotExposeTheUpdateQueryNorTheRdf4jMessageWhenTheUpdateFails() {
        String updateQuery = "INSERT DATA { GRAPH <http://rdf.insee.fr/graphes/private> { <urn:s> <urn:p> 'o' } }";
        Repository failingRepository = repositoryFailingOnPrepareUpdate("Transaction rolled back by GraphDB node 3");

        RmesException exception =
                assertThrows(RmesException.class, () -> repositoryUtils.executeUpdate(updateQuery, failingRepository));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getStatus());
        assertFalse(exception.getDetails().contains("http://rdf.insee.fr/graphes/private"));
        assertFalse(exception.getDetails().contains("GraphDB node 3"));
    }

    @Test
    void shouldLogTheUpdateQueryWhenTheUpdateFails() {
        String updateQuery = "INSERT DATA { GRAPH <http://rdf.insee.fr/graphes/private> { <urn:s> <urn:p> 'o' } }";
        Repository failingRepository = repositoryFailingOnPrepareUpdate("Transaction rolled back");
        Logger logger = (Logger) LoggerFactory.getLogger(RepositoryUtils.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(RmesException.class, () -> repositoryUtils.executeUpdate(updateQuery, failingRepository));

            assertTrue(appender.list.stream()
                    .anyMatch(event -> event.getLevel() == Level.ERROR
                            && event.getFormattedMessage().contains(updateQuery)));
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void shouldNotExposeTheRdf4jMessageWhenTheConnectionFails() {
        Repository mockRepo = mock(Repository.class);
        when(mockRepo.getConnection()).thenThrow(new RepositoryException("Connection refused: graphdb-internal:7200"));

        RmesException exception = assertThrows(RmesException.class, () -> repositoryUtils.getConnection(mockRepo));

        assertFalse(exception.getDetails().contains("graphdb-internal"));
    }

    @Test
    void shouldNotExposeTheRdf4jMessageWhenReadingAGraphFails() {
        RepositoryConnection connection = mock(RepositoryConnection.class);
        when(connection.getStatements(null, null, null, (Resource) null))
                .thenThrow(new RepositoryException("Connection refused: graphdb-internal:7200"));

        RmesException exception =
                assertThrows(RmesException.class, () -> repositoryUtils.getCompleteGraph(connection, null));

        assertFalse(exception.getDetails().contains("graphdb-internal"));
    }

    private static Repository repositoryFailingOnPrepareUpdate(String rdf4jMessage) {
        Repository repository = mock(Repository.class);
        RepositoryConnection connection = mock(RepositoryConnection.class);
        when(repository.getConnection()).thenReturn(connection);
        when(connection.prepareUpdate(eq(QueryLanguage.SPARQL), anyString()))
                .thenThrow(new RepositoryException(rdf4jMessage));
        return repository;
    }

    @Test
    void shouldGetCompleteGraph() throws RmesException {
        try (RepositoryConnection conn = testRepository.getConnection()) {
            Resource context = SimpleValueFactory.getInstance().createIRI("http://example.org/graph");

            RepositoryResult<org.eclipse.rdf4j.model.Statement> result =
                    repositoryUtils.getCompleteGraph(conn, context);

            assertNotNull(result);
            result.close();
        }
    }

    @Test
    void shouldExecuteQuery() throws RmesException {
        try (RepositoryConnection conn = testRepository.getConnection()) {
            String query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";

            String result = repositoryUtils.executeQuery(conn, query);
            assertNotNull(result);
            assertTrue(result.contains("results"));
        }
    }

    @Test
    void shouldPreserveUtf8AccentsInQueryResults() throws RmesException {
        // Inserts a literal containing accented French characters and verifies that
        // executeQuery returns them intact (no mojibake), regardless of the JVM's
        // default charset. SPARQLResultsJSONWriter writes UTF-8 by spec.
        try (RepositoryConnection conn = testRepository.getConnection()) {
            conn.add(
                    SimpleValueFactory.getInstance().createIRI("http://example.org/s1001"),
                    SimpleValueFactory.getInstance().createIRI("http://example.org/label"),
                    SimpleValueFactory.getInstance().createLiteral("Enquête capacité à innover et stratégie"));

            String query = "SELECT ?label WHERE { ?s <http://example.org/label> ?label }";
            String result = repositoryUtils.executeQuery(conn, query);

            assertNotNull(result);
            assertTrue(
                    result.contains("Enquête capacité à innover et stratégie"),
                    "Accented characters should survive the SPARQL JSON serialization round-trip; got: " + result);
        }
    }

    @Test
    void shouldExecuteAskQuery() throws RmesException {
        try (RepositoryConnection conn = testRepository.getConnection()) {
            String askQuery = "ASK { ?s ?p ?o }";

            boolean result = repositoryUtils.executeAskQuery(conn, askQuery);
            // Should return false for empty repository
            assertFalse(result);
        }
    }

    @Test
    void shouldGetResponse() throws RmesException {
        String query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";

        String response = repositoryUtils.getResponse(query, testRepository);
        assertNotNull(response);
        assertTrue(response.contains("results"));
    }

    @Test
    void shouldGetResponseForAskQuery() throws RmesException {
        String askQuery = "ASK { ?s ?p ?o }";

        boolean response = repositoryUtils.getResponseForAskQuery(askQuery, testRepository);
        assertFalse(response); // Empty repository
    }

    @Test
    void shouldGetResponseAsArray() throws RmesException {
        String query = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 10";

        JSONArray response = repositoryUtils.getResponseAsArray(query, testRepository);
        // Should return empty array for empty repository
        assertTrue(response == null || response.isEmpty());
    }

    @Test
    void shouldGetResponseAsJSONList() throws RmesException {
        String query = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 10";

        JSONArray response = repositoryUtils.getResponseAsJSONList(query, testRepository);
        // Should return empty array for empty repository
        assertTrue(response == null || response.isEmpty());
    }

    @Test
    void shouldGetResponseAsObject() throws RmesException {
        String query = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 1";

        JSONObject response = repositoryUtils.getResponseAsObject(query, testRepository);
        assertNotNull(response);
    }

    /** A SPARQL JSON result binding `?s` once per given subject. */
    private static JSONObject sparqlResultWithSubjects(String... subjects) {
        JSONArray bindings = new JSONArray();
        for (String subject : subjects) {
            bindings.put(new JSONObject().put("s", new JSONObject().put("value", subject)));
        }
        return new JSONObject().put("results", new JSONObject().put("bindings", bindings));
    }

    @Test
    void shouldConvertSparqlJSONToResultArrayValues() {
        JSONObject sparqlResult = sparqlResultWithSubjects("http://example.org/subject");

        JSONArray result = RepositoryUtils.sparqlJSONToResultArrayValues(sparqlResult);

        assertNotNull(result);
        assertEquals(1, result.length());
        assertEquals("http://example.org/subject", result.getJSONObject(0).getString("s"));
    }

    /* Le tableau vide plutôt que `null` : les appelants enchaînent sur le résultat
    (`.toString()`, `.length()`) sans le tester, une réponse sans `results` ne doit pas
    leur exploser à la figure. */
    @Test
    void shouldReturnAnEmptyArrayForEmptySparqlResults() {
        JSONObject sparqlResult = new JSONObject();

        JSONArray result = RepositoryUtils.sparqlJSONToResultArrayValues(sparqlResult);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldConvertSparqlJSONToResultListValues() {
        JSONObject sparqlResult = sparqlResultWithSubjects("http://example.org/subject");

        JSONArray result = RepositoryUtils.sparqlJSONToResultListValues(sparqlResult);

        assertNotNull(result);
        assertEquals(1, result.length());
        assertEquals("http://example.org/subject", result.getString(0));
    }

    @Test
    void shouldReturnAnEmptyArrayForEmptySparqlResultsList() {
        JSONObject sparqlResult = new JSONObject();

        JSONArray result = RepositoryUtils.sparqlJSONToResultListValues(sparqlResult);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldClearStructureAndComponents() throws RmesException {
        Resource structure = SimpleValueFactory.getInstance().createIRI("http://example.org/structure");

        assertDoesNotThrow(() -> {
            repositoryUtils.clearStructureAndComponents(structure, testRepository);
        });
    }

    @Test
    void shouldHandleMultipleBindingsInSparqlResult() {
        JSONObject sparqlResult =
                sparqlResultWithSubjects("http://example.org/subject1", "http://example.org/subject2");

        JSONArray result = RepositoryUtils.sparqlJSONToResultArrayValues(sparqlResult);

        assertNotNull(result);
        assertEquals(2, result.length());
        assertEquals("http://example.org/subject1", result.getJSONObject(0).getString("s"));
        assertEquals("http://example.org/subject2", result.getJSONObject(1).getString("s"));
    }
}
