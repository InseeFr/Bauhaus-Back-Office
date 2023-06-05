package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
<<<<<<< Updated upstream
=======
import fr.insee.rmes.config.keycloak.KeycloakServerZoneConfiguration;
import fr.insee.rmes.exceptions.RmesException;
>>>>>>> Stashed changes
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.util.Map;

public class RepositoryInitiatorWithAuthent implements RepositoryInitiator {
<<<<<<< Updated upstream
    private KeycloakServices keycloakServices;
    private String accessToken;
    private HTTPRepository repository;
=======
    private final KeycloakServices keycloakServices;
    private final Map<String, String> accessTokens=new HashMap<>();
    private final Map<String, HTTPRepository> repositories=new HashMap<>();

>>>>>>> Stashed changes

    static final Logger logger = LogManager.getLogger(RepositoryInitiatorWithAuthent.class);

    public RepositoryInitiatorWithAuthent(KeycloakServices keycloakServices) {
        this.keycloakServices=keycloakServices;
    }

    @Override
    public Repository initRepository(String rdfServer, String repositoryID) throws Exception{
            if(!this.keycloakServices.isTokenValid(this.accessToken) || this.repository==null) {

<<<<<<< Updated upstream
                accessToken = keycloakServices.getKeycloakAccessToken();
=======
    private HTTPRepository refreshRepository(String rdfServer, String repositoryID, HTTPRepository repository) throws RmesException {
            if(!this.keycloakServices.isTokenValid(this.accessTokens.get(rdfServer)) || repository==null) {

                var accessToken = keycloakServices.getKeycloakAccessToken(rdfServer);
>>>>>>> Stashed changes
                repository = new HTTPRepository(rdfServer, repositoryID);
                repository.setAdditionalHttpHeaders(Map.of("Authorization", "bearer " + accessToken));
                repository.init();
            }

        return repository;
    }

}
