package fr.insee.rmes.graphdb.exceptions;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseQueryExceptionTest {

    @Test
    void shouldCreateDatabaseQueryExceptionWithCorrectValues() {
        String query = "SELECT * FROM test";
        String errorMessage = "Connection failed";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);

        DatabaseQueryException exception = new DatabaseQueryException(rdf4jException, query);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getStatus());
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(errorMessage, exception.getDetails());
    }

    @Test
    void shouldLogErrorWhenCreatingException() {
        Logger logger = (Logger) LoggerFactory.getLogger(DatabaseQueryException.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        String query = "SELECT * FROM test";
        String errorMessage = "Connection failed";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);

        var unused = new DatabaseQueryException(rdf4jException, query);

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals(Level.ERROR, logsList.getFirst().getLevel());
        assertTrue(logsList.getFirst().getFormattedMessage().contains("Execute query failed :"));
        assertTrue(logsList.getFirst().getFormattedMessage().contains(query));

        logger.detachAppender(listAppender);
    }

    @Test
    void shouldReturnCorrectMessageFromRDF4JException() {
        String expectedMessage = "Database connection timeout";
        MalformedQueryException rdf4jException = new MalformedQueryException(expectedMessage);
        String query = "UPDATE test SET value = 1";

        DatabaseQueryException exception = new DatabaseQueryException(rdf4jException, query);

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldInheritFromRmesException() {
        MalformedQueryException rdf4jException = new MalformedQueryException("Test error");
        DatabaseQueryException exception = new DatabaseQueryException(rdf4jException, "SELECT 1");

        assertInstanceOf(fr.insee.rmes.domain.exceptions.RmesException.class, exception);
    }

    @Test
    void shouldHandleNullQuery() {
        String errorMessage = "Null query error";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);

        DatabaseQueryException exception = new DatabaseQueryException(rdf4jException, null);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getStatus());
    }

    @Test
    void shouldHandleEmptyQuery() {
        String errorMessage = "Empty query error";
        MalformedQueryException rdf4jException = new MalformedQueryException(errorMessage);

        DatabaseQueryException exception = new DatabaseQueryException(rdf4jException, "");

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getStatus());
    }
}