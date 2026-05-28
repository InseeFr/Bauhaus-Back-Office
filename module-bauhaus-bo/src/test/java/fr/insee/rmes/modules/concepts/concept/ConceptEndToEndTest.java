package fr.insee.rmes.modules.concepts.concept;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConceptEndToEndTest extends WithGraphDBContainer {

    private static final String DISSEMINATION_STATUS_PRIVE = "http://id.insee.fr/codes/base/statutDiffusion/Prive";
    private static final String CREATOR = "http://bauhaus/HIE000000";
    private static final String CONTRIBUTOR = "http://bauhaus/HIE000000";

    private static final String CREATE_CONCEPT_JSON = """
            {
                "prefLabelLg1": "Concept E2E",
                "prefLabelLg2": "E2E concept",
                "creator": "%s",
                "contributor": "%s",
                "disseminationStatus": "%s",
                "versionableNotes": [
                    {"noteType": "scopeNoteLg1", "content": "<div>Note de portée FR</div>"}
                ]
            }
            """.formatted(CREATOR, CONTRIBUTOR, DISSEMINATION_STATUS_PRIVE);

    private static final String UPDATE_CONCEPT_JSON = """
            {
                "prefLabelLg1": "Concept E2E (modifié)",
                "prefLabelLg2": "E2E concept (updated)",
                "creator": "%s",
                "contributor": "%s",
                "disseminationStatus": "%s",
                "versionableNotes": [
                    {"noteType": "scopeNoteLg1", "content": "<div>Note de portée FR (modifiée)</div>"}
                ]
            }
            """.formatted(CREATOR, CONTRIBUTOR, DISSEMINATION_STATUS_PRIVE);

    @LocalServerPort
    int serverPort;

    private static final String BAUHAUS_TEST_PUBLICATION_REPOSITORY = "bauhaus-test-pub";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("/testcontainers").withRepository("config-pub.ttl");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> BAUHAUS_TEST_PUBLICATION_REPOSITORY);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.baseURI", () -> "http://id.insee.fr/");
        container.withInitFolder("fr/insee/rmes/modules/concepts/concept")
                .withTrigFiles("concept-end-to-end-test.trig");
    }

    private String conceptsBaseUrl() {
        return "http://localhost:" + serverPort + "/api/concepts";
    }

    @Test
    @Order(1)
    @DisplayName("GET /concepts/concept/{id} on unknown id returns 404")
    void not_found_when_concept_id_is_unknown() {
        RestClient restClient = RestClient.create();

        restClient.get()
                .uri(conceptsBaseUrl() + "/concept/c-does-not-exist")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .toBodilessEntity();
    }

    @Test
    @Order(2)
    @DisplayName("POST /concepts/concept creates a concept, GET returns it with collections=[]")
    void create_then_get_concept_round_trips() {
        RestClient restClient = RestClient.create();

        var createResponse = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = createResponse.getBody();
        assertThat(id).as("POST should return a generated id starting with 'c'").startsWith("c");

        var fetched = restClient.get()
                .uri(conceptsBaseUrl() + "/concept/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        JSONObject fetchedJson = new JSONObject(fetched);
        assertThat(fetchedJson.getString("id")).isEqualTo(id);
        assertThat(fetchedJson.getString("prefLabelLg1")).isEqualTo("Concept E2E");
        assertThat(fetchedJson.optString("prefLabelLg2")).isEqualTo("E2E concept");
        assertThat(fetchedJson.getString("disseminationStatus")).isEqualTo(DISSEMINATION_STATUS_PRIVE);
        assertThat(fetchedJson.getString("contributor")).isEqualTo(CONTRIBUTOR);
        assertThat(fetchedJson.getString("validationState")).isEqualTo("Unpublished");
        assertThat(fetchedJson.has("created")).isTrue();
        JSONAssert.assertEquals("""
                {"collections": []}
                """, fetched, false);
    }

    @Test
    @Order(3)
    @DisplayName("PUT /concepts/concept/{id} on an existing concept returns 204")
    void update_existing_concept_returns_no_content() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        assertThat(id).isNotNull();

        var updateResponse = restClient.put()
                .uri(conceptsBaseUrl() + "/concept/" + id)
                .body(UPDATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(4)
    @DisplayName("Newly-created concepts appear in GET /concepts/toValidate; validation removes them")
    void to_validate_then_validate_workflow() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        var toValidateBefore = restClient.get()
                .uri(conceptsBaseUrl() + "/toValidate")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONArray toValidateArray = new JSONArray(toValidateBefore);
        assertThat(toValidateContainsId(toValidateArray, id))
                .as("toValidate list should contain freshly-created concept %s", id)
                .isTrue();

        var validateResponse = restClient.put()
                .uri(conceptsBaseUrl() + "/" + id + "/validate")
                .body("[\"%s\"]".formatted(id))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var toValidateAfter = restClient.get()
                .uri(conceptsBaseUrl() + "/toValidate")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        assertThat(toValidateContainsId(new JSONArray(toValidateAfter), id))
                .as("validated concept %s must no longer appear in /toValidate", id)
                .isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("GET /concepts lists created concepts")
    void list_all_concepts_contains_created_one() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        var listing = restClient.get()
                .uri(conceptsBaseUrl())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThat(listing).contains(id);
    }

    @Test
    @Order(6)
    @DisplayName("GET /concepts/advanced-search lists created concepts with label")
    void advanced_search_contains_created_one() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        var listing = restClient.get()
                .uri(conceptsBaseUrl() + "/advanced-search")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThat(listing)
                .as("advanced-search payload should reference newly created concept %s", id)
                .contains(id);
    }

    @Test
    @Order(7)
    @DisplayName("GET /concepts/concept/{id}/links returns a JSON array for a concept without links")
    void links_endpoint_returns_empty_array_for_isolated_concept() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        var links = restClient.get()
                .uri(conceptsBaseUrl() + "/concept/" + id + "/links")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThat(links).isNotNull();
        new JSONArray(links);
    }

    @Test
    @Order(8)
    @DisplayName("GET /concepts/concept/export/{id} returns a downloadable document")
    void export_concept_returns_attachment() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        var exportResponse = restClient.get()
                .uri(conceptsBaseUrl() + "/concept/export/" + id)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .toEntity(byte[].class);

        assertThat(exportResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        byte[] body = exportResponse.getBody();
        assertThat(body).isNotNull().isNotEmpty();
        assertThat(body[0]).isEqualTo((byte) 'P');
        assertThat(body[1]).isEqualTo((byte) 'K');
        assertThat(exportResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .as("Content-Disposition must carry an attachment filename so the browser triggers a download")
                .startsWith("attachment; filename=");
    }

    @Test
    @Order(9)
    @DisplayName("POST /concepts/concept with a malformed JSON body returns a 4xx/5xx error")
    void post_concept_with_malformed_body_fails() {
        RestClient restClient = RestClient.create();

        restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body("{ not valid json")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> true, (req, res) -> assertThat(res.getStatusCode().is2xxSuccessful())
                        .as("malformed JSON must NOT yield a 2xx response")
                        .isFalse())
                .toBodilessEntity();
    }

    @Test
    @Order(10)
    @DisplayName("DELETE /concepts/{id} removes an isolated concept and a subsequent GET returns 404")
    void delete_isolated_concept_then_get_returns_404() {
        RestClient restClient = RestClient.create();

        String id = restClient.post()
                .uri(conceptsBaseUrl() + "/concept")
                .body(CREATE_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        var deleteResponse = restClient.delete()
                .uri(conceptsBaseUrl() + "/" + id)
                .retrieve()
                .toBodilessEntity();
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        restClient.get()
                .uri(conceptsBaseUrl() + "/concept/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .toBodilessEntity();
    }

    private static boolean toValidateContainsId(JSONArray array, String id) {
        for (int i = 0; i < array.length(); i++) {
            Object item = array.get(i);
            if (item instanceof JSONObject obj && id.equals(obj.optString("id"))) {
                return true;
            }
        }
        return false;
    }
}
