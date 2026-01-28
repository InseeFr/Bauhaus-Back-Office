package fr.insee.rmes.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

public abstract class AbstractTokenService implements TokenService {

    protected RestTemplate keycloakClient = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(AbstractTokenService.class);

    protected final KeycloakProperties keycloakProperties;

    protected AbstractTokenService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    protected abstract KeycloakProperties.RealmConfig getRealmConfig();

    protected String getTokenUrl() {
        if (keycloakProperties == null || keycloakProperties.server() == null) {
            return null;
        }
        return keycloakProperties.tokenUrl(getRealmConfig());
    }

    @Override
    public String getAccessToken() {
        KeycloakProperties.RealmConfig realmConfig = getRealmConfig();
        String tokenUrl = getTokenUrl();

        logger.debug("Requesting access token from Keycloak for realm: {}", realmConfig.name());

        if (tokenUrl == null || tokenUrl.isEmpty()) {
            logger.debug("Token URL is null or empty, configuration is missing");
            throw new MissingKeycloakConfigurationException();
        }

        logger.debug("Token URL: {}", tokenUrl);
        logger.debug("Client ID: {}", realmConfig.clientid());

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", realmConfig.clientid());
        body.add("client_secret", realmConfig.clientsecret());

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            logger.debug("Sending token request to Keycloak...");
            Token accessToken = keycloakClient.postForObject(tokenUrl, entity, Token.class);

            logger.debug("Access token successfully retrieved from Keycloak");
            return accessToken.getAccessToken();

        } catch (RestClientException e) {
            logger.warn("Failed to retrieve token from Keycloak: {}", e.getMessage());
            throw new UnreachableKeycloakException(e);
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        logger.debug("Validating token...");

        if (token == null) {
            logger.debug("Token is null, validation failed");
            return false;
        }

        var isValid = false;
        var now = nowPlus1Second();
        try {
            DecodedJWT jwt = JWT.decode(token);
            if (jwt.getExpiresAt().after(now)) {
                isValid = true;
                logger.debug("Token is valid, expires at: {}", jwt.getExpiresAt());
            } else {
                logger.debug("Token has expired at: {}", jwt.getExpiresAt());
            }
        } catch (JWTDecodeException exception) {
            logger.debug("Token decoding failed: {}", exception.getMessage());
        }

        logger.debug("Token validation result: {}", isValid);
        return isValid;
    }

    protected Date nowPlus1Second() {
        return Date.from((new Date()).toInstant().plusSeconds(1));
    }
}
