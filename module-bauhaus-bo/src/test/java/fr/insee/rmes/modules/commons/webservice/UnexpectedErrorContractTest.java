package fr.insee.rmes.modules.commons.webservice;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.server.ResponseStatusException;

/**
 * Filet des erreurs imprévues (ticket 1 de l'audit #1264, ADR-1264) : toute erreur sort avec son
 * statut et un corps {@code {message, code}}, sans détail technique.
 * <p>
 * Monté sur un vrai serveur ({@code RANDOM_PORT}) et non sur MockMvc, qui ne passe ni par la
 * chaîne de sécurité complète ni par le dispatch d'erreur vers {@code /error}. Le contexte de
 * test tourne en mode DEV ({@code env: NoAuth}), celui où l'erreur se déguisait en 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(UnexpectedErrorContractTest.FailingResources.class)
@ExtendWith(OutputCaptureExtension.class)
class UnexpectedErrorContractTest {

    private static final String TECHNICAL_DETAIL = "SELECT ?s WHERE { ?s ?p ?o } failed on repository";

    @RestController
    @RequestMapping("/test-errors")
    static class FailingResources {

        @GetMapping("/unexpected")
        String unexpected() {
            throw new IllegalStateException(TECHNICAL_DETAIL);
        }

        @GetMapping("/server-status")
        String serverStatus() {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, TECHNICAL_DETAIL, new IllegalStateException(TECHNICAL_DETAIL));
        }

        @GetMapping("/not-found-status")
        String notFoundStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TECHNICAL_DETAIL);
        }

        @GetMapping("/send-error")
        void sendError(HttpServletResponse response) throws IOException {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }

        @GetMapping("/bad-request")
        String badRequest() {
            throw new RmesRuntimeBadRequestException("specific handler message");
        }
    }

    @LocalServerPort
    int serverPort;

    @Autowired
    ApplicationContext applicationContext;

    private RestClient restClient;

    @BeforeEach
    void createClient() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + serverPort + "/api")
                .defaultStatusHandler(status -> true, (request, response) -> {})
                .build();
    }

    private ResponseEntity<String> get(String path) {
        return restClient
                .get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);
    }

    private static void assertApiError(ResponseEntity<String> response, HttpStatus status) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON))
                .isTrue();
        JSONObject body = new JSONObject(response.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("message", "code");
        assertThat(body.getString("message")).isNotBlank();
        assertThat(body.getString("code")).isEqualTo(status.name());
        assertThat(response.getBody()).doesNotContain(TECHNICAL_DETAIL);
    }

    @Test
    void an_unexpected_exception_answers_500_with_a_generic_message(CapturedOutput output) {
        assertApiError(get("/test-errors/unexpected"), HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(output).contains(IllegalStateException.class.getName() + ": " + TECHNICAL_DETAIL);
    }

    @Test
    void a_response_status_exception_keeps_its_status_but_not_its_technical_reason(CapturedOutput output) {
        assertApiError(get("/test-errors/server-status"), HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(output).contains(IllegalStateException.class.getName() + ": " + TECHNICAL_DETAIL);
    }

    @Test
    void a_client_error_status_follows_the_same_contract() {
        assertApiError(get("/test-errors/not-found-status"), HttpStatus.NOT_FOUND);
    }

    @Test
    void a_spring_mvc_exception_follows_the_same_contract() {
        assertApiError(get("/no-such-endpoint"), HttpStatus.NOT_FOUND);
    }

    /** Le dispatch d'erreur vers {@code /error} ne doit plus être refusé par la sécurité (401 en DEV). */
    @Test
    void an_error_dispatched_to_the_error_page_keeps_its_status() {
        assertApiError(get("/test-errors/send-error"), HttpStatus.SERVICE_UNAVAILABLE);
    }

    /** Le filet passe après les gestionnaires spécialisés, il ne les court-circuite pas. */
    @Test
    void a_specialised_handler_still_wins_over_the_fallback() {
        ResponseEntity<String> response = get("/test-errors/bad-request");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("specific handler message");
    }

    /**
     * Spring consulte les advices dans l'ordre et s'arrête au premier qui sait traiter
     * l'exception : à ordre égal, leur rang n'est pas garanti et le filet sur {@link Exception}
     * pourrait masquer un gestionnaire spécialisé. Tout advice doit donc déclarer un ordre
     * strictement plus prioritaire que le filet.
     */
    @Test
    void every_other_advice_is_consulted_before_the_fallback() {
        List<String> notBeforeFallback = ControllerAdviceBean.findAnnotatedBeans(applicationContext).stream()
                .filter(advice -> advice.getBeanType() != UnexpectedErrorHandler.class)
                .filter(advice -> advice.getOrder() >= Ordered.LOWEST_PRECEDENCE)
                .map(advice -> advice.getBeanType().getName())
                .toList();

        assertThat(notBeforeFallback).isEmpty();
    }
}
