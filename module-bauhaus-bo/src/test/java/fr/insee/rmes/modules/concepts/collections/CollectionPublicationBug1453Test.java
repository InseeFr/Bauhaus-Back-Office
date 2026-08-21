package fr.insee.rmes.modules.concepts.collections;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproducteur du bug InseeFr/Bauhaus#1453 :
 * « Quand je publie une collection, l'état de la collecte n'est pas modifié ».
 *
 * Cause racine : `RdfUtils.collectionIRI` produit une IRI doublée
 * (`http://bauhaus/http://bauhaus/...`) car `BauhausUriBuilder.getBaseUriGestion(COLLECTION)`
 * concatène `baseUriGestion` avec une `baseURI` qui le contient déjà. Côté legacy,
 * `LegacyCollectionsRepository.collectionsValidation` et `ConceptsPublication.publishCollection`
 * écrivaient donc à la mauvaise IRI ; les triplets `isValidated true` n'étaient
 * jamais lisibles ni dans le graphe gestion ni dans le graphe publication.
 *
 * Les deux scénarios couverts ici :
 *  - cycles 1 + 2 : après validation, le dashboard reflète `isValidated=true` ;
 *  - cycles 3 + 4 : le graphe publication contient bien le triplet correspondant.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CollectionPublicationBug1453Test extends WithGraphDBContainer {

    private static final String CREATE_COLLECTION_REQUEST_JSON = """
            {
                 "id": "%s",
                 "labels": [{"value": "label fr", "lang": "fr"}],
                 "descriptions": [],
                 "creator" : "http://bauhaus/HIE000000",
                 "contributor" : "http://bauhaus/HIE000000",
                 "conceptsIdentifiers": ["c00001"]
             }
            """;

    private static final String BAUHAUS_TEST_PUBLICATION_REPOSITORY = "bauhaus-test-pub";

    @LocalServerPort
    int serverPort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        // Use a dedicated publication repository to mirror production (gestion and
        // publication are separate repos there). Sharing one repo would let the
        // dashboard SPARQL query (no graph filter) pick up both IRI prefixes.
        container.withInitFolder("/testcontainers").withRepository("config-pub.ttl");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> BAUHAUS_TEST_PUBLICATION_REPOSITORY);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.baseURI", () -> "http://id.insee.fr/");
        container.withInitFolder("fr/insee/rmes/modules/concepts/collections")
                .withTrigFiles("collections-end-to-end-test.trig");
    }

    @Test
    @DisplayName("Bug #1453 : publier une collection doit faire passer isValidated à true dans le dashboard")
    void publishing_a_collection_replaces_is_validated_false_with_true() {
        RestClient restClient = RestClient.create();
        String collectionId = "Collection-bug-1453";

        createCollection(restClient, collectionId);
        assertCollectionInDashboard(fetchDashboard(restClient), collectionId, "Unpublished", 1);

        validateCollection(restClient, collectionId);

        assertCollectionInDashboard(fetchDashboard(restClient), collectionId, "Validated", 1);
    }

    @Test
    @DisplayName("Bug #1453 (cycle 3) : après publication, le graphe publication doit contenir isValidated=true pour la collection")
    void publishing_a_collection_writes_is_validated_true_in_publication_graph() {
        RestClient restClient = RestClient.create();
        String collectionId = "Collection-bug-1453-pub";

        createCollection(restClient, collectionId);
        validateCollection(restClient, collectionId);

        boolean hasGestionIri = sparqlAsk(restClient, BAUHAUS_TEST_REPOSITORY,
                askValidatedWithPrefix(collectionId, "http://bauhaus/"));
        boolean hasPublicationIri = sparqlAsk(restClient, BAUHAUS_TEST_PUBLICATION_REPOSITORY,
                askValidatedWithPrefix(collectionId, "http://id.insee.fr/"));

        assertThat(hasGestionIri)
                .as("Le graphe gestion doit contenir <http://bauhaus/.../%s> insee:validationState 'Validated'", collectionId)
                .isTrue();
        assertThat(hasPublicationIri)
                .as("Le graphe publication doit contenir <http://id.insee.fr/.../%s> insee:validationState 'Validated'. "
                        + "Si absent, ConceptsPublication.publishCollection a filtré le triplet "
                        + "ou écrit une IRI doublée par tranformBaseURIToPublish.", collectionId)
                .isTrue();
    }

    private String collectionsEndpoint() {
        return "http://localhost:" + serverPort + "/api/concepts/collections";
    }

    private void createCollection(RestClient restClient, String collectionId) {
        var response = restClient.post()
                .uri(collectionsEndpoint())
                .body(CREATE_COLLECTION_REQUEST_JSON.formatted(collectionId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private void validateCollection(RestClient restClient, String collectionId) {
        var response = restClient.put()
                .uri(collectionsEndpoint() + "/" + collectionId + "/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .body("[\"%s\"]".formatted(collectionId))
                .retrieve()
                .toBodilessEntity();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private JSONArray fetchDashboard(RestClient restClient) {
        return new JSONArray(restClient
                .get().uri(collectionsEndpoint() + "/dashboard")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class));
    }

    private static void assertCollectionInDashboard(JSONArray dashboard, String id, String expectedValidationState, int expectedCount) {
        long matches = 0;
        for (int i = 0; i < dashboard.length(); i++) {
            JSONObject row = dashboard.getJSONObject(i);
            if (id.equals(row.getString("id"))) {
                matches++;
                assertThat(row.getString("validationState"))
                        .as("validationState for collection %s", id)
                        .isEqualTo(expectedValidationState);
            }
        }
        assertThat(matches)
                .as("Number of rows for collection %s in dashboard (duplicates indicate duplicated validationState triples — bug #1453)", id)
                .isEqualTo(expectedCount);
    }

    private static String askValidatedWithPrefix(String collectionId, String prefix) {
        return "PREFIX insee: <http://rdf.insee.fr/def/base#> "
                + "ASK { ?s insee:validationState 'Validated' . "
                + "FILTER(STRSTARTS(STR(?s), \"" + prefix + "\") "
                + "&& !STRSTARTS(STR(?s), \"" + prefix + "http://\") "
                + "&& CONTAINS(STR(?s), \"" + collectionId + "\")) }";
    }

    private static boolean sparqlAsk(RestClient restClient, String repository, String sparql) {
        String sparqlEndpoint = "http://" + container.getHost() + ":" + container.getMappedPort(7200)
                + "/repositories/" + repository;
        String json = restClient.post()
                .uri(sparqlEndpoint)
                .header("Accept", "application/sparql-results+json")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("query=" + URLEncoder.encode(sparql, StandardCharsets.UTF_8))
                .retrieve()
                .body(String.class);
        return new JSONObject(json).getBoolean("boolean");
    }
}
