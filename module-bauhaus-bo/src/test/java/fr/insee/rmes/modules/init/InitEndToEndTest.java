package fr.insee.rmes.modules.init;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InitEndToEndTest {

    @LocalServerPort
    int serverPort;

    @Test
    @DisplayName("Should return init properties with all fields including colecticaLangs")
    void should_return_init_properties() {
        String initEndpoint = "http://localhost:" + serverPort + "/api/init";
        RestClient restClient = RestClient.create(initEndpoint);

        var entityResponse = restClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThat(entityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entityResponse.getBody()).isNotNull();

        JSONObject props = new JSONObject(entityResponse.getBody());

        assertThat(props.has("appHost")).isTrue();
        assertThat(props.has("defaultContributor")).isTrue();
        assertThat(props.has("maxLengthScopeNote")).isTrue();
        assertThat(props.has("lg1")).isTrue();
        assertThat(props.has("lg2")).isTrue();
        assertThat(props.has("authType")).isTrue();
        assertThat(props.has("activeModules")).isTrue();
        assertThat(props.has("modules")).isTrue();
        assertThat(props.has("version")).isTrue();
        assertThat(props.has("extraMandatoryFields")).isTrue();
        assertThat(props.has("defaultAgencyId")).isTrue();
        assertThat(props.has("colecticaLangs")).isTrue();

        assertThat(props.getJSONArray("colecticaLangs")).isNotNull();
        assertThat(props.getJSONArray("colecticaLangs").length()).isEqualTo(2);
        assertThat(props.getJSONArray("colecticaLangs").getString(0)).isEqualTo("fr-FR");
        assertThat(props.getJSONArray("colecticaLangs").getString(1)).isEqualTo("en-GB");
    }
}
