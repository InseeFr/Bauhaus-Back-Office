package fr.insee.rmes.keycloak;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class ColecticaKeycloakServiceTest extends AbstractTokenServiceTest<ColecticaKeycloakService> {

    @Override
    ColecticaKeycloakService createService(KeycloakProperties properties) {
        return new ColecticaKeycloakService(properties);
    }

    @Test
    void getAccessToken_shouldCallKeycloakServerWithColecticaRealm() {
        service.getAccessToken();
        verify(requestBodyUriSpec).uri("keycloak.test/realms/colectica-realm/protocol/openid-connect/token");
    }

    @Test
    void getRealmConfig_shouldReturnColecticaRealm() {
        var realmConfig = service.getRealmConfig();
        assertEquals("colectica-realm", realmConfig.name());
        assertEquals("colectica-client", realmConfig.clientid());
    }

    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        ColecticaKeycloakService serviceWithNullServer =
                new ColecticaKeycloakService(KeycloakTestFixtures.propertiesWithoutServer());

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }
}
