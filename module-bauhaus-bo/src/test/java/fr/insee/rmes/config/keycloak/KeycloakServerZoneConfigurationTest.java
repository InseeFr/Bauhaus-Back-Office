package fr.insee.rmes.config.keycloak;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class KeycloakServerZoneConfigurationTest {

    @Test
    public void shouldCompareTwoServerZoneConfigurations(){

        Map<String, ServerZone> map = new HashMap<>();
        map.put("exampleOne",ServerZone.defaultZone());
        map.put("exampleTwo",ServerZone.defaultZone());

        KeycloakServerZoneConfiguration firstKeycloakServerZoneConfiguration = new KeycloakServerZoneConfiguration();
        firstKeycloakServerZoneConfiguration.setZoneByServers(map);

        KeycloakServerZoneConfiguration secondKeycloakServerZoneConfiguration = new KeycloakServerZoneConfiguration();
        secondKeycloakServerZoneConfiguration.setZoneByServers(map);

        assertEquals(firstKeycloakServerZoneConfiguration.rdfserver(),secondKeycloakServerZoneConfiguration.rdfserver());

    }
}