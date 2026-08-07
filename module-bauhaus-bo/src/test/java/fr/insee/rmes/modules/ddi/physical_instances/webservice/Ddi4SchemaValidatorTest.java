package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Le schéma DDI 4 pèse 1,1 Mo : le lire, le parser et le compiler à chaque requête coûtait des
 * dizaines de secondes. Ces tests verrouillent le fait qu'il n'est payé qu'une fois.
 */
class Ddi4SchemaValidatorTest {

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

    @Test
    void shouldCompileTheSchemaOnlyOnceAcrossValidations() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        Ddi4SchemaValidator validator = new Ddi4SchemaValidator(() -> {
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
    void shouldReportNoErrorForAConformingDocument() throws Exception {
        Ddi4SchemaValidator validator = new Ddi4SchemaValidator(() -> STUB_SCHEMA);

        assertEquals(List.of(), validator.validate(CONFORMING));
    }

    @Test
    void shouldReportTheSchemaErrors() throws Exception {
        Ddi4SchemaValidator validator = new Ddi4SchemaValidator(() -> STUB_SCHEMA);

        List<String> errors = validator.validate("""
                {"items": [], "Variabel": []}
                """);

        assertEquals(1, errors.size(), "Erreurs remontées : " + errors);
        assertTrue(errors.getFirst().contains("Variabel"), "Erreurs remontées : " + errors);
    }

    /** Le fichier livré porte un BOM UTF-8 : Jackson le refuse tel quel. */
    @Test
    void shouldStripTheByteOrderMarkOfTheSchema() throws Exception {
        Ddi4SchemaValidator validator = new Ddi4SchemaValidator(() -> "﻿" + STUB_SCHEMA);

        assertEquals(List.of(), validator.validate(CONFORMING));
    }

    @Test
    void shouldRejectAMalformedDocument() {
        Ddi4SchemaValidator validator = new Ddi4SchemaValidator(() -> STUB_SCHEMA);

        assertThrows(JsonProcessingException.class, () -> validator.validate("pas du json"));
    }
}
