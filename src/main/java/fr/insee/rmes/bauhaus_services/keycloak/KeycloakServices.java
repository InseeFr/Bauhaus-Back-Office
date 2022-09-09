package fr.insee.rmes.bauhaus_services.keycloak;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonObject;


import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;


@Service


public class KeycloakServices {

    @Autowired
    private Config config;

    private String secret;
    private String resource;
    private String server;
    static final Logger log = LogManager.getLogger(KeycloakServices.class);

    @PostConstruct
    private void init() {
        secret = config.getSecret();
        resource = config.getResource();
        server = config.getServerKeycloak();
    }

    /**
     * Permet de récuperer un jeton keycloak
     *
     * @return jeton
     */
    public String getKeycloakAccessToken() throws RmesException {

        log.debug("GET Keycloak access token");

        var authString = resource + ":" + secret;

        byte[] authBytes = authString.getBytes(StandardCharsets.UTF_8);
        var encodedAuthString = Base64.getEncoder().encodeToString(authBytes);

        var keycloakClient = new RestTemplate();
        String keycloakUrl = server + "/protocol/openid-connect/token";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuthString);

        var request = "grant_type=client_credentials";

        HttpEntity<String> entity = new HttpEntity<>(request, headers);
        try {
            String result = keycloakClient.postForObject(keycloakUrl, entity, String.class);

            JsonObject accessToken= Json.createParser(new StringReader(result)).getObject();

            log.trace("Keycloak token provided");

            return accessToken.getString("access_token");

        } catch (RestClientException e) {
            log.warn(e.getMessage());
            throw new RmesException (404,0,"Le serveur Keycloak est injoignable");
        }

    }

    /**
     * Verifie si le jeton keycloak a expiré
     *
     * @param token
     * @return boolean
     */
    public boolean isTokenValid(String token) {
        if (token == null) {
            return false;
        }
        var isValid = false;
        var now = new Date();
        try {
            DecodedJWT jwt = JWT.decode(token);
            if (jwt.getExpiresAt().after(now)) {
                isValid = true;
            }
        } catch (JWTDecodeException exception) {
            log.error("erreur" + exception.toString());

        }

        log.trace("Token is valid : " + isValid);
        return isValid;
    }
}