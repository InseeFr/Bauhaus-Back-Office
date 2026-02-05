package fr.insee.rmes.modules.commons.webservice;

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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DisseminationStatusEndToEndTest extends WithGraphDBContainer {

    final static String EXPECTED_DISSEMINATION_STATUS_JSON = """
            [
              {
                "label": "Privé",
                "url": "http://id.insee.fr/codes/base/statutDiffusion/Prive"
              },
              {
                "label": "Public générique",
                "url": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique"
              },
              {
                "label": "Public spécifique",
                "url": "http://id.insee.fr/codes/base/statutDiffusion/PublicSpecifique"
              }
            ]
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
    @DisplayName("Fetch dissemination status list with label and url")
    void should_return_dissemination_status_with_label_and_url() {
        String disseminationStatusEndpoint = "http://localhost:" + serverPort + "/api/disseminationStatus";
        RestClient restClient = RestClient.create(disseminationStatusEndpoint);

        var fetchedDisseminationStatus = restClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThat(fetchedDisseminationStatus).isNotNull();
        JSONAssert.assertEquals(EXPECTED_DISSEMINATION_STATUS_JSON, fetchedDisseminationStatus, true);
    }
}