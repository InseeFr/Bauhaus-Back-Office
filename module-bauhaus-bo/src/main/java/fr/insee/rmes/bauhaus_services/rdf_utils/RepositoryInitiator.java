package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;


public interface RepositoryInitiator {
    static RepositoryInitiator newInstance(Type type, KeycloakServices keycloakServices) {
        return type==Type.ENABLED ? new RepositoryInitiatorWithAuthent(keycloakServices):new RepositoryInitiator() {
            };
    }

    default Repository initRepository(String rdfServer, String repositoryID) throws Exception{
        Repository repo = new HTTPRepository(rdfServer, repositoryID);
        repo.init();

        return repo;
    }

    enum Type {ENABLED , DISABLED}
}
