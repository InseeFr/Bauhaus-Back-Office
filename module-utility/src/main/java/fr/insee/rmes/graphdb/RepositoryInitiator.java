package fr.insee.rmes.graphdb;

import fr.insee.rmes.keycloak.TokenService;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public interface RepositoryInitiator {
    static RepositoryInitiator newInstance(RdfBackend backend, Type type, TokenService tokenService) {
        if (backend == RdfBackend.FUSEKI) {
            return fuseki(type);
        }
        return graphDb(type, tokenService);
    }

    private static RepositoryInitiator fuseki(Type type) {
        if (type == Type.ENABLED) {
            throw new IllegalStateException(RdfBackend.PROPERTY
                    + "=fuseki : fr.insee.rmes.bauhaus.rdf.auth=ENABLED envoie un jeton Keycloak, que Fuseki ne sait pas"
                    + " vérifier. Passer l'authentification à DISABLED.");
        }
        return new FusekiRepositoryInitiator();
    }

    private static RepositoryInitiator graphDb(Type type, TokenService tokenService) {
        return type == Type.ENABLED ? new RepositoryInitiatorWithAuthent(tokenService) : new RepositoryInitiator() {};
    }

    /**
     * Crée et initialise le dépôt d'une base. {@link RepositoryUtils} ne l'appelle qu'une fois par base et garde le
     * dépôt jusqu'à l'arrêt de l'application.
     */
    default Repository initRepository(String rdfServer, String repositoryID) {
        Repository repo = new HTTPRepository(rdfServer, repositoryID);
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
