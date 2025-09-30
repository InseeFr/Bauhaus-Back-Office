package fr.insee.rmes.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.keycloak.KeycloakServices;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RepositoryUtilsTest {

    @Mock
    private KeycloakServices keycloakServices;

    private RepositoryUtils repositoryUtils;
    private Repository testRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repositoryUtils = new RepositoryUtils(keycloakServices, RepositoryInitiator.Type.DISABLED);
        testRepository = new SailRepository(new MemoryStore());
        testRepository.init();
    }

    @Test
    void shouldInitRepositoryWithValidParameters() {
        String rdfServer = "http://localhost:8080/rdf";
        String repositoryID = "test-repo";
        
        Repository result = repositoryUtils.initRepository(rdfServer, repositoryID);
        // With NO_AUTH type, this should work without throwing exceptions
        // The actual result depends on the RepositoryInitiator implementation
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
        
        HttpStatus result = RepositoryUtils.executeUpdate(updateQuery, testRepository);
        assertEquals(HttpStatus.OK, result);
    }

    @Test
    void shouldReturnExpectationFailedForNullRepository() throws RmesException {
        String updateQuery = "INSERT DATA { <http://example.org/subject> <http://example.org/predicate> \"object\" . }";
        
        HttpStatus result = RepositoryUtils.executeUpdate(updateQuery, null);
        assertEquals(HttpStatus.EXPECTATION_FAILED, result);
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
            
            String result = RepositoryUtils.executeQuery(conn, query);
            assertNotNull(result);
            assertTrue(result.contains("results"));
        }
    }

    @Test
    void shouldExecuteAskQuery() throws RmesException {
        try (RepositoryConnection conn = testRepository.getConnection()) {
            String askQuery = "ASK { ?s ?p ?o }";
            
            boolean result = RepositoryUtils.executeAskQuery(conn, askQuery);
            // Should return false for empty repository
            assertFalse(result);
        }
    }

    @Test
    void shouldGetResponse() throws RmesException {
        String query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10";
        
        String response = RepositoryUtils.getResponse(query, testRepository);
        assertNotNull(response);
        assertTrue(response.contains("results"));
    }

    @Test
    void shouldGetResponseForAskQuery() throws RmesException {
        String askQuery = "ASK { ?s ?p ?o }";
        
        boolean response = RepositoryUtils.getResponseForAskQuery(askQuery, testRepository);
        assertFalse(response); // Empty repository
    }

    @Test
    void shouldGetResponseAsArray() throws RmesException {
        String query = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 10";
        
        JSONArray response = RepositoryUtils.getResponseAsArray(query, testRepository);
        // Should return empty array for empty repository
        assertTrue(response == null || response.isEmpty());
    }

    @Test
    void shouldGetResponseAsJSONList() throws RmesException {
        String query = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 10";
        
        JSONArray response = RepositoryUtils.getResponseAsJSONList(query, testRepository);
        // Should return empty array for empty repository
        assertTrue(response == null || response.isEmpty());
    }

    @Test
    void shouldGetResponseAsObject() throws RmesException {
        String query = "SELECT ?s WHERE { ?s ?p ?o } LIMIT 1";
        
        JSONObject response = RepositoryUtils.getResponseAsObject(query, testRepository);
        assertNotNull(response);
    }

    @Test
    void shouldConvertSparqlJSONToResultArrayValues() {
        JSONObject sparqlResult = new JSONObject();
        JSONObject results = new JSONObject();
        JSONArray bindings = new JSONArray();
        
        JSONObject binding = new JSONObject();
        JSONObject subject = new JSONObject();
        subject.put("value", "http://example.org/subject");
        binding.put("s", subject);
        
        bindings.put(binding);
        results.put("bindings", bindings);
        sparqlResult.put("results", results);
        
        JSONArray result = RepositoryUtils.sparqlJSONToResultArrayValues(sparqlResult);
        
        assertNotNull(result);
        assertEquals(1, result.length());
        assertEquals("http://example.org/subject", result.getJSONObject(0).getString("s"));
    }

    @Test
    void shouldReturnNullForEmptySparqlResults() {
        JSONObject sparqlResult = new JSONObject();
        
        JSONArray result = RepositoryUtils.sparqlJSONToResultArrayValues(sparqlResult);
        assertNull(result);
    }

    @Test
    void shouldConvertSparqlJSONToResultListValues() {
        JSONObject sparqlResult = new JSONObject();
        JSONObject results = new JSONObject();
        JSONArray bindings = new JSONArray();
        
        JSONObject binding = new JSONObject();
        JSONObject subject = new JSONObject();
        subject.put("value", "http://example.org/subject");
        binding.put("s", subject);
        
        bindings.put(binding);
        results.put("bindings", bindings);
        sparqlResult.put("results", results);
        
        JSONArray result = RepositoryUtils.sparqlJSONToResultListValues(sparqlResult);
        
        assertNotNull(result);
        assertEquals(1, result.length());
        assertEquals("http://example.org/subject", result.getString(0));
    }

    @Test
    void shouldReturnNullForEmptySparqlResultsList() {
        JSONObject sparqlResult = new JSONObject();
        
        JSONArray result = RepositoryUtils.sparqlJSONToResultListValues(sparqlResult);
        assertNull(result);
    }

    @Test
    void shouldClearStructureAndComponents() throws RmesException {
        Resource structure = SimpleValueFactory.getInstance().createIRI("http://example.org/structure");
        
        assertDoesNotThrow(() -> {
            RepositoryUtils.clearStructureAndComponents(structure, testRepository);
        });
    }

    @Test
    void shouldHandleMultipleBindingsInSparqlResult() {
        JSONObject sparqlResult = new JSONObject();
        JSONObject results = new JSONObject();
        JSONArray bindings = new JSONArray();
        
        // First binding
        JSONObject binding1 = new JSONObject();
        JSONObject subject1 = new JSONObject();
        subject1.put("value", "http://example.org/subject1");
        binding1.put("s", subject1);
        
        // Second binding
        JSONObject binding2 = new JSONObject();
        JSONObject subject2 = new JSONObject();
        subject2.put("value", "http://example.org/subject2");
        binding2.put("s", subject2);
        
        bindings.put(binding1);
        bindings.put(binding2);
        results.put("bindings", bindings);
        sparqlResult.put("results", results);
        
        JSONArray result = RepositoryUtils.sparqlJSONToResultArrayValues(sparqlResult);
        
        assertNotNull(result);
        assertEquals(2, result.length());
        assertEquals("http://example.org/subject1", result.getJSONObject(0).getString("s"));
        assertEquals("http://example.org/subject2", result.getJSONObject(1).getString("s"));
    }
}