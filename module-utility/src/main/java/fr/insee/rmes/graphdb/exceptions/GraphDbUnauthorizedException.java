package fr.insee.rmes.graphdb.exceptions;

import fr.insee.rmes.graphdb.RepositoryInitiator;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraphDB a répondu <em>401 Unauthorized</em>.
 * <p>
 * RDF4J lève dans ce cas une {@link UnauthorizedException} sans message : sans traitement
 * particulier, l'API répondait une 500 au corps vide, illisible pour diagnostiquer ce qui
 * est presque toujours une erreur de configuration. Le message porté par cette exception
 * nomme le mode d'authentification RDF effectivement configuré et l'action corrective
 * correspondante.
 */
public class GraphDbUnauthorizedException extends DatabaseQueryException {

    public static final String RDF_AUTH_PROPERTY = "fr.insee.rmes.bauhaus.rdf.auth";

    private static final int MAX_CAUSE_DEPTH = 10;

    private static final Logger logger = LoggerFactory.getLogger(GraphDbUnauthorizedException.class);

    public GraphDbUnauthorizedException(RDF4JException exception, String query, RepositoryInitiator.Type authType) {
        this(exception, query, buildMessage(authType));
    }

    private GraphDbUnauthorizedException(RDF4JException exception, String query, String message) {
        super(exception, query, message);
        logger.error(message);
    }

    /**
     * @return {@code true} si l'échec est (ou a pour cause) un 401 renvoyé par GraphDB.
     */
    public static boolean isUnauthorized(Throwable throwable) {
        Throwable current = throwable;
        // profondeur bornée : une chaîne de causes cyclique ferait boucler indéfiniment
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof UnauthorizedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String buildMessage(RepositoryInitiator.Type authType) {
        if (authType == RepositoryInitiator.Type.DISABLED) {
            return "GraphDB a refusé la requête (401 Unauthorized) alors que l'authentification RDF est désactivée ("
                    + RDF_AUTH_PROPERTY + "=DISABLED) : aucun jeton n'a été transmis. "
                    + "Si le serveur GraphDB est sécurisé, positionnez " + RDF_AUTH_PROPERTY
                    + "=ENABLED et configurez le client Keycloak utilisé pour obtenir le jeton.";
        }
        return "GraphDB a refusé la requête (401 Unauthorized) alors que l'authentification RDF est activée ("
                + RDF_AUTH_PROPERTY + "=ENABLED) : le jeton transmis a été rejeté. "
                + "Vérifiez la configuration du client Keycloak (URL, realm, identifiants) et ses droits sur le dépôt.";
    }
}
