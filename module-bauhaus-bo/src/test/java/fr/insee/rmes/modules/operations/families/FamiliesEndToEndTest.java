package fr.insee.rmes.modules.operations.families;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/**
 * Parcours complet d'une famille d'opérations à travers l'API : création, relecture, mise à jour,
 * publication. Le chemin d'écriture est passé du service historique au module hexagonal ; ce test
 * fixe le contrat HTTP que le front consomme, codes d'erreur compris.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FamiliesEndToEndTest extends WithGraphDBContainer {

    private static final String ISO_8601_LOCAL_DATE_TIME_PATTERN =
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?(\\.\\d+)?$";
    private static final String BAUHAUS_TEST_PUBLICATION_REPOSITORY = "bauhaus-test-pub";

    private static final String FAMILY_REQUEST_JSON = """
            {
                "prefLabelLg1": "%s",
                "prefLabelLg2": "%s",
                "abstractLg1": "Résumé de la famille",
                "abstractLg2": "Family abstract"
            }
            """;

    /**
     * Le corps du GET porte deux fois les clés {@code series} et {@code subjects} — le mixin qui
     * doit masquer celles du modèle de domaine n'est posé que sur le mapper Jackson 2, pas sur
     * celui qui sérialise les réponses HTTP. Jackson garde la dernière occurrence, {@code org.json}
     * refuse le document : la relecture passe donc par Jackson.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    private String familiesEndpoint() {
        return "http://localhost:" + serverPort + "/api/operations";
    }

    @Test
    @Order(1)
    @DisplayName("Create a family, read it back, update it then publish it")
    void ok_when_family_created_updated_then_validated() {
        RestClient restClient = RestClient.create();

        String id = restClient
                .post()
                .uri(familiesEndpoint() + "/family")
                .body(FAMILY_REQUEST_JSON.formatted("Famille e2e", "e2e family"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .body(String.class);

        assertThat(id).isNotBlank();

        JsonNode fetched = getFamily(restClient, id);
        assertThat(fetched.get("id").asText()).isEqualTo(id);
        assertThat(fetched.get("prefLabelLg1").asText()).isEqualTo("Famille e2e");
        assertThat(fetched.get("prefLabelLg2").asText()).isEqualTo("e2e family");
        assertThat(fetched.get("abstractLg1").asText()).isEqualTo("Résumé de la famille");
        assertThat(fetched.get("abstractLg2").asText()).isEqualTo("Family abstract");
        assertThat(fetched.get("validationState").asText()).isEqualTo("Unpublished");
        String created = fetched.get("created").asText();
        assertThat(created).matches(ISO_8601_LOCAL_DATE_TIME_PATTERN);

        var updateResponse = restClient
                .put()
                .uri(familiesEndpoint() + "/family/" + id)
                .body("""
                        {
                            "id": "%s",
                            "prefLabelLg1": "Famille e2e v2",
                            "prefLabelLg2": "e2e family v2",
                            "abstractLg1": "Résumé v2",
                            "abstractLg2": "Abstract v2",
                            "created": "%s",
                            "validationState": "Unpublished"
                        }
                        """.formatted(id, created))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        fetched = getFamily(restClient, id);
        assertThat(fetched.get("prefLabelLg1").asText()).isEqualTo("Famille e2e v2");
        assertThat(fetched.get("prefLabelLg2").asText()).isEqualTo("e2e family v2");
        assertThat(fetched.get("abstractLg1").asText()).isEqualTo("Résumé v2");
        assertThat(fetched.get("abstractLg2").asText()).isEqualTo("Abstract v2");
        assertThat(fetched.get("validationState").asText())
                .as("une famille jamais publiée reste Unpublished après édition")
                .isEqualTo("Unpublished");
        assertThat(fetched.get("created").asText())
                .as("la date de création renvoyée par le client doit survivre à la mise à jour")
                .isEqualTo(created);

        var validateResponse = restClient
                .put()
                .uri(familiesEndpoint() + "/family/" + id + "/validate")
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(getFamily(restClient, id).get("validationState").asText()).isEqualTo("Validated");

        restClient
                .put()
                .uri(familiesEndpoint() + "/family/" + id + "/validate")
                .accept(MediaType.TEXT_PLAIN)
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(new JSONObject(response.bodyTo(String.class)).getInt("code"))
                            .isEqualTo(1301);
                    return null;
                });
    }

    @Test
    @Order(2)
    @DisplayName("POST family without prefLabelLg1 or prefLabelLg2 returns 400 with the faulty fields")
    void bad_request_when_a_pref_label_is_missing() {
        RestClient restClient = RestClient.create();

        restClient
                .post()
                .uri(familiesEndpoint() + "/family")
                .body("""
                        {
                            "abstractLg1": "Résumé sans libellé"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    JSONAssert.assertEquals("""
                            {"errors":[
                              {"field":"prefLabelLg1","message":"prefLabelLg1 is required"},
                              {"field":"prefLabelLg2","message":"prefLabelLg2 is required"}
                            ]}
                            """, response.bodyTo(String.class), false);
                    return null;
                });

        restClient
                .post()
                .uri(familiesEndpoint() + "/family")
                .body(FAMILY_REQUEST_JSON.formatted("Famille sans libellé secondaire", ""))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    JSONAssert.assertEquals("""
                            {"errors":[{"field":"prefLabelLg2","message":"prefLabelLg2 is required"}]}
                            """, response.bodyTo(String.class), false);
                    return null;
                });
    }

    @Test
    @Order(3)
    @DisplayName("POST family with a prefLabel already used returns 400 with the error code the front translates")
    void bad_request_when_a_pref_label_is_already_used() {
        RestClient restClient = RestClient.create();

        restClient
                .post()
                .uri(familiesEndpoint() + "/family")
                .body(FAMILY_REQUEST_JSON.formatted("Famille e2e unique", "e2e unique family"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toBodilessEntity();

        restClient
                .post()
                .uri(familiesEndpoint() + "/family")
                .body(FAMILY_REQUEST_JSON.formatted("Famille e2e unique", "another e2e family"))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(new JSONObject(response.bodyTo(String.class)).getString("message"))
                            .isEqualTo("406_OPERATION_FAMILY_OPERATION_FAMILY_EXISTING_PREF_LABEL_LG1");
                    return null;
                });

        restClient
                .post()
                .uri(familiesEndpoint() + "/family")
                .body(FAMILY_REQUEST_JSON.formatted("Famille e2e encore unique", "e2e unique family"))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(new JSONObject(response.bodyTo(String.class)).getString("message"))
                            .isEqualTo("406_OPERATION_FAMILY_OPERATION_FAMILY_EXISTING_PREF_LABEL_LG2");
                    return null;
                });
    }

    @Test
    @Order(4)
    @DisplayName("PUT family on an unknown id returns 404")
    void not_found_when_updating_an_unknown_family() {
        RestClient restClient = RestClient.create();

        restClient
                .put()
                .uri(familiesEndpoint() + "/family/unknown-family")
                .body(FAMILY_REQUEST_JSON.formatted("Famille fantôme", "ghost family"))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(new JSONObject(response.bodyTo(String.class)).getInt("code"))
                            .isEqualTo(541);
                    return null;
                });
    }

    private JsonNode getFamily(RestClient restClient, String id) {
        String body = restClient
                .get()
                .uri(familiesEndpoint() + "/family/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        try {
            return MAPPER.readTree(body);
        } catch (Exception e) {
            throw new AssertionError("Unreadable family payload: " + body, e);
        }
    }
}
