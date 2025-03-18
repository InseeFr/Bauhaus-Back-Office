package fr.insee.rmes.config.keycloak;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakServerZoneConfigurationTest {

    @Test
    void shouldNotFindZone() {
        ServerZone serverZone = new ServerZone("TheZone");
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        zoneByServers.put("ZoneOne",serverZone);
        KeycloakServerZoneConfiguration KZC = new KeycloakServerZoneConfiguration();
        KZC.setZoneByServers(zoneByServers);
    }

}