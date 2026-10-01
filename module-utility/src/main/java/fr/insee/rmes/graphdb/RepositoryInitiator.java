package fr.insee.rmes.graphdb;

import fr.insee.rmes.keycloak.TokenService;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public interface RepositoryInitiator {
    static RepositoryInitiator newInstance(RdfBackend backend, Type type, TokenService tokenService) {
        if (backend == RdfBackend.FUSEKI) {
            throw new IllegalStateException(RdfBackend.PROPERTY
                    + "=fuseki : aucun initiator ne sait encore parler à Fuseki (FusekiRepositoryInitiator, F4)");
        }
        return graphDb(type, tokenService);
    }

    private static RepositoryInitiator graphDb(Type type, TokenService tokenService) {
        return type == Type.ENABLED ? new RepositoryInitiatorWithAuthent(tokenService) : new RepositoryInitiator() {};
    }

    default Repository initRepository(String rdfServer, String repositoryID) throws Exception {
        Repository repo = new HTTPRepository(rdfServer, repositoryID);
        repo.init();

        return repo;
    }

    enum Type {
        ENABLED,
        DISABLED
    }
}
