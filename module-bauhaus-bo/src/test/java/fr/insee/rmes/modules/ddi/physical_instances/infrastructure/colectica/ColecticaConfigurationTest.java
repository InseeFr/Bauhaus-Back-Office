package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.exceptions.InvalidColecticaConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ColecticaConfigurationTest {

    private static final ColecticaConfiguration.ColecticaInstanceConfiguration VALID_SERVER =
            new ColecticaConfiguration.ColecticaInstanceConfiguration(
                    "https://example.com",
                    "/api/v1/",
                    List.of("type1"),
                    "resp",
                    "format",
                    "password",
                    "user",
                    "pass",
                    "fr.insee"
            );

    @Test
    void should_throw_exception_when_langs_is_null() {
        assertThatThrownBy(() ->
                new ColecticaConfiguration(
                        null,
                        VALID_SERVER,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        )
                .isInstanceOf(InvalidColecticaConfigurationException.class)
                .hasMessage("langs cannot be null or empty");
    }

    @Test
    void should_throw_exception_when_langs_is_empty() {
        assertThatThrownBy(() ->
                new ColecticaConfiguration(
                        Collections.emptyList(),
                        VALID_SERVER,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        )
                .isInstanceOf(InvalidColecticaConfigurationException.class)
                .hasMessage("langs cannot be null or empty");
    }

    @Test
    void should_throw_exception_when_lang_code_is_invalid() {
        assertThatThrownBy(() ->
                new ColecticaConfiguration(
                        List.of("invalid-code"),
                        VALID_SERVER,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        )
                .isInstanceOf(InvalidColecticaConfigurationException.class)
                .hasMessageContaining("Invalid language code: 'invalid-code'")
                .hasMessageContaining("Expected format: xx-XX");
    }

    @Test
    void should_throw_exception_when_lang_code_has_wrong_case() {
        assertThatThrownBy(() ->
                new ColecticaConfiguration(
                        List.of("FR-fr"),
                        VALID_SERVER,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        )
                .isInstanceOf(InvalidColecticaConfigurationException.class)
                .hasMessageContaining("Invalid language code: 'FR-fr'");
    }

    @Test
    void should_accept_valid_language_codes() {
        assertDoesNotThrow(() ->
                new ColecticaConfiguration(
                        List.of("fr-FR", "en-GB", "de-DE"),
                        VALID_SERVER,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        );
    }

    @Test
    void should_accept_single_valid_language_code() {
        assertDoesNotThrow(() ->
                new ColecticaConfiguration(
                        List.of("fr-FR"),
                        VALID_SERVER,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        );
    }
}
