package fr.insee.rmes.modules.concepts.concept;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConceptCollectionEndToEndTest extends WithGraphDBContainer {

    static final String CREATE_COLLECTION_WITH_CONCEPT_JSON = """
            {
                "labels": [{"value": "Collection test", "lang": "fr"}],
                "descriptions": [],
                "creator": "http://bauhaus/HIE000000",
                "contributor": "http://bauhaus/HIE000000",
                "conceptsIdentifiers": ["c00001"]
            }
            """;

    static final String UPDATE_CONCEPT_WITH_COLLECTIONS_JSON = """
            {
                "prefLabelLg1": "Concept test",
                "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                "creator": "http://bauhaus/HIE000000",
                "collections": ["%s"]
            }
            """;

    static final String CREATE_EMPTY_COLLECTION_JSON = """
            {
                "labels": [{"value": "Collection vide", "lang": "fr"}],
                "descriptions": [],
                "creator": "http://bauhaus/HIE000000",
                "contributor": "http://bauhaus/HIE000000",
                "conceptsIdentifiers": []
            }
            """;

    static final String UPDATE_CONCEPT_WITHOUT_COLLECTIONS_JSON = """
            {
                "prefLabelLg1": "Concept test",
                "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                "creator": "http://bauhaus/HIE000000",
                "collections": []
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
                .withTrigFiles("concept-collection-end-to-end-test.trig");
    }

    @Test
    @DisplayName("Concept returns empty collections when not linked to any collection")
    void concept_with_no_collection_returns_empty_list() {
        RestClient restClient = RestClient.create();
        String conceptUrl = "http://localhost:" + serverPort + "/api/concepts/concept/c00002";

        var response = restClient.get()
                .uri(conceptUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        JSONAssert.assertEquals("""
                {"collections": []}
                """, response, false);
    }

    @Test
    @DisplayName("Concept collections are synced when concept is saved with collections field")
    void concept_collections_sync_on_save() {
        RestClient restClient = RestClient.create();
        String conceptsBaseUrl = "http://localhost:" + serverPort + "/api/concepts";
        String collectionsEndpoint = conceptsBaseUrl + "/collections";
        String c00001Endpoint = conceptsBaseUrl + "/concept/c00001";

        // Initially c00001 has no collections
        var c00001Initial = restClient.get()
                .uri(c00001Endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                {"collections": []}
                """, c00001Initial, false);

        // Create a collection containing c00001
        var createResponse = restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_WITH_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String collectionId = createResponse.getBody();
        assertThat(collectionId).isNotNull();

        // c00001 should now belong to the collection
        var c00001AfterCollectionCreation = restClient.get()
                .uri(c00001Endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                {"collections": ["%s"]}
                """.formatted(collectionId), c00001AfterCollectionCreation, false);

        // Update concept to remove from all collections
        var updateStatus = restClient.put()
                .uri(c00001Endpoint)
                .body(UPDATE_CONCEPT_WITHOUT_COLLECTIONS_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        assertThat(updateStatus.getStatusCode().is2xxSuccessful()).isTrue();

        // c00001 should no longer belong to any collection
        var c00001AfterRemoval = restClient.get()
                .uri(c00001Endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                {"collections": []}
                """, c00001AfterRemoval, false);
    }

    @Test
    @DisplayName("Saving concept with multiple collections links all of them")
    void concept_with_multiple_collections_saves_all() {
        RestClient restClient = RestClient.create();
        String conceptsBaseUrl = "http://localhost:" + serverPort + "/api/concepts";
        String collectionsEndpoint = conceptsBaseUrl + "/collections";
        String c00001Endpoint = conceptsBaseUrl + "/concept/c00001";

        // Create first collection (with c00001 as member)
        var createResponse1 = restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_COLLECTION_WITH_CONCEPT_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String collectionId1 = createResponse1.getBody();

        // Create second collection (empty)
        var createResponse2 = restClient.post()
                .uri(collectionsEndpoint)
                .body(CREATE_EMPTY_COLLECTION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String collectionId2 = createResponse2.getBody();

        // Update concept to link it to both collections
        var updateStatus = restClient.put()
                .uri(c00001Endpoint)
                .body("""
                        {
                            "prefLabelLg1": "Concept test",
                            "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                            "creator": "http://bauhaus/HIE000000",
                            "collections": ["%s", "%s"]
                        }
                        """.formatted(collectionId1, collectionId2))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        assertThat(updateStatus.getStatusCode().is2xxSuccessful()).isTrue();

        // Both collections should be returned
        var c00001Response = restClient.get()
                .uri(c00001Endpoint)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        JSONAssert.assertEquals("""
                {"collections": ["%s", "%s"]}
                """.formatted(collectionId1, collectionId2), c00001Response, false);
    }
}
