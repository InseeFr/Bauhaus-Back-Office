package fr.insee.rmes.modules.concepts.collections;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class CollectionsEndToEndTest extends WithGraphDBContainer {

    public static final String ISO_8601_DATE_TIME_PATTERN = "^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?$";
    final static String CREATE_COLLECTION_REQUEST_JSON = """
            {
                 "labels": [{"value": "label fr", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "HIE000000",
                 "contributor" : "HIE000000",
                 "conceptsIdentifiers": ["c00001"]
             }
            """;

    final static String UPDATE_COLLECTION_REQUEST_JSON = """
            {   
                "id": "%s",
                 "labels": [{"value": "label fr v2", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "HIE000001",
                 "contributor" : "HIE000002",
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
    }

    @Test
    @DisplayName("Fetch all collections then add another one then check it is well added")
    void ok_when_collection_added_test() {

        String collectionsEndpoint = "http://localhost:" + serverPort + "/api/concepts/collections/";
        RestClient restClient = RestClient.create(collectionsEndpoint);
        var fetchedCollections = restClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("[]", fetchedCollections, true);

        var entityResponse = restClient.post().body(CREATE_COLLECTION_REQUEST_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);

        assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String uuid = entityResponse.getBody();

        assertThat(entityResponse.getHeaders().get(HttpHeaders.LOCATION)).containsExactly("http://localhost:" + serverPort + "/api/concepts/collections/" + uuid);
        //TODO check uuid regexp
        assertThat(uuid).isNotNull();

        fetchedCollections = restClient
                .get()
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
                .get().uri(uuid)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                {
                  "id" : "%s",
                   "labels": [{"value": "label fr", "lang": "FR"}],
                   "descriptions": [],
                   "creator" : "HIE000000",
                   "contributor" : "HIE000000",
                   "isValidated": false,
                }
                """.formatted(uuid), fetchedCollections, false);
        System.out.println(fetchedCollections);
        assertThat((new JSONObject(fetchedCollections)).getString("created")).matches(ISO_8601_DATE_TIME_PATTERN);
        assertThat((new JSONObject(fetchedCollections)).has("modified")).isTrue();
        assertThat((new JSONObject(fetchedCollections)).isNull("modified")).isTrue();

        var updateResponseKo = restClient.put().uri(uuid).body(UPDATE_COLLECTION_REQUEST_JSON.formatted("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .onStatus(status -> true, (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        var updateResponseOk = restClient.put().uri(uuid).body(UPDATE_COLLECTION_REQUEST_JSON.formatted(uuid))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(Void.class);

        assertThat(updateResponseOk.getStatusCode()).isEqualTo(HttpStatus.OK);

        fetchedCollections = restClient
                .get().uri(uuid)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        JSONAssert.assertEquals("""
                {
                  "id" : "%s",
                   "labels": [{"value": "label fr v2", "lang": "FR"}],
                   "descriptions": [],
                   "creator" : "HIE000001",
                   "contributor" : "HIE000002",
                   "isValidated": false,
                }
                """.formatted(uuid), fetchedCollections, false);

    }

}
