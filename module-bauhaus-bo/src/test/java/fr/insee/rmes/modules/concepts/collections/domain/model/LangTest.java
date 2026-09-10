package fr.insee.rmes.modules.concepts.collections.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import org.junit.jupiter.api.Test;

class LangTest {

    @Test
    void default_language_should_be_fr() {
        assertThat(Lang.defaultLanguage()).isEqualTo(Lang.FR);
    }

    @Test
    void alternative_language_should_be_en() {
        assertThat(Lang.alternativeLanguage()).isEqualTo(Lang.EN);
    }
}
