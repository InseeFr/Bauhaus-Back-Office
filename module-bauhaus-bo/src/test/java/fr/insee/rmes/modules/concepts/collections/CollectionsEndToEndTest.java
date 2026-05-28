package fr.insee.rmes.modules.concepts.collections;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CollectionsEndToEndTest extends WithGraphDBContainer {

    public static final String ISO_8601_DATE_TIME_PATTERN = "^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?$";
    private static final String BAUHAUS_TEST_PUBLICATION_REPOSITORY = "bauhaus-test-pub";
    final static String CREATE_COLLECTION_REQUEST_JSON = """
            {
                 "id": "%s",
                 "labels": [{"value": "label fr", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "http://bauhaus/HIE000000",
                 "contributor" : "http://bauhaus/HIE000000",
                 "conceptsIdentifiers": ["c00001"]
             }
            """;

    final static String UPDATE_COLLECTION_REQUEST_JSON = """
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
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("/testcontainers").withRepository("config-pub.ttl");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> BAUHAUS_TEST_PUBLICATION_REPOSITORY);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.baseURI", () -> "http://id.insee.fr/");
        container.withInitFolder("fr/insee/rmes/modules/concepts/collections")
                .withTrigFiles("collections-end-to-end-test.trig");
    }

    @Test
    @Order(1)
    @DisplayName("Fetch all collections then add another one then check it is well added")
    void ok_when_collection_added_test() {

        String collectionsEndpoint = "http://localhost:" + serverPort + "/api/concepts/collections";
        RestClient restClient = RestClient.create();

        var fetchedCollections = restClient
                .get()
                .uri(collectionsEndpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("[]", fetchedCollections, true);

        String requestedId = "Collection-e2e-001";
        var entityResponse = restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(requestedId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);

        assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String uuid = entityResponse.getBody();

        assertThat(uuid).isEqualTo(requestedId);
        assertThat(entityResponse.getHeaders().get(HttpHeaders.LOCATION)).containsExactly(collectionsEndpoint + "/" + uuid);

        fetchedCollections = restClient
                .get()
                .uri(collectionsEndpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                [
                  {
                    "id" : "%s",
                    "label": {"value": "label fr", "lang": "FR"}
                  }
                ]
                """.formatted(uuid), fetchedCollections, true);

        fetchedCollections = restClient
                .get().uri(collectionsEndpoint + "/" + uuid)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
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

        var updateResponseKo = restClient.put().uri(collectionsEndpoint + "/" + uuid).body(UPDATE_COLLECTION_REQUEST_JSON.formatted("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .onStatus(status -> true, (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        var updateResponseOk = restClient.put().uri(collectionsEndpoint + "/" + uuid).body(UPDATE_COLLECTION_REQUEST_JSON.formatted(uuid))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(Void.class);

        assertThat(updateResponseOk.getStatusCode()).isEqualTo(HttpStatus.OK);

        fetchedCollections = restClient
                .get().uri(collectionsEndpoint + "/" + uuid)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

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

        var dashboardResponse = restClient
                .get().uri(collectionsEndpoint + "/dashboard")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
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

        var toValidateResponse = restClient
                .get().uri(collectionsEndpoint + "/toValidate")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                [
                  {
                    "id": "%s",
                    "label": "label fr v2"
                  }
                ]
                """.formatted(uuid), toValidateResponse, false);

        var membersResponse = restClient
                .get().uri(collectionsEndpoint + "/" + uuid + "/members")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
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
        String collectionsEndpoint = "http://localhost:" + serverPort + "/api/concepts/collections";
        RestClient restClient = RestClient.create();

        restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted("invalid id with spaces"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .onStatus(status -> true, (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .toBodilessEntity();
    }

    @Test
    @Order(3)
    @DisplayName("POST collection with already-existing id returns 409")
    void conflict_when_id_already_exists() {
        String collectionsEndpoint = "http://localhost:" + serverPort + "/api/concepts/collections";
        RestClient restClient = RestClient.create();
        String existingId = "Collection-conflict-001";

        var firstCreate = restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(existingId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(firstCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(existingId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .onStatus(status -> true, (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT))
                .toBodilessEntity();
    }

    @Test
    @Order(4)
    @DisplayName("PUT /{id}/validate sets validationState=Validated on the collection")
    void ok_when_collection_validated() {
        String collectionsEndpoint = "http://localhost:" + serverPort + "/api/concepts/collections";
        RestClient restClient = RestClient.create();
        String validatedId = "Collection-validate-001";

        restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(validatedId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toBodilessEntity();

        var validateResponse = restClient.put()
                .uri(collectionsEndpoint + "/" + validatedId + "/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body("[\"%s\"]".formatted(validatedId))
                .retrieve()
                .toBodilessEntity();
        assertThat(validateResponse.getStatusCode().is2xxSuccessful()).isTrue();

        var fetched = restClient
                .get().uri(collectionsEndpoint + "/" + validatedId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        assertThat(new JSONObject(fetched).getString("validationState")).isEqualTo("Validated");
    }

    @Test
    @Order(5)
    @DisplayName("GET /{id}/export returns an ODT document")
    void ok_when_collection_exported() {
        String collectionsEndpoint = "http://localhost:" + serverPort + "/api/concepts/collections";
        RestClient restClient = RestClient.create();
        String exportedId = "Collection-export-001";

        restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(exportedId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toBodilessEntity();

        var exportResponse = restClient
                .get().uri(collectionsEndpoint + "/" + exportedId + "/export")
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
                .startsWith("attachment; filename=\"");
        assertThat(exportResponse.getHeaders().getFirst("Access-Control-Expose-Headers"))
                .as("Access-Control-Expose-Headers must expose Content-Disposition so the browser fetch can read it")
                .contains("Content-Disposition");
    }

}
