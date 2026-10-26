package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.keycloak.TokenService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class ColecticaRepositoryConfigurationTest {

    private static final ColecticaConfiguration COLECTICA_CONFIGURATION = new ColecticaConfiguration(
            List.of("fr-FR"),
            new ColecticaConfiguration.ColecticaInstanceConfiguration(
                    "https://example.com",
                    "/api/v1/",
                    Collections.emptyMap(),
                    "resp",
                    "format",
                    "password",
                    "user",
                    "pass",
                    "fr.insee",
                    null,
                    null),
            null,
            null);

    @Test
    void primaryDDIRepository_usesTheSearchRowsAndMutualizedCodeListContentsCacheRegions() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        DDIRepository repository = new ColecticaRepositoryConfiguration()
                .primaryDDIRepository(
                        COLECTICA_CONFIGURATION,
                        mock(DDI3toDDI4ConverterService.class),
                        mock(DDI4toDDI3ConverterService.class),
                        mock(ColecticaClient.class),
                        mock(MutualizedCodeListRefsStrategy.class),
                        cacheManager);

        assertThat(repository).isInstanceOf(DDIRepositoryImpl.class);
        assertThat(cacheManager.getCacheNames())
                .containsExactlyInAnyOrder(
                        ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS,
                        ColecticaCacheNames.MUTUALIZED_CODE_LIST_CONTENTS);
    }

    @Test
    void colecticaClient_givesUpOnceTheReadTimeoutIsReached_whenColecticaNeverAnswers() throws IOException {
        // The OS completes the TCP handshake for a bound socket even if nobody calls accept():
        // the connection opens, the request is sent, and no answer ever comes back.
        try (var silentColectica = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            var configuration = new ColecticaConfiguration(
                    List.of("fr-FR"),
                    new ColecticaConfiguration.ColecticaInstanceConfiguration(
                            "http://localhost:" + silentColectica.getLocalPort(),
                            "/api/v1/",
                            Collections.emptyMap(),
                            "resp",
                            "format",
                            "password",
                            "user",
                            "pass",
                            "fr.insee",
                            Duration.ofSeconds(1),
                            Duration.ofMillis(200)),
                    null,
                    null);
            ColecticaClient client =
                    new ColecticaRepositoryConfiguration().colecticaClient(configuration, mock(TokenService.class));

            assertTimeoutPreemptively(
                    Duration.ofSeconds(5),
                    () -> assertThatThrownBy(client::ping).hasRootCauseInstanceOf(SocketTimeoutException.class));
        }
    }

    @Test
    void instanceConfiguration_appliesDefaultTimeouts_whenNoneAreConfigured() {
        var server = COLECTICA_CONFIGURATION.server();

        assertThat(server.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(server.readTimeout()).isEqualTo(Duration.ofMinutes(2));
    }
}
