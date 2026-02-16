package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.keycloak.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Colectica authenticator using Keycloak.
 * Retrieves tokens from Keycloak using the colectica realm.
 */
@Component
@ConditionalOnProperty(
        name = "fr.insee.rmes.bauhaus.colectica.server.authenticationMode",
        havingValue = "token"
)
public class TokenColecticaAuthenticator implements ColecticaAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(TokenColecticaAuthenticator.class);

    private final TokenService tokenService;

    public TokenColecticaAuthenticator(@Qualifier("colectica") TokenService tokenService) {
        this.tokenService = tokenService;
        logger.info("Using Keycloak authentication mode for Colectica API");
    }

    @Override
    public <T> T executeWithAuth(Function<String, T> apiCall) {
        logger.debug("Executing API call with Keycloak token");
        String token = tokenService.getAccessToken();
        return apiCall.apply(token);
    }
}
