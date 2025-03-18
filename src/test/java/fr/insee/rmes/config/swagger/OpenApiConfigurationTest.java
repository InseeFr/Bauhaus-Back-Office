package fr.insee.rmes.config.swagger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigurationTest {

    @Test
    void shouldDisplayCorrectOpenAPI() {

        OpenApiConfiguration myOpenAPI = new OpenApiConfiguration();
        String actual = myOpenAPI.openAPI("v421").toString();

        String expected = "class OpenAPI {\n" +
                "    openapi: 3.0.1\n" +
                "    info: class Info {\n" +
                "        title: Bauhaus\n" +
                "        description: Back office de Bauhaus (rmesgncs)\n" +
                "        summary: null\n" +
                "        termsOfService: null\n" +
                "        contact: null\n" +
                "        license: null\n" +
                "        version: v421\n" +
                "    }\n" +
                "    externalDocs: null\n" +
                "    servers: null\n" +
                "    security: null\n" +
                "    tags: null\n" +
                "    paths: null\n" +
                "    components: null\n" +
                "}";

        assertEquals(expected,actual);
    }

}