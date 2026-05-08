package fr.insee.rmes.modules.commons;

import fr.insee.rmes.testcontainers.WithGraphDBContainer;
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
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("fr/insee/rmes/modules/commons")
                .withTrigFiles("themes-end-to-end-test.trig");
    }

    @Test
    @DisplayName("Fetch themes returns all themes")
    void ok_when_fetching_themes() {
        RestClient restClient = RestClient.create("http://localhost:" + serverPort + "/api");

        var response = restClient.get().uri("/themes")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        JSONAssert.assertEquals("""
                [
                  { "uri": "http://id.insee.fr/concepts/theme/t001" },
                  { "uri": "http://id.insee.fr/concepts/theme/t002" },
                  { "uri": "http://id.insee.fr/concepts/theme/t003" },
                  { "uri": "http://id.insee.fr/concepts/theme/t004" }
                ]
                """, response, false);
    }
}