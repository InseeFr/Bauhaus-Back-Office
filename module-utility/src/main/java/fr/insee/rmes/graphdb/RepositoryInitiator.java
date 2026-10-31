package fr.insee.rmes.graphdb;

import fr.insee.rmes.keycloak.TokenService;
import java.util.Optional;
import org.eclipse.rdf4j.http.client.HttpClientSessionManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public interface RepositoryInitiator {
    static RepositoryInitiator newInstance(
            RdfBackend backend, Type type, TokenService tokenService, Optional<RdfBasicCredentials> credentials) {
        if (backend == RdfBackend.FUSEKI) {
            return fuseki(type, credentials);
        }
        if (credentials.isPresent()) {
            throw new IllegalStateException(RdfBackend.PROPERTY
                    + "=graphdb : " + RdfBasicCredentials.USERNAME_PROPERTY + " ne sert qu'à Fuseki, GraphDB"
                    + " s'authentifie par jeton Keycloak (fr.insee.rmes.bauhaus.rdf.auth=ENABLED).");
        }
        return graphDb(type, tokenService);
    }

    private static RepositoryInitiator fuseki(Type type, Optional<RdfBasicCredentials> credentials) {
        if (type == Type.ENABLED) {
            throw new IllegalStateException(RdfBackend.PROPERTY
                    + "=fuseki : fr.insee.rmes.bauhaus.rdf.auth=ENABLED envoie un jeton Keycloak, que Fuseki ne sait pas"
                    + " vérifier. Passer l'authentification à DISABLED.");
        }
        return new FusekiRepositoryInitiator(credentials);
    }

    private static RepositoryInitiator graphDb(Type type, TokenService tokenService) {
        return type == Type.ENABLED ? new RepositoryInitiatorWithAuthent(tokenService) : new RepositoryInitiator() {};
    }

    /**
     * Crée et initialise le dépôt d'une base. {@link RepositoryUtils} ne l'appelle qu'une fois par base et garde le
     * dépôt jusqu'à l'arrêt de l'application. Le client HTTP est partagé entre les dépôts et appartient à
     * l'appelant : fermer le dépôt ne le ferme pas.
     */
    default Repository initRepository(
            String rdfServer, String repositoryID, HttpClientSessionManager httpClientSessionManager) {
        var repo = new HTTPRepository(rdfServer, repositoryID);
        repo.setHttpClientSessionManager(httpClientSessionManager);
        repo.init();

        return repo;
    }

    /** Appelé à chaque prêt du dépôt partagé, pour renouveler ce qui expire entre deux requêtes. */
    default void beforeLending(Repository repository) {}

    enum Type {
        ENABLED,
        DISABLED
    }
}
