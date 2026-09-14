package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = PaginationPropertiesTest.TestConfig.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.per_page=42")
class PaginationPropertiesTest {

    @Autowired
    private PaginationProperties paginationProperties;

    @Test
    void shouldBindPerPage() {
        assertEquals(42, paginationProperties.perPage());
    }

    @EnableConfigurationProperties(PaginationProperties.class)
    static class TestConfig {}
}
