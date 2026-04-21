package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

/**
 * Colectica authenticator using username/password.
 * Retrieves a token from the Colectica API using credentials.
 */
@Component
@ConditionalOnProperty(
        name = "fr.insee.rmes.bauhaus.colectica.server.authenticationMode",
        havingValue = "password",
        matchIfMissing = true
)
public class PasswordColecticaAuthenticator implements ColecticaAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(PasswordColecticaAuthenticator.class);

    private final RestTemplate restTemplate;
    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private String cachedAuthToken;

    public PasswordColecticaAuthenticator(
            RestTemplate restTemplate,
            ColecticaConfiguration colecticaConfiguration
    ) {
        this.restTemplate = restTemplate;
        this.instanceConfiguration = colecticaConfiguration.server();
        logger.info("Using password authentication mode for Colectica API");
    }

    @Override
    public <T> T executeWithAuth(Function<String, T> apiCall) {
        try {
            String token = getAuthToken(false);
            return apiCall.apply(token);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("Authentication failed with cached token, refreshing and retrying...");
                cachedAuthToken = null;
                String newToken = getAuthToken(true);
                return apiCall.apply(newToken);
            }
            throw e;
        }
    }

    private String getAuthToken(boolean forceRefresh) {
        if (!forceRefresh && cachedAuthToken != null) {
            logger.debug("Using cached authentication token");
            return cachedAuthToken;
        }

        logger.info("Authenticating to Colectica API with username/password");

        String tokenUrl = instanceConfiguration.baseServerUrl() + "/token/createtoken";

        AuthenticationRequest authRequest = new AuthenticationRequest(
                instanceConfiguration.username(),
                instanceConfiguration.password()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthenticationRequest> requestEntity = new HttpEntity<>(authRequest, headers);

        AuthenticationResponse authResponse = restTemplate.postForObject(
                tokenUrl,
                requestEntity,
                AuthenticationResponse.class
        );

        if (authResponse == null || authResponse.accessToken() == null) {
            logger.error("Failed to retrieve authentication token");
            throw new RuntimeException("Authentication failed: unable to retrieve access token");
        }

        cachedAuthToken = authResponse.accessToken();
        return cachedAuthToken;
    }
}