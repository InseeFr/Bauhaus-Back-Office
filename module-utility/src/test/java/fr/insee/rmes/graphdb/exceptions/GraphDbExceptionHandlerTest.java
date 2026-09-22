package fr.insee.rmes.graphdb.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.graphdb.RepositoryInitiator;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GraphDbExceptionHandlerTest {

    private GraphDbExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GraphDbExceptionHandler();
    }

    @Test
    void shouldAnswerAGenericMessageInsteadOfTheRdf4jOne() {
        String errorMessage = "Database connection failed";
        String query = "SELECT * FROM test";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, query);

        ResponseEntity<String> response = handler.genericInternalServerException(databaseException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(DatabaseQueryException.GENERIC_MESSAGE, response.getBody());
    }

    @Test
    void shouldNotExposeTheSparqlQueryNorTheRdf4jMessage() {
        String query = "SELECT ?secret WHERE { GRAPH <http://rdf.insee.fr/graphes/private> { ?s ?p ?secret } }";
        MalformedQueryException rdf4jException =
                new MalformedQueryException("Encountered \" \"}\" at line 1, column 42 in " + query);
        DatabaseQueryException databaseException = new DatabaseQueryException(rdf4jException, query);

        String body = handler.genericInternalServerException(databaseException).getBody();

        assertFalse(body.contains("http://rdf.insee.fr/graphes/private"));
        assertFalse(body.contains("Encountered"));
    }

    @Test
    void shouldReturnAnExplicitBodyWhenGraphDbAnswersUnauthorized() {
        GraphDbUnauthorizedException unauthorized = new GraphDbUnauthorizedException(
                new UnauthorizedException(), "SELECT * WHERE { ?s ?p ?o }", RepositoryInitiator.Type.DISABLED);

        ResponseEntity<String> response = handler.genericInternalServerException(unauthorized);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("401"));
        assertTrue(response.getBody().contains("fr.insee.rmes.bauhaus.rdf.auth=DISABLED"));
    }
}
