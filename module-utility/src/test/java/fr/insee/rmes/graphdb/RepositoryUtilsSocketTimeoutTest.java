package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.keycloak.TokenService;
import java.net.ServerSocket;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Un triplestore qui accepte la connexion puis se tait ne doit pas bloquer le thread de la requête : sans délai de
 * socket, RDF4J attend dix jours.
 */
class RepositoryUtilsSocketTimeoutTest {

    private static final String QUERY = "SELECT ?s WHERE { ?s ?p ?o }";

    @Test
    void gives_up_on_a_triplestore_that_never_answers() throws Exception {
        // Le noyau accepte les connexions dans la file d'attente du socket, sans qu'aucun accept() ne réponde.
        try (var silentServer = new ServerSocket(0)) {
            var repositoryUtils =
                    new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED, null, Duration.ofMillis(200));

            assertGivesUp(repositoryUtils, silentServer);
        }
    }

    @Test
    void reads_the_socket_timeout_from_the_configuration() throws Exception {
        try (var silentServer = new ServerSocket(0)) {
            new ApplicationContextRunner()
                    // SpringApplication pose ce service de conversion, qui lit « 200ms » comme une Duration.
                    .withInitializer(context -> context.getBeanFactory()
                            .setConversionService(ApplicationConversionService.getSharedInstance()))
                    .withPropertyValues(
                            "fr.insee.rmes.bauhaus.rdf.auth=DISABLED",
                            RepositoryUtils.SOCKET_TIMEOUT_PROPERTY + "=200ms")
                    .withBean(TokenService.class, () -> mock(TokenService.class))
                    .withBean(RepositoryUtils.class)
                    .run(context -> assertGivesUp(context.getBean(RepositoryUtils.class), silentServer));
        }
    }

    private static void assertGivesUp(RepositoryUtils repositoryUtils, ServerSocket silentServer) {
        var repository = repositoryUtils.initRepository("http://localhost:" + silentServer.getLocalPort(), "gestion");

        assertTimeoutPreemptively(
                Duration.ofSeconds(10),
                () -> assertThatThrownBy(() -> repositoryUtils.getResponse(QUERY, repository))
                        .isInstanceOf(RmesException.class));
    }
}
