package fr.insee.rmes.graphdb.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class DatabaseQueryException extends RmesException {
    static final Logger logger = LoggerFactory.getLogger(DatabaseQueryException.class);

    private static final String EXECUTE_QUERY_FAILED = "Execute query failed : ";

    private final RDF4JException exception;

    public DatabaseQueryException(RDF4JException exception, String query) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
        this.exception = exception;

        logger.error("{} {}",EXECUTE_QUERY_FAILED, query, this.exception);
    }

    public String getMessage() {
        return exception.getMessage();
    }
}
