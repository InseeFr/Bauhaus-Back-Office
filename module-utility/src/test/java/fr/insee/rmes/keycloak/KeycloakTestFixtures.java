package fr.insee.rmes.keycloak;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.springframework.web.client.RestClient;

/** What {@link KeycloakServiceTest} and {@link ColecticaKeycloakServiceTest} set up alike. */
final class KeycloakTestFixtures {

    private static final KeycloakProperties.RealmConfig DEFAULT_REALM =
            new KeycloakProperties.RealmConfig("default-realm", "default-client", "default-secret");
    private static final KeycloakProperties.RealmConfig COLECTICA_REALM =
            new KeycloakProperties.RealmConfig("colectica-realm", "colectica-client", "colectica-secret");

    private KeycloakTestFixtures() {}

    static KeycloakProperties properties() {
        return new KeycloakProperties(new KeycloakProperties.Server("keycloak.test"), DEFAULT_REALM, COLECTICA_REALM);
    }

    static KeycloakProperties propertiesWithoutServer() {
        return new KeycloakProperties(null, DEFAULT_REALM, COLECTICA_REALM);
    }

    static Token token(String accessToken) {
        return new Token() {
            @Override
            public String getAccessToken() {
                return accessToken;
            }
        };
    }

    /** Routes {@code client.post().uri(..).retrieve()} to {@code responseSpec}. */
    static void stubTokenRequest(
            RestClient client,
            RestClient.RequestBodyUriSpec requestBodyUriSpec,
            RestClient.RequestBodySpec requestBodySpec,
            RestClient.ResponseSpec responseSpec) {
        when(client.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }
}
