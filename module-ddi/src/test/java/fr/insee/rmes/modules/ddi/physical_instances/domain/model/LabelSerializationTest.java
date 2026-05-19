package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void labelSerializesAsContentArrayOfMultilingualStringValue() throws JsonProcessingException {
        Label label = new Label(MultilingualStrings.of("fr-FR", "Mon libellé"));

        String json = MAPPER.writeValueAsString(label);

        assertEquals(
                "{\"Content\":[{\"MultilingualStringValue\":{\"LanguageTag\":\"fr-FR\",\"Value\":\"Mon libellé\"}}]}",
                json);
    }

    @Test
    void descriptionSerializesAsContentArrayOfMultilingualStringValue() throws JsonProcessingException {
        Description description = new Description(MultilingualStrings.of("fr-FR", "Une description"));

        String json = MAPPER.writeValueAsString(description);

        assertEquals(
                "{\"Content\":[{\"MultilingualStringValue\":{\"LanguageTag\":\"fr-FR\",\"Value\":\"Une description\"}}]}",
                json);
    }

    @Test
    void labelDoesNotEmitLegacyProperties() throws JsonProcessingException {
        Label label = new Label(MultilingualStrings.of("fr-FR", "x"));

        String json = MAPPER.writeValueAsString(label);

        assertThat(json).doesNotContain("@xml:lang").doesNotContain("#text");
    }
}
