package fr.insee.rmes.config.swagger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigurationTest {

    @Test
    void shouldDisplayCorrectOpenAPI() {

        OpenApiConfiguration myOpenAPI = new OpenApiConfiguration();
        String actual = myOpenAPI.openAPI("v421").toString().replaceAll("\\s+", "");

        String expected = "class OpenAPI {" + "openapi: 3.0.1" + "info: class Info {" + "title: Bauhaus" + "description: Back office de Bauhaus (rmesgncs)" + "summary: null" + "termsOfService: null" + "contact: null" + "license: null" + "version: v421" + "}" + "externalDocs: null" + "servers: null" + "security: null" + "tags: null" + "paths: null" + "components: null" + "}";

        assertEquals(expected.replaceAll("\\s+", ""),actual);
    }

}