package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultilingualStringSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void serializesMultilingualStringValueWithLanguageTagAndValue() throws JsonProcessingException {
        MultilingualStringValue value = new MultilingualStringValue("fr-FR", "Libellé");

        String json = MAPPER.writeValueAsString(value);

        assertEquals("{\"LanguageTag\":\"fr-FR\",\"Value\":\"Libellé\"}", json);
    }

    @Test
    void serializesMultilingualStringEntryWithNestedMultilingualStringValue() throws JsonProcessingException {
        MultilingualStringEntry entry = new MultilingualStringEntry(new MultilingualStringValue("fr-FR", "Libellé"));

        String json = MAPPER.writeValueAsString(entry);

        assertEquals("{\"MultilingualStringValue\":{\"LanguageTag\":\"fr-FR\",\"Value\":\"Libellé\"}}", json);
    }

    @Test
    void multilingualStringsHelperBuildsSingleEntryList() {
        List<MultilingualStringEntry> entries = MultilingualStrings.of("en-US", "Label");

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).value().languageTag()).isEqualTo("en-US");
        assertThat(entries.get(0).value().value()).isEqualTo("Label");
    }

    @Test
    void doesNotEmitXmlLangOrTextProperties() throws JsonProcessingException {
        MultilingualStringEntry entry = new MultilingualStringEntry(new MultilingualStringValue("fr-FR", "x"));

        String json = MAPPER.writeValueAsString(entry);

        assertThat(json).doesNotContain("@xml:lang").doesNotContain("#text");
    }
}
