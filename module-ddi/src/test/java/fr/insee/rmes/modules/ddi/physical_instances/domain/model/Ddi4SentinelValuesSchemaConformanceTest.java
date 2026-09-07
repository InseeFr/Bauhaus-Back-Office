package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema.ClasspathDdi4SchemaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valeurs sentinelles (#1566) — garde-fou « génération à partir du schéma » : la sérialisation
 * Jackson des modèles DDI 4 portant les sentinelles doit rester conforme au {@code ddi-schema.json}
 * (le même que sert {@code GET /ddi/schema} et qu'utilise {@code POST /ddi/validate}, et dont sont
 * générés les types du front).
 */
class Ddi4SentinelValuesSchemaConformanceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private static JsonNode schemaRoot;

    @BeforeAll
    static void loadSchema() throws Exception {
        schemaRoot = MAPPER.readTree(new ClasspathDdi4SchemaRepository().schemaDocument());
    }

    /** Valide {@code value} sérialisé par Jackson contre {@code #/$defs/<defName>} du schéma DDI 4. */
    private static Set<ValidationMessage> validateAgainstDef(Object value, String defName) throws Exception {
        ObjectNode wrapper = MAPPER.createObjectNode();
        wrapper.put("$ref", "#/$defs/" + defName);
        wrapper.set("$defs", schemaRoot.get("$defs"));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                .getSchema(wrapper);

        return schema.validate(MAPPER.readTree(MAPPER.writeValueAsString(value)));
    }

    @Test
    void missingValuesReference_matchesSchemaReferenceShape() throws Exception {
        Reference mmvrRef = Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation");

        assertThat(validateAgainstDef(mmvrRef, "reference")).isEmpty();
    }

    @Test
    void variableWithMissingValuesReference_matchesSchemaVariableDef() throws Exception {
        CodeRepresentation codeRepresentation = new CodeRepresentation(
                CodeRepresentation.TYPE,
                null,
                Reference.of("fr.insee", "cl-1", "1", "CodeList"));
        Ddi4Variable variable = new Ddi4Variable(
                Ddi4Variable.TYPE,
                null,
                "urn:ddi:fr.insee:var-1:1",
                "fr.insee", "var-1", "1",
                null,
                LangStrings.of("fr-FR", "AGEMEN8"),
                LangStrings.of("fr-FR", "Âge détaillé"),
                null,
                new VariableRepresentation(
                        null,
                        codeRepresentation,
                        null, null, null,
                        Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation")),
                null);

        assertThat(validateAgainstDef(variable, "Variable")).isEmpty();
    }

    @Test
    void managedMissingValuesRepresentation_matchesItsSchemaDef() throws Exception {
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                null,
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee", "mmvr-1", "1",
                LangStrings.of("fr-FR", "Valeurs sentinelles NSP/REF"),
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE,
                        null,
                        Reference.of("fr.insee", "cl-sentinelles-1", "1", "CodeList"))));

        assertThat(validateAgainstDef(mmvr, "ManagedMissingValuesRepresentation")).isEmpty();
    }
}
