package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TitleSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void titleSerializesAsStringArrayOfMultilingualStringValue() throws JsonProcessingException {
        Title title = new Title(MultilingualStrings.of("fr-FR", "Mon titre"));

        String json = MAPPER.writeValueAsString(title);

        assertEquals(
                "{\"String\":[{\"MultilingualStringValue\":{\"LanguageTag\":\"fr-FR\",\"Value\":\"Mon titre\"}}]}",
                json);
    }

    @Test
    void variableNameSerializesAsStringArrayOfMultilingualStringValue() throws JsonProcessingException {
        VariableName variableName = new VariableName(MultilingualStrings.of("fr-FR", "VAR1"));

        String json = MAPPER.writeValueAsString(variableName);

        assertEquals(
                "{\"String\":[{\"MultilingualStringValue\":{\"LanguageTag\":\"fr-FR\",\"Value\":\"VAR1\"}}]}",
                json);
    }

    @Test
    void titleDoesNotEmitLegacyProperties() throws JsonProcessingException {
        Title title = new Title(MultilingualStrings.of("fr-FR", "x"));

        String json = MAPPER.writeValueAsString(title);

        assertThat(json).doesNotContain("@xml:lang").doesNotContain("#text");
    }
}
