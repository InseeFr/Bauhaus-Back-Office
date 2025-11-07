package fr.insee.rmes.modules.concepts.collections.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LangTest {

    @Test
    void defaultLanguageShouldBeFR(){
        assertThat(Lang.defaultLanguage()).isEqualTo(Lang.FR);
    }

    @Test
    void alternativeLanguageShouldBeEN(){
        assertThat(Lang.alternativeLanguage()).isEqualTo(Lang.EN);
    }

}