package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import fr.insee.rmes.config.keycloak.KeycloakServer;
import fr.insee.rmes.config.keycloak.KeycloakServerZoneConfiguration;
import fr.insee.rmes.config.keycloak.ServerZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Primary
public class KeycloakServicesStub extends KeycloakServices {


    public KeycloakServicesStub(
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.secret}") String secret,
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.id}") String clientID,
            @Value("${fr.insee.rmes.bauhaus.auth-server-url}") String serverKeycloak,
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.dmz.secret}") String secretDmz,
            @Value("${fr.insee.rmes.bauhaus.keycloak.client.dmz.id}") String clientDmzId,
            @Value("${fr.insee.rmes.bauhaus.dmz.auth-server-url}") String serverKeycloakDmz,
            KeycloakServerZoneConfiguration keycloakServerZoneConfiguration) {
        super(secret, clientID, serverKeycloak, secretDmz, clientDmzId, serverKeycloakDmz, keycloakServerZoneConfiguration);

    }

    public void setRestTemplate(RestTemplate restTemplate){
        super.keycloakClient=restTemplate;
    }

}
