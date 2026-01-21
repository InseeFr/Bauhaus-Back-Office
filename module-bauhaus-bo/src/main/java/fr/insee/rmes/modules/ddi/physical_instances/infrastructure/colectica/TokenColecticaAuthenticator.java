package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Colectica authenticator using a static token.
 * Uses a pre-configured token without calling the authentication endpoint.
 */
@Component
@ConditionalOnProperty(
        name = "fr.insee.rmes.bauhaus.colectica.server.authenticationMode",
        havingValue = "token"
)
public class TokenColecticaAuthenticator implements ColecticaAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(TokenColecticaAuthenticator.class);

    private final String token;

    public TokenColecticaAuthenticator(ColecticaConfiguration colecticaConfiguration) {
        this.token = colecticaConfiguration.server().token();
        if (this.token == null || this.token.isBlank()) {
            throw new IllegalStateException("Token authentication mode requires a non-empty token configuration");
        }
        logger.info("Using static token authentication mode for Colectica API");
    }

    @Override
    public <T> T executeWithAuth(Function<String, T> apiCall) {
        logger.debug("Executing API call with static token");
        return apiCall.apply(token);
    }
}