package fr.insee.rmes.testcontainers.e2e.operations;

import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyResourcesE2ETest extends BaseE2ETest {

    @Test
    void testGetFamilies() {
        var familyId = new Object() {
            String value;
        };

        RestTestClient.ResponseSpec response = restTestClient.get()
                .uri("/operations/families")
                .headers(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .exchange();
        URI requestUrl = response.returnResult().getUrl();
        response
                .expectStatus().isOk()
                .expectBody().jsonPath("$").isArray()
                .jsonPath("$[0].id").value(String.class, id -> {
                    assertThat(id).isNotEmpty();
                    familyId.value = id;
                })
                .jsonPath("$[0].label").isNotEmpty()
                .jsonPath("$[0]._links.self.href").isEqualTo(requestUrl.resolve("/api/operations/family/" + familyId.value).toString());
    }

    @Test
    void testGetFamilyById() {

        RestTestClient.ResponseSpec response = restTestClient.get()
                .uri("/operations/family/s88")
                .headers(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .exchange();
        URI requestUrl = response.returnResult().getUrl();
        response
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("s88")
                .jsonPath("$.prefLabelLg1").value(String.class, pref -> assertThat(pref == null || "Voir également".equals(pref)).isTrue())
                .jsonPath("$.series[1]").exists()
                .jsonPath("$.series[0]._links.self.href").isEqualTo(requestUrl.resolve("/api/operations/series/s1033").toString());
    }

}