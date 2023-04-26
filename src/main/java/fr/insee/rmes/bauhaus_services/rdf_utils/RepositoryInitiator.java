package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.util.Map;
import java.util.Objects;

public interface RepositoryInitiator {
    static RepositoryInitiator newInstance(Type type, KeycloakServices keycloakServices) {
        switch (type) {
            case ENABLED:
                return new RepositoryInitiatorWithAuthent(keycloakServices);
            case DISABLED:
                return new RepositoryInitiator() {};
            default:
                return new RepositoryInitiator() {};
        }
    }

    default Repository initRepository(String rdfServer, String repositoryID) throws Exception{
        Repository repo = new HTTPRepository(rdfServer, repositoryID);
        repo.init();

        return repo;
    }

    public enum Type {ENABLED , DISABLED}
}
