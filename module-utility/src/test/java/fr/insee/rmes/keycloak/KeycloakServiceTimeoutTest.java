package fr.insee.rmes.keycloak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class KeycloakServiceTimeoutTest {

    @Test
    void getAccessToken_givesUpOnceTheReadTimeoutIsReached_whenKeycloakNeverAnswers() throws IOException {
        // The OS completes the TCP handshake for a bound socket even if nobody calls accept():
        // the connection opens, the request is sent, and no answer ever comes back.
        try (var silentKeycloak = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            var properties = new KeycloakProperties(
                    new KeycloakProperties.Server(
                            "http://localhost:" + silentKeycloak.getLocalPort(),
                            Duration.ofSeconds(1),
                            Duration.ofMillis(200)),
                    new KeycloakProperties.RealmConfig("default-realm", "default-client", "default-secret"),
                    new KeycloakProperties.RealmConfig("colectica-realm", "colectica-client", "colectica-secret"));
            var keycloakService = new KeycloakService(properties);

            var exception = assertTimeoutPreemptively(
                    Duration.ofSeconds(5),
                    () -> assertThrows(UnreachableKeycloakException.class, keycloakService::getAccessToken));

            assertThat(exception).hasRootCauseInstanceOf(SocketTimeoutException.class);
        }
    }

    @Test
    void server_appliesDefaultTimeouts_whenNoneAreConfigured() {
        var server = new KeycloakProperties.Server("http://keycloak.test", null, null);

        assertThat(server.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(server.readTimeout()).isEqualTo(Duration.ofSeconds(10));
    }
}
