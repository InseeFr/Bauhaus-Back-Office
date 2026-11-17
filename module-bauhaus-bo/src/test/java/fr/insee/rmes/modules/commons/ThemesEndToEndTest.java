package fr.insee.rmes.modules.commons;

import static fr.insee.rmes.testcontainers.GraphDbTestProperties.registerGestion;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemesEndToEndTest extends WithGraphDBContainer {

    @LocalServerPort
    int serverPort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registerGestion(registry);
        container.withInitFolder("fr/insee/rmes/modules/commons").withTrigFiles("themes-end-to-end-test.trig");
    }

    @Test
    @DisplayName("Fetch themes returns only the themes, not the other concepts of the graph")
    void ok_when_fetching_themes() throws JSONException {
        RestClient restClient = RestClient.create("http://localhost:" + serverPort + "/api");

        var response = restClient
                .get()
                .uri("/themes")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        JSONAssert.assertEquals("""
                [
                  { "uri": "http://bauhaus/concepts/theme/t001", "label": { "value": "Agriculture", "lang": "FR" } },
                  { "uri": "http://bauhaus/concepts/theme/t003", "label": { "value": "Démographie", "lang": "FR" } },
                  { "uri": "http://bauhaus/concepts/theme/t004", "label": { "value": "Emploi", "lang": "FR" } },
                  { "uri": "http://bauhaus/concepts/theme/t002", "label": { "value": "Économie", "lang": "FR" } }
                ]
                """, response, true);
    }
}
