package fr.insee.rmes.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Date;

public abstract class AbstractTokenService implements TokenService {

    protected RestClient keycloakClient = RestClient.create();

    private static final Logger logger = LoggerFactory.getLogger(AbstractTokenService.class);

    protected final KeycloakProperties keycloakProperties;

    /**
     * Re-fetch the token once fewer than this many seconds remain before its expiry, so a cached token
     * is never used too close to (or past) {@code exp} mid-request. Independent of {@link #isTokenValid}
     * (which keeps its own 1-second margin for callers that cache externally, e.g. GraphDB).
     */
    private static final long REFRESH_MARGIN_SECONDS = 30;

    private volatile String cachedToken;

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

    /**
     * Returns a valid access token, reusing the in-memory cached one while it stays comfortably before
     * its expiry (see {@link #REFRESH_MARGIN_SECONDS}) and fetching a fresh one from Keycloak otherwise.
     * The service is a singleton shared across threads, hence the volatile field + double-checked
     * locking so concurrent callers trigger a single re-fetch.
     */
    @Override
    public String getAccessToken() {
        String token = cachedToken;
        if (isFresh(token)) {
            return token;
        }
        synchronized (this) {
            token = cachedToken;
            if (isFresh(token)) {
                return token;
            }
            token = requestNewToken();
            cachedToken = token;
            return token;
        }
    }

    @Override
    public void invalidate() {
        cachedToken = null;
    }

    /** True when {@code token} is a JWT whose expiry is more than {@link #REFRESH_MARGIN_SECONDS} away. */
    private boolean isFresh(String token) {
        if (token == null) {
            return false;
        }
        try {
            Date expiresAt = JWT.decode(token).getExpiresAt();
            return expiresAt != null
                && expiresAt.toInstant().isAfter(Instant.now().plusSeconds(REFRESH_MARGIN_SECONDS));
        } catch (JWTDecodeException e) {
            return false;
        }
    }

    private String requestNewToken() {
        KeycloakProperties.RealmConfig realmConfig = getRealmConfig();
        String tokenUrl = getTokenUrl();

        logger.debug("Requesting access token from Keycloak for realm: {}", realmConfig.name());

        if (tokenUrl == null || tokenUrl.isEmpty()) {
            logger.debug("Token URL is null or empty, configuration is missing");
            throw new MissingKeycloakConfigurationException();
        }

        logger.debug("Token URL: {}", tokenUrl);
        logger.debug("Client ID: {}", realmConfig.clientid());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", realmConfig.clientid());
        body.add("client_secret", realmConfig.clientsecret());

        try {
            logger.debug("Sending token request to Keycloak...");
            Token accessToken = keycloakClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(Token.class);

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
            Date expiresAt = jwt.getExpiresAt();
            if (expiresAt != null && expiresAt.after(now)) {
                isValid = true;
                logger.debug("Token is valid, expires at: {}", expiresAt);
            } else {
                logger.debug("Token has expired or has no expiry claim: {}", expiresAt);
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
