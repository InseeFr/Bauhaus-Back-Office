package fr.insee.rmes;

import fr.insee.rmes.modules.shared_kernel.domain.model.ConfiguredLanguages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BauhausLanguagesPropertiesTest.TestConfig.class)
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.lg1=fr",
        "fr.insee.rmes.bauhaus.lg2=en"
})
class BauhausLanguagesPropertiesTest {

    @Autowired
    private BauhausLanguagesProperties languages;

    @Test
    void shouldBindLg1AndLg2() {
        assertEquals("fr", languages.lg1());
        assertEquals("en", languages.lg2());
    }

    @Test
    void shouldExposeBothLanguagesToTheDomainWithoutSwappingThem() {
        assertEquals(new ConfiguredLanguages("fr", "en"), languages.toDomain());
    }

    @EnableConfigurationProperties(BauhausLanguagesProperties.class)
    static class TestConfig {
    }
}
