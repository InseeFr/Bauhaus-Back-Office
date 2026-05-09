package fr.insee.rmes.rdf_utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RepositoryGestionTest {

    @Mock
    private RdfConnectionDetails rdfConnectionDetails;

    @Mock
    private RepositoryUtils repositoryUtils;

    @Mock
    private Repository repository;

    @Mock
    private RepositoryConnection connection;

    @Mock
    private RepositoryResult<Statement> repositoryResult;

    private RepositoryGestion repositoryGestion;
    private ValueFactory valueFactory;

    @BeforeEach
    void setUp() {
        repositoryGestion = new RepositoryGestion(rdfConnectionDetails, repositoryUtils);
        valueFactory = SimpleValueFactory.getInstance();
        
        lenient().when(rdfConnectionDetails.getUrlServer()).thenReturn("http://localhost:8080");
        lenient().when(rdfConnectionDetails.repositoryId()).thenReturn("test-repo");
        lenient().when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(repository);
        lenient().when(repository.getConnection()).thenReturn(connection);
    }

    @Test
    void shouldGetResponse() throws RmesException {
        String query = "SELECT * WHERE { ?s ?p ?o }";
        
        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        String result = repositoryGestion.getResponse(query);

        assertNotNull(result);
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldExecuteUpdate() throws RmesException {
        String updateQuery = "INSERT { <http://example.org/s> <http://example.org/p> <http://example.org/o> } WHERE { }";
        
        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        HttpStatus result = repositoryGestion.executeUpdate(updateQuery);

        assertEquals(HttpStatus.OK, result);
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldGetResponseAsObject() throws RmesException {
        String query = "SELECT * WHERE { ?s ?p ?o }";
        
        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        JSONObject result = repositoryGestion.getResponseAsObject(query);

        assertNotNull(result);
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldGetResponseAsArray() throws RmesException {
        String query = "SELECT * WHERE { ?s ?p ?o }";
        
        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        JSONArray result = repositoryGestion.getResponseAsArray(query);

        assertTrue( result.isEmpty());
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldGetResponseAsJSONList() throws RmesException {
        String query = "SELECT * WHERE { ?s ?p ?o }";
        
        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        JSONArray result = repositoryGestion.getResponseAsJSONList(query);

        assertNotNull(result);
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldGetResponseAsBoolean() throws RmesException {
        String query = "ASK { ?s ?p ?o }";
        
        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        boolean result = repositoryGestion.getResponseAsBoolean(query);

        assertFalse(result); // Empty repository returns false for ASK queries
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldGetStatements() throws RmesException {
        IRI subject = valueFactory.createIRI("http://example.org/subject");

        when(connection.getStatements(subject, null, null, false)).thenReturn(repositoryResult);

        RepositoryResult<Statement> result = repositoryGestion.getStatements(connection, subject);

        assertEquals(repositoryResult, result);
        verify(connection).getStatements(subject, null, null, false);
    }

    @Test
    void shouldGetStatementsWithNullConnection() throws RmesException {
        IRI subject = valueFactory.createIRI("http://example.org/subject");

        when(connection.getStatements(subject, null, null, false)).thenReturn(repositoryResult);

        RepositoryResult<Statement> result = repositoryGestion.getStatements(null, subject);

        assertEquals(repositoryResult, result);
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
        verify(connection).getStatements(subject, null, null, false);
    }

    @Test
    void shouldThrowExceptionWhenGetStatementsThrowsRepositoryException() throws RmesException {
        IRI subject = valueFactory.createIRI("http://example.org/subject");
        RepositoryException repositoryException = new RepositoryException("Test exception");

        when(connection.getStatements(subject, null, null, false)).thenThrow(repositoryException);

        assertThrows(RmesException.class, () -> repositoryGestion.getStatements(connection, subject));
    }

    @Test
    void shouldGetHasPartStatements() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");

        when(connection.getStatements(null, DCTERMS.HAS_PART, object, false)).thenReturn(repositoryResult);

        RepositoryResult<Statement> result = repositoryGestion.getHasPartStatements(connection, object);

        assertEquals(repositoryResult, result);
        verify(connection).getStatements(null, DCTERMS.HAS_PART, object, false);
    }

    @Test
    void shouldGetReplacesStatements() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");

        when(connection.getStatements(null, DCTERMS.REPLACES, object, false)).thenReturn(repositoryResult);

        RepositoryResult<Statement> result = repositoryGestion.getReplacesStatements(connection, object);

        assertEquals(repositoryResult, result);
        verify(connection).getStatements(null, DCTERMS.REPLACES, object, false);
    }

    @Test
    void shouldGetIsReplacedByStatements() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");

        when(connection.getStatements(null, DCTERMS.IS_REPLACED_BY, object, false)).thenReturn(repositoryResult);

        RepositoryResult<Statement> result = repositoryGestion.getIsReplacedByStatements(connection, object);

        assertEquals(repositoryResult, result);
        verify(connection).getStatements(null, DCTERMS.IS_REPLACED_BY, object, false);
    }

    @Test
    void shouldCloseStatements() throws RmesException {
        repositoryGestion.closeStatements(repositoryResult);

        verify(repositoryResult).close();
    }

    @Test
    void shouldThrowExceptionWhenCloseStatementsThrowsRepositoryException() throws RmesException {
        RepositoryException repositoryException = new RepositoryException("Test exception");
        doThrow(repositoryException).when(repositoryResult).close();

        assertThrows(RmesException.class, () -> repositoryGestion.closeStatements(repositoryResult));
    }



    @Test
    void shouldDeleteTripletByPredicateAndValueWithConnection() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");
        IRI predicate = valueFactory.createIRI("http://example.org/predicate");
        IRI graph = valueFactory.createIRI("http://example.org/graph");
        Value value = valueFactory.createLiteral("test value");

        repositoryGestion.deleteTripletByPredicateAndValue(object, predicate, graph, connection, value);

        verify(connection).remove(object, predicate, value, graph);
        verify(connection).close();
    }

    @Test
    void shouldDeleteTripletByPredicateAndValueWithoutConnection() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");
        IRI predicate = valueFactory.createIRI("http://example.org/predicate");
        IRI graph = valueFactory.createIRI("http://example.org/graph");
        Value value = valueFactory.createLiteral("test value");

        repositoryGestion.deleteTripletByPredicateAndValue(object, predicate, graph, value);

        verify(connection).remove(object, predicate, value, graph);
        verify(connection).close();
    }

    @Test
    void shouldDeleteTripletByPredicate() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");
        IRI predicate = valueFactory.createIRI("http://example.org/predicate");
        IRI graph = valueFactory.createIRI("http://example.org/graph");

        repositoryGestion.deleteTripletByPredicate(object, predicate, graph, connection);

        verify(connection).remove(object, predicate, null, graph);
        verify(connection).close();
    }

    @Test
    void shouldLoadSimpleObjectWithoutDeletion() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");
        Model model = new TreeModel();

        repositoryGestion.loadSimpleObjectWithoutDeletion(object, model, connection);

        verify(connection).add(model);
        verify(connection).close();
    }

    @Test
    void shouldLoadSimpleObject() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");
        Model model = new TreeModel();

        repositoryGestion.loadSimpleObject(object, model, connection);

        verify(connection).remove(object, null, null);
        verify(connection).add(model);
        verify(connection).close();
    }

    @Test
    void shouldDeleteObject() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");

        repositoryGestion.deleteObject(object, connection);

        verify(connection).remove(object, null, null);
        verify(connection).close();
    }

    @Test
    void shouldReplaceGraph() throws RmesException {
        IRI graph = valueFactory.createIRI("http://example.org/graph");
        Model model = new TreeModel();

        repositoryGestion.replaceGraph(graph, model, connection);

        verify(connection).clear(graph);
        verify(connection).add(model);
        verify(connection).close();
    }

    @Test
    void shouldObjectValidation() throws RmesException {
        IRI resourceURI = valueFactory.createIRI("http://example.org/resource");
        Model model = new TreeModel();

        repositoryGestion.objectValidation(resourceURI, model);

        verify(connection).remove(resourceURI, INSEE.VALIDATION_STATE, null);
        verify(connection).remove(resourceURI, INSEE.IS_VALIDATED, null);
        verify(connection).add(model);
        verify(connection).close();
    }

    @Test
    void shouldObjectsValidation() throws RmesException {
        List<IRI> collectionsToValidate = Arrays.asList(
            valueFactory.createIRI("http://example.org/resource1"),
            valueFactory.createIRI("http://example.org/resource2")
        );
        Model model = new TreeModel();

        repositoryGestion.objectsValidation(collectionsToValidate, model);

        for (IRI item : collectionsToValidate) {
            verify(connection).remove(item, INSEE.VALIDATION_STATE, null);
            verify(connection).remove(item, INSEE.IS_VALIDATED, null);
        }
        verify(connection).add(model);
        verify(connection).close();
    }

    @Test
    void shouldGetConnection() throws RmesException {
        when(repositoryUtils.getConnection(repository)).thenReturn(connection);

        RepositoryConnection result = repositoryGestion.getConnection();

        assertEquals(connection, result);
        verify(repositoryUtils).getConnection(repository);
    }

    @Test
    void shouldOverrideTriplets() throws RmesException {
        IRI simsUri = valueFactory.createIRI("http://example.org/sims");
        IRI graph = valueFactory.createIRI("http://example.org/graph");
        Model model = new TreeModel();
        IRI predicate = valueFactory.createIRI("http://example.org/predicate");
        model.add(simsUri, predicate, valueFactory.createLiteral("test"));

        repositoryGestion.overrideTriplets(simsUri, model, graph);

        verify(connection).remove(simsUri, predicate, null, graph);
        verify(connection).add(model);
        verify(connection).close();
    }

    @Test
    void shouldGetCompleteGraph() throws RmesException {
        IRI graphIri = valueFactory.createIRI("http://example.org/graph");

        when(repositoryUtils.getCompleteGraph(connection, graphIri)).thenReturn(repositoryResult);

        RepositoryResult<Statement> result = repositoryGestion.getCompleteGraph(connection, graphIri);

        assertEquals(repositoryResult, result);
        verify(repositoryUtils).getCompleteGraph(connection, graphIri);
    }

    @Test
    void shouldGetMultipleTripletsForObject() throws RmesException {
        JSONObject object = new JSONObject();
        String objectKey = "results";
        String query = "SELECT * WHERE { ?s ?p ?o }";
        String queryKey = "value";

        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        repositoryGestion.getMultipleTripletsForObject(object, objectKey, query, queryKey);

        // Empty repository won't add anything to the object
        assertTrue(object.has(objectKey));
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldHandleNullArrayInGetMultipleTripletsForObject() throws RmesException {
        JSONObject object = new JSONObject();
        String objectKey = "results";
        String query = "SELECT * WHERE { ?s ?p ?o }";
        String queryKey = "value";

        Repository memoryRepo = new SailRepository(new MemoryStore());
        memoryRepo.init();
        when(repositoryUtils.initRepository(anyString(), anyString())).thenReturn(memoryRepo);

        repositoryGestion.getMultipleTripletsForObject(object, objectKey, query, queryKey);

        assertTrue(object.has(objectKey));
        verify(repositoryUtils).initRepository("http://localhost:8080", "test-repo");
    }

    @Test
    void shouldKeepHierarchicalOperationLinks() throws RmesException {
        IRI object = valueFactory.createIRI("http://example.org/object");
        Model model = new TreeModel();

        when(connection.getStatements(null, DCTERMS.HAS_PART, object, false)).thenReturn(repositoryResult);
        when(connection.getStatements(object, DCTERMS.HAS_PART, null, false)).thenReturn(repositoryResult);
        when(connection.getStatements(null, DCTERMS.IS_PART_OF, object, false)).thenReturn(repositoryResult);
        when(connection.getStatements(object, DCTERMS.IS_PART_OF, null, false)).thenReturn(repositoryResult);
        when(repositoryResult.hasNext()).thenReturn(false);

        repositoryGestion.keepHierarchicalOperationLinks(object, model);

        verify(connection, times(4)).getStatements(any(), any(), any(), eq(false));
        verify(connection, times(4)).remove(repositoryResult);
    }
}