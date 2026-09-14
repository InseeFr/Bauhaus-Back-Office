package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema;

import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Lit le JSON Schema DDI 4 livré dans les ressources du module.
 * <p>
 * Le fichier porte un BOM UTF-8 que Jackson refuse : il est retiré ici, une fois pour tous les
 * usages du schéma — la validation comme l'exposition sur {@code GET /ddi/schema}.
 */
@ServerSideAdaptor
public class ClasspathDdi4SchemaRepository implements Ddi4SchemaRepository {

    private static final String DEFAULT_RESOURCE = "ddi-schema.json";
    private static final char BYTE_ORDER_MARK = '﻿';

    private final String resourceName;

    public ClasspathDdi4SchemaRepository() {
        this(DEFAULT_RESOURCE);
    }

    ClasspathDdi4SchemaRepository(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public String schemaDocument() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new UncheckedIOException(new FileNotFoundException("Schéma DDI 4 introuvable : " + resourceName));
            }
            String document = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return document.isEmpty() || document.charAt(0) != BYTE_ORDER_MARK ? document : document.substring(1);
        } catch (IOException e) {
            throw new UncheckedIOException("Schéma DDI 4 illisible : " + resourceName, e);
        }
    }
}
