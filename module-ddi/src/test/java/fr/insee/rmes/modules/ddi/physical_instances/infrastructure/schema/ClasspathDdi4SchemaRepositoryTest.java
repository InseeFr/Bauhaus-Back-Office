package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

class ClasspathDdi4SchemaRepositoryTest {

    /**
     * Le fichier livré porte un BOM UTF-8, que Jackson refuse. Le retirer ici plutôt qu'au moment de
     * la validation, c'est le retirer une fois pour tous les usages du schéma — validation comme
     * exposition sur {@code GET /ddi/schema}.
     */
    @Test
    void shouldStripTheByteOrderMark() {
        var repository = new ClasspathDdi4SchemaRepository("schema-with-bom.json");

        String document = repository.schemaDocument();

        assertFalse(document.startsWith("﻿"), "Le BOM doit être retiré");
        assertTrue(document.startsWith("{"), "Document lu : " + document);
    }

    @Test
    void shouldFailWhenTheSchemaResourceIsMissing() {
        var repository = new ClasspathDdi4SchemaRepository("schema-qui-nexiste-pas.json");

        assertThrows(UncheckedIOException.class, repository::schemaDocument);
    }

    /** Le vrai schéma est livré dans ce module : il doit être lisible tel quel. */
    @Test
    void shouldReadTheShippedSchema() {
        var repository = new ClasspathDdi4SchemaRepository();

        assertTrue(repository.schemaDocument().startsWith("{"));
    }
}
