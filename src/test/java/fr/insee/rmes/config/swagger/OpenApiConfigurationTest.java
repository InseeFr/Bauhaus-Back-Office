package fr.insee.rmes.config.swagger;

import fr.insee.rmes.config.keycloak.KeycloakServerZoneConfiguration;
import fr.insee.rmes.config.keycloak.ServerZone;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigurationTest {

    @Test
    void shouldNotFindZone() {
        ServerZone serverZone = new ServerZone("TheZone");
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        zoneByServers.put("ZoneOne",serverZone);
        KeycloakServerZoneConfiguration KZC = new KeycloakServerZoneConfiguration();
        KZC.setZoneByServers(zoneByServers);
    }
}