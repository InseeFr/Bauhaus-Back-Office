package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LangStringSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void serializesLangStringWithAtLanguageAndAtValue() throws JsonProcessingException {
        LangString langString = new LangString("fr-FR", "Libellé");

        String json = MAPPER.writeValueAsString(langString);

        assertEquals("{\"@language\":\"fr-FR\",\"@value\":\"Libellé\"}", json);
    }

    @Test
    void serializesListOfLangStringAsBareArray() throws JsonProcessingException {
        List<LangString> langStrings = List.of(new LangString("fr-FR", "Libellé"));

        String json = MAPPER.writeValueAsString(langStrings);

        assertEquals("[{\"@language\":\"fr-FR\",\"@value\":\"Libellé\"}]", json);
    }

    @Test
    void doesNotEmitLegacyProperties() throws JsonProcessingException {
        LangString langString = new LangString("fr-FR", "x");

        String json = MAPPER.writeValueAsString(langString);

        assertThat(json)
                .doesNotContain("LanguageTag")
                .doesNotContain("MultilingualStringValue")
                .doesNotContain("@xml:lang")
                .doesNotContain("#text");
    }
}
