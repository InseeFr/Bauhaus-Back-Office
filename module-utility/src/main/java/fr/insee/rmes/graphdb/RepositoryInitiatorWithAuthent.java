package fr.insee.rmes.graphdb;

import fr.insee.rmes.keycloak.TokenService;
import java.util.Map;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public class RepositoryInitiatorWithAuthent implements RepositoryInitiator {

    private final TokenService tokenService;
    private volatile String accessToken;

    public RepositoryInitiatorWithAuthent(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /** Le dépôt est partagé : le jeton expiré est remplacé dans ses en-têtes, sans recréer de dépôt. */
    @Override
    public void beforeLending(Repository repository) {
        String token = accessToken;
        if (!tokenService.isTokenValid(token)) {
            token = tokenService.getAccessToken();
            accessToken = token;
        }
        ((HTTPRepository) repository).setAdditionalHttpHeaders(Map.of("Authorization", "bearer " + token));
    }
}
