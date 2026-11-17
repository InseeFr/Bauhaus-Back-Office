package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.insee.rmes.keycloak.TokenService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Les identifiants de Fuseki sont lus par Spring, comme dans l'application. Le faux serveur répond comme Fuseki : un
 * défi {@code 401} tant que la requête n'est pas authentifiée.
 */
class RepositoryUtilsBasicAuthTest {

    private static final String ASK = "ASK { ?s ?p ?o }";

    private final List<String> authorizations = new CopyOnWriteArrayList<>();

    private HttpServer fuseki;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("fr.insee.rmes.bauhaus.rdf.auth=DISABLED", "fr.insee.rmes.rdf.backend=fuseki")
            .withBean(TokenService.class, () -> mock(TokenService.class))
            .withBean(RepositoryUtils.class);

    @BeforeEach
    void startFuseki() throws IOException {
        fuseki = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        fuseki.createContext("/bauhaus/sparql", this::answer);
        fuseki.start();
    }

    @AfterEach
    void stopFuseki() {
        fuseki.stop(0);
    }

    @Test
    void sends_the_configured_credentials_to_fuseki() {
        contextRunner
                .withPropertyValues(
                        RdfBasicCredentials.USERNAME_PROPERTY + "=bauhaus",
                        RdfBasicCredentials.PASSWORD_PROPERTY + "=secret")
                .run(context -> {
                    var repositoryUtils = context.getBean(RepositoryUtils.class);
                    var repository = repositoryUtils.initRepository(fusekiUrl(), "bauhaus");

                    assertThat(repositoryUtils.getResponseForAskQuery(ASK, repository))
                            .isTrue();
                    assertThat(authorizations).contains(basic("bauhaus", "secret"));
                });
    }

    @Test
    void refuses_to_start_with_a_username_but_no_password() {
        contextRunner
                .withPropertyValues(RdfBasicCredentials.USERNAME_PROPERTY + "=bauhaus")
                .run(context -> assertThat(context)
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining(RdfBasicCredentials.PASSWORD_PROPERTY));
    }

    @Test
    void refuses_to_start_with_a_password_but_no_username() {
        contextRunner
                .withPropertyValues(RdfBasicCredentials.PASSWORD_PROPERTY + "=secret")
                .run(context -> assertThat(context)
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining(RdfBasicCredentials.USERNAME_PROPERTY));
    }

    @Test
    void refuses_to_start_with_basic_credentials_on_graphdb() {
        contextRunner
                .withPropertyValues(
                        "fr.insee.rmes.rdf.backend=graphdb",
                        RdfBasicCredentials.USERNAME_PROPERTY + "=bauhaus",
                        RdfBasicCredentials.PASSWORD_PROPERTY + "=secret")
                .run(context -> assertThat(context)
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining("fr.insee.rmes.rdf.backend=graphdb")
                        .hasMessageContaining(RdfBasicCredentials.USERNAME_PROPERTY));
    }

    private void answer(HttpExchange exchange) throws IOException {
        exchange.getRequestBody().readAllBytes();
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization == null) {
            exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"fuseki\"");
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
            return;
        }
        authorizations.add(authorization);
        byte[] body = "{\"head\":{},\"boolean\":true}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/sparql-results+json");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private String fusekiUrl() {
        return "http://localhost:" + fuseki.getAddress().getPort();
    }

    private static String basic(String username, String password) {
        return "Basic "
                + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
