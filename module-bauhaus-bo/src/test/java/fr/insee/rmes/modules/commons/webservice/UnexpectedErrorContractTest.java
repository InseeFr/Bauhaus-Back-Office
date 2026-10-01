package fr.insee.rmes.modules.commons.webservice;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.exceptions.DatabaseQueryException;
import fr.insee.rmes.graphdb.exceptions.GraphDbUnauthorizedException;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.NotFoundAttributeException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.query.MalformedQueryException;
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

        @GetMapping("/database-query")
        String databaseQuery() throws DatabaseQueryException {
            throw new DatabaseQueryException(new MalformedQueryException(TECHNICAL_DETAIL), TECHNICAL_DETAIL);
        }

        @GetMapping("/graphdb-unauthorized")
        String graphDbUnauthorized() throws DatabaseQueryException {
            throw new GraphDbUnauthorizedException(
                    new UnauthorizedException(), TECHNICAL_DETAIL, RepositoryInitiator.Type.DISABLED);
        }

        @GetMapping("/generic-internal")
        String genericInternal() throws GenericInternalServerException {
            throw new GenericInternalServerException("{\"message\":\"" + TECHNICAL_DETAIL + "\"}");
        }

        @GetMapping("/attribute-not-found")
        String attributeNotFound() throws NotFoundAttributeException {
            throw new NotFoundAttributeException("I.1.1");
        }

        @GetMapping("/rubric-without-range")
        String rubricWithoutRange() throws OperationDocumentationRubricWithoutRangeException {
            throw new OperationDocumentationRubricWithoutRangeException("I.6.4");
        }

        @GetMapping("/no-such-file")
        String noSuchFile() throws NoSuchFileException {
            throw new NoSuchFileException("/var/bauhaus/storage/" + TECHNICAL_DETAIL);
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

    private static String messageOf(ResponseEntity<String> response) {
        return new JSONObject(response.getBody()).getString("message");
    }

    private static void assertApiError(ResponseEntity<String> response, HttpStatus status) {
        assertApiError(response, status, status.name());
    }

    private static void assertApiError(ResponseEntity<String> response, HttpStatus status, String code) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON))
                .isTrue();
        ApiErrorContract.assertApiError(response.getBody());
        assertThat(new JSONObject(response.getBody()).getString("code")).isEqualTo(code);
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

        assertApiError(response, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        assertThat(messageOf(response)).isEqualTo("specific handler message");
    }

    @Test
    void a_failed_rdf_query_answers_a_coded_error_without_the_query() {
        assertApiError(get("/test-errors/database-query"), HttpStatus.INTERNAL_SERVER_ERROR, "RDF_QUERY_FAILED");
    }

    /** Diagnostic de configuration écrit pour l'exploitant : il reste le message de l'erreur. */
    @Test
    void a_graphdb_401_answers_its_configuration_diagnostic() {
        ResponseEntity<String> response = get("/test-errors/graphdb-unauthorized");

        assertApiError(response, HttpStatus.INTERNAL_SERVER_ERROR, "RDF_AUTHENTICATION_FAILED");
        assertThat(messageOf(response)).contains("fr.insee.rmes.bauhaus.rdf.auth=DISABLED");
    }

    @Test
    void a_generic_internal_server_exception_does_not_relay_its_details() {
        assertApiError(get("/test-errors/generic-internal"), HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }

    @Test
    void an_unknown_msd_attribute_answers_404_naming_the_attribute() {
        ResponseEntity<String> response = get("/test-errors/attribute-not-found");

        assertApiError(response, HttpStatus.NOT_FOUND, "NOT_FOUND");
        assertThat(messageOf(response)).contains("I.1.1");
    }

    @Test
    void a_rubric_without_range_answers_400_naming_the_rubric() {
        ResponseEntity<String> response = get("/test-errors/rubric-without-range");

        assertApiError(response, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        assertThat(messageOf(response)).contains("I.6.4");
    }

    @Test
    void a_missing_file_answers_404_without_its_storage_path() {
        ResponseEntity<String> response = get("/test-errors/no-such-file");

        assertApiError(response, HttpStatus.NOT_FOUND, "NOT_FOUND");
        assertThat(response.getBody()).doesNotContain("/var/bauhaus/storage");
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
