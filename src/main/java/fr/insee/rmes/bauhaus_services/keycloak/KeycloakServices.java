package fr.insee.rmes.bauhaus_services.keycloak;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonObject;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    private String clientId;
    private String server;
    static final Logger log = LogManager.getLogger(KeycloakServices.class);

    @PostConstruct
    private void init() {
        secret = config.getSecret();
        server = config.getServerKeycloak();
        clientId= config.getClientId();
    }

    /**
     * Permet de récuperer un jeton keycloak
     *
     * @return jeton
     */
    public String getKeycloakAccessToken() throws RmesException {

        log.debug("GET Keycloak access token");

        var keycloakClient = new RestTemplate();
        String keycloakUrl = server + "/protocol/openid-connect/token";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "client_credentials");
        body.add("client_id",clientId);
        body.add("client_secret",secret);


        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {

            token accessToken = keycloakClient.postForObject( keycloakUrl, entity , token.class );

            log.trace("Keycloak token provided");
            return accessToken.getAccessToken();


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