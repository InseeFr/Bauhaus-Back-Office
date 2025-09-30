package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakServerZoneConfigurationTest {

    @Test
    void shouldCreateEmptyConfiguration() {
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration();
        assertNull(config.rdfserver());
    }

    @Test
    void shouldCreateConfigurationWithMap() {
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        ServerZone interneZone = new ServerZone("INTERNE");
        ServerZone dmzZone = new ServerZone("DMZ");
        
        zoneByServers.put("server1.insee.fr", interneZone);
        zoneByServers.put("server2.insee.fr", dmzZone);
        
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(zoneByServers);
        
        assertEquals(zoneByServers, config.rdfserver());
        assertEquals(2, config.rdfserver().size());
    }

    @Test
    void shouldSetZoneByServers() {
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration();
        
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        ServerZone interneZone = new ServerZone("INTERNE");
        zoneByServers.put("test.server.fr", interneZone);
        
        config.setZoneByServers(zoneByServers);
        
        assertEquals(zoneByServers, config.rdfserver());
    }

    @Test
    void shouldHandleNullMap() {
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(null);
        assertNull(config.rdfserver());
    }

    @Test
    void shouldHandleEmptyMap() {
        Map<String, ServerZone> emptyMap = new HashMap<>();
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(emptyMap);
        
        assertEquals(emptyMap, config.rdfserver());
        assertTrue(config.rdfserver().isEmpty());
    }

    @Test
    void shouldOverwriteExistingConfiguration() {
        Map<String, ServerZone> initialMap = new HashMap<>();
        initialMap.put("initial.server.fr", new ServerZone("INTERNE"));
        
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(initialMap);
        assertEquals(1, config.rdfserver().size());
        
        Map<String, ServerZone> newMap = new HashMap<>();
        newMap.put("new.server.fr", new ServerZone("DMZ"));
        newMap.put("another.server.fr", new ServerZone("INTERNE"));
        
        config.setZoneByServers(newMap);
        assertEquals(2, config.rdfserver().size());
        assertFalse(config.rdfserver().containsKey("initial.server.fr"));
        assertTrue(config.rdfserver().containsKey("new.server.fr"));
        assertTrue(config.rdfserver().containsKey("another.server.fr"));
    }

    @Test
    void shouldPreserveServerZoneObjects() {
        ServerZone customZone = new ServerZone("DMZ");
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        zoneByServers.put("custom.server.fr", customZone);
        
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(zoneByServers);
        
        ServerZone retrievedZone = config.rdfserver().get("custom.server.fr");
        assertSame(customZone, retrievedZone);
        assertEquals(ServerZone.Zone.DMZ, retrievedZone.zone());
    }

    @Test
    void shouldAllowMultipleServersWithSameZone() {
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        ServerZone interneZone1 = new ServerZone("INTERNE");
        ServerZone interneZone2 = new ServerZone("INTERNE");
        
        zoneByServers.put("server1.insee.fr", interneZone1);
        zoneByServers.put("server2.insee.fr", interneZone2);
        
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(zoneByServers);
        
        assertEquals(2, config.rdfserver().size());
        assertEquals(ServerZone.Zone.INTERNE, config.rdfserver().get("server1.insee.fr").zone());
        assertEquals(ServerZone.Zone.INTERNE, config.rdfserver().get("server2.insee.fr").zone());
    }

    @Test
    void shouldHandleSpecialServerNames() {
        Map<String, ServerZone> zoneByServers = new HashMap<>();
        zoneByServers.put("server-with-dashes.com", new ServerZone("INTERNE"));
        zoneByServers.put("server_with_underscores.com", new ServerZone("DMZ"));
        zoneByServers.put("192.168.1.100", new ServerZone("INTERNE"));
        zoneByServers.put("localhost:8080", new ServerZone("DMZ"));
        
        KeycloakServerZoneConfiguration config = new KeycloakServerZoneConfiguration(zoneByServers);
        
        assertEquals(4, config.rdfserver().size());
        assertTrue(config.rdfserver().containsKey("server-with-dashes.com"));
        assertTrue(config.rdfserver().containsKey("server_with_underscores.com"));
        assertTrue(config.rdfserver().containsKey("192.168.1.100"));
        assertTrue(config.rdfserver().containsKey("localhost:8080"));
    }
}