package fr.insee.rmes.modules.commons.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ThemePropertiesTest {

    @Test
    @DisplayName("A missing theme type is rejected")
    void ko_when_type_is_missing() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new ThemeProperties("concepts", null));

        assertThat(exception).hasMessageContaining("fr.insee.rmes.bauhaus.theme.type");
    }

    @Test
    @DisplayName("A blank theme type is rejected")
    void ko_when_type_is_blank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new ThemeProperties("concepts", "  "));

        assertThat(exception).hasMessageContaining("fr.insee.rmes.bauhaus.theme.type");
    }

    @Test
    @DisplayName("A missing theme graph is rejected")
    void ko_when_graph_is_missing() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ThemeProperties(null, "http://bauhaus/concepts/themes/Theme"));

        assertThat(exception).hasMessageContaining("fr.insee.rmes.bauhaus.theme.graph");
    }

    @Test
    @DisplayName("A fully configured theme is accepted")
    void ok_when_graph_and_type_are_provided() {
        assertDoesNotThrow(() -> new ThemeProperties("concepts", "http://bauhaus/concepts/themes/Theme"));
    }

    @Test
    @DisplayName("The application refuses to start when the theme type is not configured")
    void ko_when_starting_without_type() {
        new ApplicationContextRunner()
                .withUserConfiguration(ThemePropertiesConfiguration.class)
                .withPropertyValues("fr.insee.rmes.bauhaus.theme.graph=concepts")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining("fr.insee.rmes.bauhaus.theme.type"));
    }

    @Test
    @DisplayName("The application starts and binds both properties when they are configured")
    void ok_when_starting_with_a_complete_configuration() {
        new ApplicationContextRunner()
                .withUserConfiguration(ThemePropertiesConfiguration.class)
                .withPropertyValues(
                        "fr.insee.rmes.bauhaus.theme.graph=concepts",
                        "fr.insee.rmes.bauhaus.theme.type=http://bauhaus/concepts/themes/Theme")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .getBean(ThemeProperties.class)
                        .isEqualTo(new ThemeProperties("concepts", "http://bauhaus/concepts/themes/Theme")));
    }

    @Configuration
    @EnableConfigurationProperties(ThemeProperties.class)
    static class ThemePropertiesConfiguration {}
}
