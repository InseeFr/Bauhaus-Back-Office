package fr.insee.rmes.modules.concepts.collections;

import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "fr.insee.rmes.bauhaus.activeModules=concepts")
class CollectionsEndToEndTest extends BaseE2ETest {

    public static final String ISO_8601_DATE_TIME_PATTERN = "^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?$";
    static final String CREATE_COLLECTION_REQUEST_JSON = """
            {
                 "labels": [{"value": "label fr", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "HIE000000",
                 "contributor" : "HIE000000",
                 "conceptsIdentifiers": ["c00001"]
             }
            """;

    static final String UPDATE_COLLECTION_REQUEST_JSON = """
            {
               "id": "%s",
               "labels": [{"value": "label fr v2", "lang": "fr"}],
               "descriptions": [],
               "creator" : "HIE000001",
               "contributor" : "HIE000002",
               "conceptsIdentifiers": ["c00001"],
               "created": %s
            }
            """;

    public static final String UUID_PATTERN = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
    }

    @Test
    @DisplayName("Fetch all collections then add another one then check it is well added")
    void ok_when_collection_added_test() {

        restTestClient
                .get()
                .uri("/concepts/collections")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");


        var response = restTestClient.post()
                .uri("/concepts/collections")
                .body(CREATE_COLLECTION_REQUEST_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .exchange();
        URI requestUrl = response.returnResult().getUrl();
        String uuid = response.returnResult(String.class).getResponseBody();
        response.expectStatus().isCreated()
                .expectHeader().location(requestUrl.resolve("/api/concepts/collections/"+uuid).toString());
        assertThat(uuid).matches(UUID_PATTERN);

        restTestClient
                .get()
                .uri("/concepts/collections")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                        [
                          {
                            "id" : "%s",
                            "label": {"value": "label fr", "lang": "FR"}
                          }
                        ]
                        """.formatted(uuid));

        var captureCreated = new Object(){
            String value;
        };
        restTestClient
                .get()
                .uri("/concepts/collections/" + uuid)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                        {
                          "id" : "%s",
                           "labels": [{"value": "label fr", "lang": "FR"}],
                           "descriptions": [],
                           "creator" : "HIE000000",
                           "contributor" : "HIE000000",
                           "isValidated": false,
                        }
                        """.formatted(uuid), JsonCompareMode.LENIENT)
                .jsonPath("$.created").value(String.class, v -> {
                    captureCreated.value = v;
                    assertThat(captureCreated.value).matches(ISO_8601_DATE_TIME_PATTERN);
                })
                .jsonPath("$.modified").isEmpty();


        restTestClient
                .put()
                .uri("/concepts/collections/" + uuid)
                .body(UPDATE_COLLECTION_REQUEST_JSON.formatted("1", ""))
                .headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isBadRequest();

        restTestClient
                .put()
                .uri("/concepts/collections/" + uuid)
                .body(UPDATE_COLLECTION_REQUEST_JSON.formatted(uuid, "\""+captureCreated.value+"\""))
                .headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        response = restTestClient
                .get()
                .uri("/concepts/collections/" + uuid)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
        System.out.println(response.returnResult(String.class).getResponseBody());
        response
                .expectStatus().isOk()
                .expectBody().json("""
                        {
                          "id" : "%s",
                           "labels": [{"value": "label fr v2", "lang": "FR"}],
                           "descriptions": [],
                           "creator" : "HIE000001",
                           "contributor" : "HIE000002",
                           "isValidated": false,
                        }
                        """.formatted(uuid), JsonCompareMode.LENIENT)
                .jsonPath("$.created").isEqualTo(captureCreated.value)
                .jsonPath("$.modified").value(String.class, v -> assertThat(v).matches(ISO_8601_DATE_TIME_PATTERN));

    }

}
