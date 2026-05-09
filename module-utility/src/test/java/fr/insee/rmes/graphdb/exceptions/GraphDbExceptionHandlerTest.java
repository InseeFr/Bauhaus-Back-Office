package fr.insee.rmes.graphdb.exceptions;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GraphDbExceptionHandlerTest {

    private GraphDbExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GraphDbExceptionHandler();
    }

    @Test
    void shouldHandleDatabaseQueryException() {
        String errorMessage = "Database connection failed";
        String query = "SELECT * FROM test";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, query);

        ResponseEntity<String> response = handler.genericInternalServerException(databaseException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void shouldReturnCorrectHttpStatusForDatabaseQueryException() {
        String errorMessage = "Query execution timeout";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, "UPDATE test SET value = 1");

        ResponseEntity<String> response = handler.genericInternalServerException(databaseException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnExceptionMessageInResponseBody() {
        String expectedMessage = "SPARQL query syntax error";
        MalformedQueryException rdf4jException = new MalformedQueryException(expectedMessage);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, "INVALID QUERY");

        ResponseEntity<String> response = handler.genericInternalServerException(databaseException);

        assertEquals(expectedMessage, response.getBody());
    }


    @Test
    void shouldHandleEmptyMessageInException() {
        String emptyMessage = "";
        MalformedQueryException rdf4jException = new MalformedQueryException(emptyMessage);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, "SELECT 1");

        ResponseEntity<String> response = handler.genericInternalServerException(databaseException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(emptyMessage, response.getBody());
    }

    @Test
    void shouldReturnResponseEntityWithStringBody() {
        String errorMessage = "Connection to RDF store failed";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, "ASK { ?s ?p ?o }");

        ResponseEntity<String> response = handler.genericInternalServerException(databaseException);

        assertInstanceOf(ResponseEntity.class, response);
        assertInstanceOf(String.class, response.getBody());
        assertEquals(errorMessage, response.getBody());
    }
}