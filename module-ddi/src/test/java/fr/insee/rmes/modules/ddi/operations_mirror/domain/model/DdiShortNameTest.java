package fr.insee.rmes.modules.ddi.operations_mirror.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DdiShortNameTest {

    @ParameterizedTest
    @CsvSource({
        "Enquête emploi, LP-ENQUETEEMPLOI",
        "  Recensement  de la population , LP-RECENSEMENTDELAPOPULATION",
        "RP, LP-RP",
        "Enquête Loyers & Charges, LP-ENQUETELOYERS&CHARGES"
    })
    void stripsSpacesAndDiacriticsThenUppercasesAndPrefixes(String shortLabel, String expected) {
        assertThat(DdiShortName.prefixed("LP-", shortLabel)).isEqualTo(expected);
    }

    @Test
    void usesTheVariableSchemePrefixWhenAsked() {
        assertThat(DdiShortName.prefixed("VS-", "Enquête emploi")).isEqualTo("VS-ENQUETEEMPLOI");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void hasNoNameWhenTheLabelIsMissing(String shortLabel) {
        assertThat(DdiShortName.prefixed("LP-", shortLabel)).isNull();
    }
}
