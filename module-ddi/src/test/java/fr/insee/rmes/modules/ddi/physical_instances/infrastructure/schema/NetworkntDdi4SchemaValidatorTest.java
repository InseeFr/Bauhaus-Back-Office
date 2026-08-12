package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidDdi4JsonException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le schéma DDI 4 pèse 1,1 Mo : le lire, le parser et le compiler à chaque requête coûtait des
 * dizaines de secondes. Ces tests verrouillent le fait qu'il n'est payé qu'une fois.
 */
class NetworkntDdi4SchemaValidatorTest {

    private static final String STUB_SCHEMA =
            """
            {
              "$schema": "http://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "properties": {"items": {"type": "array"}},
              "required": ["items"],
              "additionalProperties": false
            }
            """;

    private static final String CONFORMING = """
            {"items": []}
            """;

    private static Ddi4SchemaRepository schemaOf(String document) {
        return () -> document;
    }

    @Test
    void shouldCompileTheSchemaOnlyOnceAcrossValidations() {
        AtomicInteger reads = new AtomicInteger();
        var validator = new NetworkntDdi4SchemaValidator(() -> {
            reads.incrementAndGet();
            return STUB_SCHEMA;
        });

        validator.validate(CONFORMING);
        validator.validate(CONFORMING);
        validator.validate(CONFORMING);

        assertEquals(1, reads.get(),
                "Le schéma doit être lu et compilé une seule fois, pas à chaque validation");
    }

    @Test
    void shouldReportNoErrorForAConformingDocument() {
        var validator = new NetworkntDdi4SchemaValidator(schemaOf(STUB_SCHEMA));

        assertEquals(List.of(), validator.validate(CONFORMING));
    }

    @Test
    void shouldReportTheSchemaErrors() {
        var validator = new NetworkntDdi4SchemaValidator(schemaOf(STUB_SCHEMA));

        List<String> errors = validator.validate("""
                {"items": [], "Variabel": []}
                """);

        assertEquals(1, errors.size(), "Erreurs remontées : " + errors);
        assertTrue(errors.getFirst().contains("Variabel"), "Erreurs remontées : " + errors);
    }

    /** Un document mal formé est une faute de l'appelant, pas une panne : elle doit être typée. */
    @Test
    void shouldRejectAMalformedDocument() {
        var validator = new NetworkntDdi4SchemaValidator(schemaOf(STUB_SCHEMA));

        assertThrows(InvalidDdi4JsonException.class, () -> validator.validate("pas du json"));
    }

    /** Un schéma illisible est une panne d'infrastructure : surtout pas un 400 déguisé. */
    @Test
    void shouldFailLoudlyWhenTheSchemaIsUnreadable() {
        var validator = new NetworkntDdi4SchemaValidator(schemaOf("ceci n'est pas un schéma"));

        assertThrows(IllegalStateException.class, () -> validator.validate(CONFORMING));
    }
}
