package fr.insee.rmes.graphdb;

import fr.insee.rmes.keycloak.TokenService;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.util.HashMap;
import java.util.Map;

public class RepositoryInitiatorWithAuthent implements RepositoryInitiator {

    private final TokenService tokenService;
    private final Map<String, String> accessTokens=new HashMap<>();
    private final Map<String, HTTPRepository> repositories=new HashMap<>();


    public RepositoryInitiatorWithAuthent(TokenService tokenService) {
        this.tokenService=tokenService;
    }

    @Override
    public Repository initRepository(String rdfServer, String repositoryID) throws Exception {
        var repository=repositories.get(rdfServer + repositoryID);
        repository=refreshRepository(rdfServer, repositoryID, repository);
        repositories.put( rdfServer + repositoryID, repository);
        return repository;
    }

    private HTTPRepository refreshRepository(String rdfServer, String repositoryID, HTTPRepository repository) {
            if(!this.tokenService.isTokenValid(this.accessTokens.get(rdfServer + repositoryID)) || repository==null) {

                var accessToken = tokenService.getAccessToken();

                repository = new HTTPRepository(rdfServer, repositoryID);
                repository.setAdditionalHttpHeaders(Map.of("Authorization", "bearer " + accessToken));
                this.accessTokens.put(rdfServer + repositoryID, accessToken);
                repository.init();
            }

        return repository;
    }

}
