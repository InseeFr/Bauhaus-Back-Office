package fr.insee.rmes.modules.concepts.collections;

import fr.insee.rmes.json.JSONUtils;
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

/**
 * Régression : la validation du pattern d'ID introduite récemment (commit qui ajoute
 * la possibilité de définir un ID arbitraire à la création) ne doit pas casser la
 * lecture des collections déjà persistées avec un ID legacy non conforme
 * (underscore, point, etc.). Le pattern n'est légitime qu'à l'entrée (POST).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LegacyCollectionIdReadTest extends WithGraphDBContainer {

    @LocalServerPort
    int serverPort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("fr/insee/rmes/modules/concepts/collections")
                .withTrigFiles("legacy-collection-with-underscore.trig");
    }

    @Test
    @DisplayName("GET /collections renvoie 200 avec une collection legacy dont l'ID contient un underscore")
    void listing_collections_does_not_500_on_legacy_id_with_underscore() {
        RestClient restClient = RestClient.create();
        String url = "http://localhost:" + serverPort + "/api/concepts/collections";

        var response = restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONArray rows = new JSONArray(response.getBody());
        boolean foundLegacy = JSONUtils.stream(rows)
                .anyMatch(row -> "Legacy_collection_001".equals(row.getString("id")));
        assertThat(foundLegacy)
                .as("La collection legacy 'Legacy_collection_001' (ID avec underscore) doit être listée comme les autres")
                .isTrue();
    }
}
