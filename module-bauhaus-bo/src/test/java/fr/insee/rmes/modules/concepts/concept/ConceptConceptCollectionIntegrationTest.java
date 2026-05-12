package fr.insee.rmes.modules.concepts.concept;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConceptConceptCollectionIntegrationTest extends WithGraphDBContainer {

    private static final String DISSEMINATION_STATUS = "http://id.insee.fr/codes/base/statutDiffusion/Prive";
    private static final String CREATOR = "http://bauhaus/HIE000000";
    private static final String CONTRIBUTOR = "http://bauhaus/HIE000000";

    private static final String COLLECTION_TEMPLATE = """
            {
                "id": "%s",
                "labels": [{"value": "%s", "lang": "fr"}],
                "descriptions": [],
                "creator": "%s",
                "contributor": "%s",
                "conceptsIdentifiers": []
            }
            """;

    private static final String CONCEPT_WITH_NOTE_TEMPLATE = """
            {
                "prefLabelLg1": "%s",
                "creator": "%s",
                "contributor": "%s",
                "disseminationStatus": "%s",
                "collections": %s,
                "versionableNotes": [
                    {"noteType": "scopeNoteLg1", "content": "<div>Note FR</div>"}
                ]
            }
            """;

    @LocalServerPort
    int serverPort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("fr/insee/rmes/modules/concepts/concept")
                .withTrigFiles("concept-concept-collection-integration-test.trig");
    }

    private String conceptsBase() {
        return "http://localhost:" + serverPort + "/api/concepts";
    }

    private String createCollection(String id, String label) {
        var response = RestClient.create().post()
                .uri(conceptsBase() + "/collections")
                .body(COLLECTION_TEMPLATE.formatted(id, label, CREATOR, CONTRIBUTOR))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private String createConcept(String label, String collectionsJsonArray) {
        return RestClient.create().post()
                .uri(conceptsBase() + "/concept")
                .body(CONCEPT_WITH_NOTE_TEMPLATE.formatted(
                        label, CREATOR, CONTRIBUTOR, DISSEMINATION_STATUS, collectionsJsonArray))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    private JSONArray getCollectionMembers(String collectionId) {
        String response = RestClient.create().get()
                .uri(conceptsBase() + "/collections/" + collectionId + "/members")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        return new JSONArray(response);
    }

    private static boolean containsId(JSONArray array, String id) {
        for (int i = 0; i < array.length(); i++) {
            if (id.equals(array.getJSONObject(i).optString("id"))) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("POST concept with collections=[A] adds the concept as a member of A")
    void create_concept_with_collections_registers_link() {
        String collectionId = createCollection("Collection-cross-001", "Cross 001");

        String conceptId = createConcept("Concept cross 001", "[\"" + collectionId + "\"]");

        assertThat(containsId(getCollectionMembers(collectionId), conceptId))
                .as("collection %s must contain freshly-created concept %s", collectionId, conceptId)
                .isTrue();
    }

    @Test
    @DisplayName("PUT concept with collections=[A] later adds it to A even if created without")
    void update_concept_with_collections_adds_link() {
        String collectionId = createCollection("Collection-cross-002", "Cross 002");
        String conceptId = createConcept("Concept cross 002", "[]");
        assertThat(containsId(getCollectionMembers(collectionId), conceptId)).isFalse();

        RestClient.create().put()
                .uri(conceptsBase() + "/concept/" + conceptId)
                .body(CONCEPT_WITH_NOTE_TEMPLATE.formatted(
                        "Concept cross 002",
                        CREATOR, CONTRIBUTOR, DISSEMINATION_STATUS,
                        "[\"" + collectionId + "\"]"))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();

        assertThat(containsId(getCollectionMembers(collectionId), conceptId)).isTrue();
    }

    @Test
    @DisplayName("PUT concept with collections=[] later removes the previously-set link")
    void update_concept_without_collections_removes_link() {
        String collectionId = createCollection("Collection-cross-003", "Cross 003");
        String conceptId = createConcept("Concept cross 003", "[\"" + collectionId + "\"]");
        assertThat(containsId(getCollectionMembers(collectionId), conceptId)).isTrue();

        RestClient.create().put()
                .uri(conceptsBase() + "/concept/" + conceptId)
                .body(CONCEPT_WITH_NOTE_TEMPLATE.formatted(
                        "Concept cross 003",
                        CREATOR, CONTRIBUTOR, DISSEMINATION_STATUS,
                        "[]"))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();

        assertThat(containsId(getCollectionMembers(collectionId), conceptId)).isFalse();
    }

    @Test
    @DisplayName("POST concept referencing an unknown collection id returns 400")
    void create_concept_with_unknown_collection_is_rejected() {
        RestClient.create().post()
                .uri(conceptsBase() + "/concept")
                .body(CONCEPT_WITH_NOTE_TEMPLATE.formatted(
                        "Concept cross 004",
                        CREATOR, CONTRIBUTOR, DISSEMINATION_STATUS,
                        "[\"Collection-does-not-exist\"]"))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> true,
                        (req, res) -> assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .toBodilessEntity();
    }

    @Test
    @DisplayName("DELETE concept removes it from the visible members of its collections")
    void delete_concept_drops_it_from_collection_members() {
        String collectionId = createCollection("Collection-cross-005", "Cross 005");
        String conceptId = createConcept("Concept cross 005", "[\"" + collectionId + "\"]");
        assertThat(containsId(getCollectionMembers(collectionId), conceptId)).isTrue();

        RestClient.create().delete()
                .uri(conceptsBase() + "/" + conceptId)
                .retrieve()
                .toBodilessEntity();

        assertThat(containsId(getCollectionMembers(collectionId), conceptId)).isFalse();
    }
}
