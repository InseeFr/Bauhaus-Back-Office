package fr.insee.rmes;

import fr.insee.rmes.modules.shared_kernel.domain.model.ConfiguredLanguages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    @DisplayName("A missing lg1 is rejected")
    void ko_when_lg1_is_missing() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new BauhausLanguagesProperties(null, "en"));

        assertThat(exception).hasMessageContaining("fr.insee.rmes.bauhaus.lg1");
    }

    @Test
    @DisplayName("A blank lg2 is rejected")
    void ko_when_lg2_is_blank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new BauhausLanguagesProperties("fr", "  "));

        assertThat(exception).hasMessageContaining("fr.insee.rmes.bauhaus.lg2");
    }

    @Test
    @DisplayName("Both languages configured is accepted")
    void ok_when_both_languages_are_provided() {
        assertDoesNotThrow(() -> new BauhausLanguagesProperties("fr", "en"));
    }

    @Test
    @DisplayName("The application refuses to start when lg1 is not configured")
    void ko_when_starting_without_lg1() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues("fr.insee.rmes.bauhaus.lg2=en")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining("fr.insee.rmes.bauhaus.lg1 must be configured"));
    }

    @Test
    @DisplayName("The application refuses to start when lg2 is not configured")
    void ko_when_starting_without_lg2() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues("fr.insee.rmes.bauhaus.lg1=fr")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining("fr.insee.rmes.bauhaus.lg2 must be configured"));
    }

    @Test
    @DisplayName("The application starts and binds both languages when they are configured")
    void ok_when_starting_with_a_complete_configuration() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues(
                        "fr.insee.rmes.bauhaus.lg1=fr",
                        "fr.insee.rmes.bauhaus.lg2=en")
                .run(context -> assertThat(context).hasNotFailed()
                        .getBean(BauhausLanguagesProperties.class)
                        .isEqualTo(new BauhausLanguagesProperties("fr", "en")));
    }

    @Configuration
    @EnableConfigurationProperties(BauhausLanguagesProperties.class)
    static class TestConfig {
    }
}
