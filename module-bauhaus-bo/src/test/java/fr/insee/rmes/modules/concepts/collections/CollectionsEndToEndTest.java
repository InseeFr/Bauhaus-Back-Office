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
                   "isValidated": false,
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
                   "isValidated": false,
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
                    "isValidated": false,
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

}
