package fr.insee.rmes.modules.codeslists.codeslists;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/**
 * Parcours complet d'une liste de codes, du POST jusqu'à l'ajout d'un code, contre un vrai
 * triplestore.
 * <p>
 * Ce que ce test verrouille et qu'aucun test de contrôleur ne voit :
 * <ul>
 *     <li>une liste créée par l'API accepte immédiatement un ajout de code — c'est-à-dire que
 *     {@code lastCodeUriSegment} a bien été écrit. Sans lui, l'écriture d'un code partait en 500 ;</li>
 *     <li>la date de création survit à une mise à jour, et l'état de publication d'une liste
 *     publiée devient « Modified », pas « Unpublished » ;</li>
 *     <li>l'unicité, la cohérence id url / id corps et le 404 sur liste inconnue tiennent encore
 *     après le passage à l'architecture hexagonale.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CodesListsEndToEndTest extends WithGraphDBContainer {

    private static final String BAUHAUS_TEST_PUBLICATION_REPOSITORY = "bauhaus-test-pub";

    /**
     * Corps complet. Les huit champs sont ceux que le front exige déjà et que le record
     * {@code CodesListRequest} rend obligatoires côté back.
     */
    private static final String CODES_LIST_BODY = """
            {
              "id": "%1$s",
              "labelLg1": "Liste %1$s",
              "labelLg2": "List %1$s",
              "creator": "http://bauhaus/HIE000000",
              "contributor": ["http://bauhaus/HIE000000"],
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Public",
              "lastListUriSegment": "%2$s",
              "lastClassUriSegment": "%3$s",
              "lastCodeUriSegment": "%2$s-code"
            }""";

    @LocalServerPort
    int serverPort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("/testcontainers").withRepository("config-pub.ttl");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> BAUHAUS_TEST_PUBLICATION_REPOSITORY);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.baseURI", () -> "http://id.insee.fr/");
    }

    private String codesListsEndpoint() {
        return "http://localhost:" + serverPort + "/api/codeList";
    }

    private static String body(String id, String uriSegment, String classSegment) {
        return CODES_LIST_BODY.formatted(id, uriSegment, classSegment);
    }

    private String create(String id, String uriSegment, String classSegment) {
        var response = RestClient.create()
                .post()
                .uri(codesListsEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .body(body(id, uriSegment, classSegment))
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private JSONObject fetchDetailed(String id) {
        return new JSONObject(RestClient.create()
                .get()
                .uri(codesListsEndpoint() + "/detailed/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class));
    }

    @Test
    @Order(1)
    @DisplayName("Create a codes list, read it back, add a code to it, then update it")
    void ok_when_codes_list_created_then_a_code_is_added() {
        String id = create("CL_E2E", "cl-e2e", "ClE2e");
        assertThat(id).isEqualTo("CL_E2E");

        JSONObject created = fetchDetailed("CL_E2E");
        assertThat(created.getString("labelLg1")).isEqualTo("Liste CL_E2E");
        assertThat(created.getString("labelLg2")).isEqualTo("List CL_E2E");
        assertThat(created.getString("creator")).isEqualTo("http://bauhaus/HIE000000");
        assertThat(created.getString("disseminationStatus"))
                .isEqualTo("http://id.insee.fr/codes/base/statutDiffusion/Public");
        assertThat(created.getString("validationState")).isEqualTo("Unpublished");
        assertThat(created.getString("lastCodeUriSegment")).isEqualTo("cl-e2e-code");
        assertThat(created.getString("lastClassUriSegment")).isEqualTo("ClE2e");
        assertThat(created.getString("lastListUriSegment")).isEqualTo("cl-e2e");
        String createdDate = created.getString("created");
        assertThat(createdDate).isNotBlank();

        // Le point qui motive la validation de lastCodeUriSegment : sans lui, ce POST partait en 500.
        var codeResponse = RestClient.create()
                .post()
                .uri(codesListsEndpoint() + "/detailed/CL_E2E/codes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body("""
                        {"code":"A","labelLg1":"Premier code","labelLg2":"First code"}""")
                .retrieve()
                .toEntity(String.class);
        assertThat(codeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String codes = RestClient.create()
                .get()
                .uri(codesListsEndpoint() + "/detailed/CL_E2E/codes?page=1")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        assertThat(new JSONObject(codes).getJSONArray("items").getJSONObject(0).getString("code"))
                .isEqualTo("A");

        var updateResponse = RestClient.create()
                .put()
                .uri(codesListsEndpoint() + "/CL_E2E")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .body(body("CL_E2E", "cl-e2e", "ClE2e").replace("Liste CL_E2E", "Liste CL_E2E v2"))
                .retrieve()
                .toEntity(String.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JSONObject updated = fetchDetailed("CL_E2E");
        assertThat(updated.getString("labelLg1")).isEqualTo("Liste CL_E2E v2");
        // La date de création n'appartient pas au client : elle est relue en base, pas dans le corps.
        assertThat(updated.getString("created")).isEqualTo(createdDate);
    }

    @Test
    @Order(2)
    @DisplayName("PUT with an id that does not match the url returns 400")
    void bad_request_when_the_body_id_does_not_match_the_url() {
        create("CL_MISMATCH", "cl-mismatch", "ClMismatch");

        RestClient.create()
                .put()
                .uri(codesListsEndpoint() + "/CL_MISMATCH")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("CL_OTHER", "cl-mismatch", "ClMismatch"))
                .retrieve()
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .toBodilessEntity();
    }

    @Test
    @Order(3)
    @DisplayName("PUT on a codes list that does not exist returns 404")
    void not_found_when_the_codes_list_does_not_exist() {
        RestClient.create()
                .put()
                .uri(codesListsEndpoint() + "/CL_UNKNOWN")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("CL_UNKNOWN", "cl-unknown", "ClUnknown"))
                .retrieve()
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .toBodilessEntity();
    }

    @Test
    @Order(4)
    @DisplayName("POST with an identifier already taken returns 400")
    void bad_request_when_the_identifier_is_already_taken() {
        create("CL_UNIQUE", "cl-unique", "ClUnique");

        RestClient.create()
                .post()
                .uri(codesListsEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("CL_UNIQUE", "cl-unique-bis", "ClUniqueBis"))
                .retrieve()
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .toBodilessEntity();
    }

    @Test
    @Order(5)
    @DisplayName("POST with a blank mandatory field returns 400 without touching the repository")
    void bad_request_when_a_mandatory_field_is_blank() {
        RestClient.create()
                .post()
                .uri(codesListsEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body("CL_BLANK", "cl-blank", "ClBlank").replace("\"cl-blank-code\"", "\"   \""))
                .retrieve()
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .toBodilessEntity();

        RestClient.create()
                .get()
                .uri(codesListsEndpoint() + "/detailed/CL_BLANK")
                .retrieve()
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .toBodilessEntity();
    }

    @Test
    @Order(6)
    @DisplayName("Updating a published codes list moves it to Modified, not back to Unpublished")
    void modified_when_a_published_codes_list_is_updated() {
        create("CL_PUBLISHED", "cl-published", "ClPublished");

        var publishResponse = RestClient.create()
                .put()
                .uri(codesListsEndpoint() + "/CL_PUBLISHED/validate")
                .retrieve()
                .toBodilessEntity();
        assertThat(publishResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(fetchDetailed("CL_PUBLISHED").getString("validationState")).isEqualTo("Validated");

        RestClient.create()
                .put()
                .uri(codesListsEndpoint() + "/CL_PUBLISHED")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .body(body("CL_PUBLISHED", "cl-published", "ClPublished")
                        .replace("Liste CL_PUBLISHED", "Liste publiée v2"))
                .retrieve()
                .toBodilessEntity();

        JSONObject updated = fetchDetailed("CL_PUBLISHED");
        assertThat(updated.getString("labelLg1")).isEqualTo("Liste publiée v2");
        assertThat(updated.getString("validationState")).isEqualTo("Modified");
    }

    @Test
    @Order(7)
    @DisplayName("The created codes list is exposed with a Location header pointing at itself")
    void location_header_points_at_the_created_codes_list() {
        var response = RestClient.create()
                .post()
                .uri(codesListsEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .body(body("CL_LOCATION", "cl-location", "ClLocation"))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getHeaders().get(HttpHeaders.LOCATION))
                .containsExactly(codesListsEndpoint() + "/CL_LOCATION");
    }
}
