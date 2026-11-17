package fr.insee.rmes.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class FusekiContainerTest extends WithFusekiContainer {

    private static final String SCHEME = "<http://codelist/CL_FUSEKI_CONTAINER>";

    @Test
    void should_load_a_trig_fixture_into_its_named_graph() throws Exception {
        fixtureLoader().withTrigFiles("fuseki-container-it.trig");

        assertThat(ask("ASK { GRAPH <http://rdf.insee.fr/graphes/codes> { " + SCHEME + " ?p ?o } }"))
                .isTrue();
    }

    @Test
    void should_expose_named_graphs_through_the_default_graph() throws Exception {
        fixtureLoader().withTrigFiles("fuseki-container-it.trig");

        assertThat(ask("ASK { " + SCHEME + " ?p ?o }")).isTrue();
    }

    @Test
    void should_empty_the_dataset_on_reset() throws Exception {
        fixtureLoader().withTrigFiles("fuseki-container-it.trig");

        container.resetTestData();

        assertThat(ask("ASK { GRAPH ?g { ?s ?p ?o } }")).isFalse();
    }

    private static boolean ask(String query) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(container.getDatasetUrl() + "/sparql?query="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8)))
                .header("Accept", "application/sparql-results+json")
                .GET()
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            return new JSONObject(response.body()).getBoolean("boolean");
        }
    }
}
