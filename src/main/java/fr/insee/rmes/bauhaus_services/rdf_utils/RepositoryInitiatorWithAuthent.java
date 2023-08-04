package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;

import fr.insee.rmes.exceptions.RmesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.util.HashMap;
import java.util.Map;

public class RepositoryInitiatorWithAuthent implements RepositoryInitiator {

    private final KeycloakServices keycloakServices;
    private final Map<String, String> accessTokens=new HashMap<>();
    private final Map<String, HTTPRepository> repositories=new HashMap<>();


    static final Logger logger = LogManager.getLogger(RepositoryInitiatorWithAuthent.class);

    public RepositoryInitiatorWithAuthent(KeycloakServices keycloakServices) {
        this.keycloakServices=keycloakServices;
    }

    @Override
    public Repository initRepository(String rdfServer, String repositoryID) throws Exception {
        var repository=repositories.get(rdfServer + repositoryID);
        repository=refreshRepository(rdfServer, repositoryID, repository);
        repositories.put( rdfServer + repositoryID, repository);
        return repository;
    }

    private HTTPRepository refreshRepository(String rdfServer, String repositoryID, HTTPRepository repository) throws RmesException {
            if(!this.keycloakServices.isTokenValid(this.accessTokens.get(rdfServer + repositoryID)) || repository==null) {

                var accessToken = keycloakServices.getKeycloakAccessToken(rdfServer);

                repository = new HTTPRepository(rdfServer, repositoryID);
                repository.setAdditionalHttpHeaders(Map.of("Authorization", "bearer " + accessToken));
                this.accessTokens.put(rdfServer + repositoryID, accessToken);
                repository.init();
            }

        return repository;
    }

}
