package fr.insee.rmes.graphdb.exceptions;

import fr.insee.rmes.domain.exceptions.CodedRmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class DatabaseQueryException extends CodedRmesException {
    static final Logger logger = LoggerFactory.getLogger(DatabaseQueryException.class);

    public static final String GENERIC_MESSAGE = "The RDF database could not be queried.";

    public static final String RDF_QUERY_FAILED = "RDF_QUERY_FAILED";

    private static final String EXECUTE_QUERY_FAILED = "Execute query failed : ";

    private final RDF4JException exception;

    private final String message;

    public DatabaseQueryException(RDF4JException exception, String query) {
        this(exception, query, exception.getMessage(), RDF_QUERY_FAILED, GENERIC_MESSAGE);
    }

    /**
     * @param message message journalisé, qui peut citer la requête ou GraphDB
     * @param code code de l'erreur, clé de traduction du front
     * @param clientMessage message de la réponse HTTP : ni requête SPARQL, ni message RDF4J
     */
    protected DatabaseQueryException(
            RDF4JException exception, String query, String message, String code, String clientMessage) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), code, clientMessage, exception);
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
