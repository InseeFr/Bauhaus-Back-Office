package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.util.Map;

public class RepositoryInitiatorWithAuthent implements RepositoryInitiator {
    private KeycloakServices keycloakServices;
    private String accessToken;
    private HTTPRepository repository;

    static final Logger logger = LogManager.getLogger(RepositoryInitiatorWithAuthent.class);

    public RepositoryInitiatorWithAuthent(KeycloakServices keycloakServices) {
        this.keycloakServices=keycloakServices;
    }

    @Override
    public Repository initRepository(String rdfServer, String repositoryID) throws Exception{
            if(!this.keycloakServices.isTokenValid(this.accessToken) || this.repository==null) {

                accessToken = keycloakServices.getKeycloakAccessToken();
                repository = new HTTPRepository(rdfServer, repositoryID);
                repository.setAdditionalHttpHeaders(Map.of("Authorization", "bearer " + accessToken));
                repository.init();
            }

        return repository;
    }
}
