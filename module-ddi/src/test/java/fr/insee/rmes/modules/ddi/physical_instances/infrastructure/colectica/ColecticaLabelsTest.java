package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ColecticaLabelsTest {

    private final ColecticaLabels labels = new ColecticaLabels(List.of("en-GB", "fr-FR"));

    private static Map<String, String> orderedLabels(String... langAndValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < langAndValues.length; i += 2) {
            map.put(langAndValues[i], langAndValues[i + 1]);
        }
        return map;
    }

    @Test
    void firstNonBlankPrefersAnyConfiguredLanguageOverAnUnconfiguredOne() {
        Map<String, String> label = orderedLabels("de-DE", "Etikett", "fr-FR", "Libellé");

        assertThat(labels.firstNonBlank(label)).contains("Libellé");
    }

    @Test
    void fromLanguageMapPrefersAnyConfiguredLanguageOverAnUnconfiguredOne() {
        Map<String, String> label = orderedLabels("de-DE", "Etikett", "fr-FR", "Libellé");

        assertThat(labels.fromLanguageMap(label)).isEqualTo("Libellé");
    }

    @Test
    void defaultLangIsTheFirstConfiguredLanguage() {
        assertThat(labels.defaultLang()).isEqualTo("en-GB");
    }
}
