package fr.insee.rmes.graphdb.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class DatabaseQueryException extends RmesException {
    static final Logger logger = LoggerFactory.getLogger(DatabaseQueryException.class);

    private static final String EXECUTE_QUERY_FAILED = "Execute query failed : ";

    private final RDF4JException exception;

    private final String message;

    public DatabaseQueryException(RDF4JException exception, String query) {
        this(exception, query, exception.getMessage());
    }

    protected DatabaseQueryException(RDF4JException exception, String query, String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        this.exception = exception;
        this.message = message;

        logger.error("{} {}", EXECUTE_QUERY_FAILED, query, this.exception);
    }

    /**
     * Traduit un échec RDF4J en exception applicative, en explicitant le cas particulier
     * du 401 renvoyé par GraphDB, que RDF4J signale sans message.
     */
    public static DatabaseQueryException from(
            RDF4JException exception, String query, RepositoryInitiator.Type authType) {
        if (GraphDbUnauthorizedException.isUnauthorized(exception)) {
            return new GraphDbUnauthorizedException(exception, query, authType);
        }
        return new DatabaseQueryException(exception, query);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
