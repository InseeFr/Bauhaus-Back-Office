package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Valide un document DDI 4 contre {@code ddi-schema.json}.
 * <p>
 * Le schéma pèse 1,1 Mo et déclare 161 types : le lire, le parser et le compiler coûte cher, il
 * n'est donc payé qu'une fois puis réutilisé. Deux réglages font l'essentiel du gain :
 * <ul>
 *   <li>la compilation est mémorisée (avant, chaque requête repartait du fichier) ;</li>
 *   <li>{@code preloadJsonSchema(false)} : la résolution <em>eager</em> de tous les {@code $ref}
 *       du schéma prenait à elle seule une vingtaine de secondes. En résolution paresseuse, seules
 *       les branches réellement traversées par le document sont montées — même verdict, pour
 *       quelques millisecondes.</li>
 * </ul>
 * Contrepartie de la résolution paresseuse : un {@code $ref} cassé n'est plus détecté à la
 * compilation mais à la première validation qui l'emprunte. Le schéma est une ressource interne
 * sans référence distante, le risque est théorique.
 * <p>
 * L'instance compilée est partagée entre requêtes : {@code JsonSchema} est documenté comme
 * thread-safe tant que sa configuration n'est pas modifiée, et la résolution paresseuse des
 * {@code $ref} se synchronise sur la fabrique.
 */
@Component
public class Ddi4SchemaValidator {

    private static final String SCHEMA_RESOURCE = "ddi-schema.json";

    private final Supplier<String> schemaSource;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Compilé à la première validation, puis réutilisé. */
    private volatile JsonSchema schema;

    public Ddi4SchemaValidator() {
        this(Ddi4SchemaValidator::readSchemaFromClasspath);
    }

    Ddi4SchemaValidator(Supplier<String> schemaSource) {
        this.schemaSource = schemaSource;
    }

    /**
     * @return les messages d'erreur du schéma, vide si le document est conforme
     * @throws JsonProcessingException si {@code json} n'est pas du JSON bien formé
     */
    public List<String> validate(String json) throws JsonProcessingException {
        JsonNode document = mapper.readTree(json);
        return compiledSchema()
            .validate(document)
            .stream()
            .map(ValidationMessage::getMessage)
            .toList();
    }

    private JsonSchema compiledSchema() {
        JsonSchema compiled = this.schema;
        if (compiled == null) {
            synchronized (this) {
                compiled = this.schema;
                if (compiled == null) {
                    compiled = compile();
                    this.schema = compiled;
                }
            }
        }
        return compiled;
    }

    private JsonSchema compile() {
        String content = schemaSource.get();
        // Le fichier livré porte un BOM UTF-8, que Jackson refuse.
        if (content.startsWith("﻿")) {
            content = content.substring(1);
        }
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
            .preloadJsonSchema(false)
            .build();
        try {
            JsonNode schemaNode = mapper.readTree(content);
            return JsonSchemaFactory.getInstance(
                SpecVersion.VersionFlag.V202012
            ).getSchema(schemaNode, config);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "Schéma DDI 4 illisible : " + SCHEMA_RESOURCE,
                e
            );
        }
    }

    private static String readSchemaFromClasspath() {
        try (
            InputStream is = new ClassPathResource(
                SCHEMA_RESOURCE
            ).getInputStream()
        ) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Schéma DDI 4 introuvable : " + SCHEMA_RESOURCE,
                e
            );
        }
    }
}
