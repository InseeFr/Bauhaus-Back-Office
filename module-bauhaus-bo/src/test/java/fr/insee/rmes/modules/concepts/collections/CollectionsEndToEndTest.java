package fr.insee.rmes.modules.concepts.collections;

import static fr.insee.rmes.modules.concepts.DocumentExportAssertions.assertDownloadableOpenDocument;
import static fr.insee.rmes.testcontainers.GraphDbTestProperties.registerGestionAndDedicatedPublication;
import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CollectionsEndToEndTest extends WithGraphDBContainer {

    public static final String ISO_8601_DATE_TIME_PATTERN =
            "^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?$";
    static final String CREATE_COLLECTION_REQUEST_JSON = """
            {
                 "id": "%s",
                 "labels": [{"value": "label fr", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "http://bauhaus/HIE000000",
                 "contributor" : "http://bauhaus/HIE000000",
                 "conceptsIdentifiers": ["c00001"]
             }
            """;

    static final String UPDATE_COLLECTION_REQUEST_JSON = """
            {
                "id": "%s",
                 "labels": [{"value": "label fr v2", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "http://bauhaus/HIE000001",
                 "contributor" : "http://bauhaus/HIE000002",
                 "conceptsIdentifiers": ["c00001"]
             }
            """;

    @LocalServerPort
    int serverPort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registerGestionAndDedicatedPublication(registry);
        container
                .withInitFolder("fr/insee/rmes/modules/concepts/collections")
                .withTrigFiles("collections-end-to-end-test.trig");
    }

    @Test
    @Order(1)
    @DisplayName("Fetch all collections then add another one then check it is well added")
    void ok_when_collection_added_test() {

        String collectionsEndpoint = collectionsEndpoint();
        RestClient restClient = RestClient.create();

        var fetchedCollections = getJson(restClient, collectionsEndpoint);
        JSONAssert.assertEquals("[]", fetchedCollections, true);

        String requestedId = "Collection-e2e-001";
        var entityResponse = postCollection(restClient, requestedId).toEntity(String.class);

        assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String uuid = entityResponse.getBody();

        assertThat(uuid).isEqualTo(requestedId);
        assertThat(entityResponse.getHeaders().get(HttpHeaders.LOCATION))
                .containsExactly(collectionsEndpoint + "/" + uuid);

        fetchedCollections = getJson(restClient, collectionsEndpoint);
        JSONAssert.assertEquals("""
                [
                  {
                    "id" : "%s",
                    "label": {"value": "label fr", "lang": "FR"}
                  }
                ]
                """.formatted(uuid), fetchedCollections, true);

        fetchedCollections = getJson(restClient, collectionsEndpoint + "/" + uuid);
        JSONAssert.assertEquals("""
                {
                  "id" : "%s",
                   "labels": [{"value": "label fr", "lang": "FR"}],
                   "descriptions": [],
                   "creator" : "http://bauhaus/HIE000000",
                   "contributor" : "http://bauhaus/HIE000000",
                   "validationState": "Unpublished",
                }
                """.formatted(uuid), fetchedCollections, false);
        assertThat((new JSONObject(fetchedCollections)).getString("created")).matches(ISO_8601_DATE_TIME_PATTERN);
        assertThat((new JSONObject(fetchedCollections)).has("modified")).isTrue();
        assertThat((new JSONObject(fetchedCollections)).isNull("modified")).isTrue();

        var updateResponseKo = putCollection(restClient, uuid, "1")
                .onStatus(HttpStatusCode::isError, (req, res) -> {})
                .toBodilessEntity();

        assertThat(updateResponseKo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        var updateResponseOk = putCollection(restClient, uuid, uuid).toEntity(Void.class);

        assertThat(updateResponseOk.getStatusCode()).isEqualTo(HttpStatus.OK);

        fetchedCollections = getJson(restClient, collectionsEndpoint + "/" + uuid);

        JSONAssert.assertEquals("""
                {
                  "id" : "%s",
                   "labels": [{"value": "label fr v2", "lang": "FR"}],
                   "descriptions": [],
                   "creator" : "http://bauhaus/HIE000001",
                   "contributor" : "http://bauhaus/HIE000002",
                   "validationState": "Unpublished",
                }
                """.formatted(uuid), fetchedCollections, false);

        var dashboardResponse = getJson(restClient, collectionsEndpoint + "/dashboard");
        JSONAssert.assertEquals("""
                [
                  {
                    "id": "%s",
                    "label": "label fr v2",
                    "validationState": "Unpublished",
                    "nbMembers": 1
                  }
                ]
                """.formatted(uuid), dashboardResponse, false);

        var toValidateResponse = getJson(restClient, collectionsEndpoint + "/toValidate");
        JSONAssert.assertEquals("""
                [
                  {
                    "id": "%s",
                    "label": "label fr v2"
                  }
                ]
                """.formatted(uuid), toValidateResponse, false);

        var membersResponse = getJson(restClient, collectionsEndpoint + "/" + uuid + "/members");
        JSONAssert.assertEquals("""
                [
                  {
                    "id": "c00001",
                    "prefLabelLg1": "Concept test"
                  }
                ]
                """, membersResponse, false);
    }

    @Test
    @Order(2)
    @DisplayName("POST collection with invalid id returns 400")
    void bad_request_when_id_is_invalid() {
        RestClient restClient = RestClient.create();

        postCollection(restClient, "invalid id with spaces")
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .toBodilessEntity();
    }

    @Test
    @Order(3)
    @DisplayName("POST collection with already-existing id returns 409")
    void conflict_when_id_already_exists() {
        RestClient restClient = RestClient.create();
        String existingId = "Collection-conflict-001";

        var firstCreate = postCollection(restClient, existingId).toEntity(String.class);
        assertThat(firstCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        postCollection(restClient, existingId)
                .onStatus(
                        status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT))
                .toBodilessEntity();
    }

    @Test
    @Order(4)
    @DisplayName("PUT /{id}/validate sets validationState=Validated on the collection")
    void ok_when_collection_validated() {
        String collectionsEndpoint = collectionsEndpoint();
        RestClient restClient = RestClient.create();
        String validatedId = "Collection-validate-001";

        postCollection(restClient, validatedId).toBodilessEntity();

        var validateResponse = restClient
                .put()
                .uri(collectionsEndpoint + "/" + validatedId + "/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body("[\"%s\"]".formatted(validatedId))
                .retrieve()
                .toBodilessEntity();
        assertThat(validateResponse.getStatusCode().is2xxSuccessful()).isTrue();

        var fetched = getJson(restClient, collectionsEndpoint + "/" + validatedId);
        assertThat(new JSONObject(fetched).getString("validationState")).isEqualTo("Validated");
    }

    @Test
    @Order(5)
    @DisplayName("GET /{id}/export returns an ODT document")
    void ok_when_collection_exported() {
        String collectionsEndpoint = collectionsEndpoint();
        RestClient restClient = RestClient.create();
        String exportedId = "Collection-export-001";

        postCollection(restClient, exportedId).toBodilessEntity();

        var exportResponse = restClient
                .get()
                .uri(collectionsEndpoint + "/" + exportedId + "/export")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .toEntity(byte[].class);

        assertDownloadableOpenDocument(exportResponse, "attachment; filename=\"");
        assertThat(exportResponse.getHeaders().getFirst("Access-Control-Expose-Headers"))
                .as("Access-Control-Expose-Headers must expose Content-Disposition so the browser fetch can read it")
                .contains("Content-Disposition");
    }

    private String collectionsEndpoint() {
        return "http://localhost:" + serverPort + "/api/concepts/collections";
    }

    private RestClient.ResponseSpec postCollection(RestClient restClient, String collectionId) {
        return restClient
                .post()
                .uri(collectionsEndpoint())
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(collectionId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve();
    }

    private RestClient.ResponseSpec putCollection(RestClient restClient, String collectionId, String bodyId) {
        return restClient
                .put()
                .uri(collectionsEndpoint() + "/" + collectionId)
                .body(UPDATE_COLLECTION_REQUEST_JSON.formatted(bodyId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve();
    }

    private static String getJson(RestClient restClient, String uri) {
        return restClient
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }
}
