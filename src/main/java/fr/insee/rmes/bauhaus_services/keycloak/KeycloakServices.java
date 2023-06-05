package fr.insee.rmes.bauhaus_services.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiatorWithAuthent;
import fr.insee.rmes.config.keycloak.KeycloakServer;
import fr.insee.rmes.config.keycloak.KeycloakServerZoneConfiguration;
import fr.insee.rmes.config.keycloak.ServerZone;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.time.Instant;
import java.time.LocalDateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class KeycloakServices {

    private final Map<String, ServerZone> zonesByServer;
    private final Map<String, ServerZone> zonesByUrl=new HashMap<>();
    private RestTemplate keycloakClient = new RestTemplate();
    static final Logger log = LogManager.getLogger(KeycloakServices.class);
    private final Map<ServerZone.Zone, KeycloakServer> keycloakServers;

    static final Logger logger = LogManager.getLogger(KeycloakServices.class);

    protected KeycloakServices(){
        zonesByServer=Map.of();
        keycloakServers=Map.of();
    }

    public KeycloakServices(
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.secret}") String secret,
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.id}") String clientID,
            @Value("${fr.insee.rmes.bauhaus.auth-server-url}") String serverKeycloak,
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.dmz.secret}") String secretDmz,
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.dmz.id}") String clientDmzId,
            @Value("${fr.insee.rmes.bauhaus.dmz.auth-server-url}") String serverKeycloakDmz,
            KeycloakServerZoneConfiguration keycloakServerZoneConfiguration) {
        keycloakServers = Map.of(ServerZone.Zone.INTERNE,
                new KeycloakServer(secret, clientID, serverKeycloak),
                ServerZone.Zone.DMZ,
                new KeycloakServer(secretDmz, clientDmzId, serverKeycloakDmz)
        );
        this.zonesByServer=keycloakServerZoneConfiguration.zonesByServer();
    }

    /**
     * Permet de récuperer un jeton keycloak
     *
     * @return jeton
     */
    public String getKeycloakAccessToken(String rdfServerUrl) throws RmesException {

        if (keycloakServers == null) {
            throw new RmesException(500, "Unable to retrieve token : some propertis provided to connect to keycloak servers or invalid", "");
        }

        var zone=findZoneForKeycloakForRdfServer(rdfServerUrl);

        log.debug("GET Keycloak access token pour " + zone);

        KeycloakServer keycloakServer = keycloakServers.get(zone.serverZone());

        String keycloakUrl = keycloakServer.server() + "/protocol/openid-connect/token";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", keycloakServer.clientId());
        body.add("client_secret", keycloakServer.secret());

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {

            Token accessToken = keycloakClient.postForObject(keycloakUrl, entity, Token.class);

            log.trace("Keycloak token provided");
            return accessToken.getAccessToken();

        } catch (RestClientException e) {
            log.warn(e.getMessage());
            throw new RmesException(404, 0, "Le serveur Keycloak est injoignable");
        }

    }

    private ServerZone findZoneForKeycloakForRdfServer(String rdfServerUrl) {
        var retour=zonesByUrl.get(rdfServerUrl);
        if (retour!=null){
            return retour;
        }
        return findZoneFromUrlAndStoreIt(rdfServerUrl);

    }

    public ServerZone findZoneFromUrlAndStoreIt(String url) {
        var retour= zonesByServer.entrySet().stream()
                .filter(entry->url.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findAny();
        if (retour.isEmpty()){
            logger.warn("Unable to find server zone for url "+url+" : default zone used");
            retour= Optional.of(ServerZone.defaultZone());
        }
        zonesByUrl.put(url, retour.get());
        return retour.get();
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
        var now = nowPlus1Second();
        try {
            DecodedJWT jwt = JWT.decode(token);
            if (jwt.getExpiresAt().after(now)) {
                isValid = true;
            }
        } catch (JWTDecodeException exception) {
            log.error("erreur {}", exception.toString());

        }

        log.trace("Token is valid : {}", isValid);
        return isValid;
    }

    protected Date nowPlus1Second() {
        return Date.from((new Date()).toInstant().plusSeconds(1));
    }
}