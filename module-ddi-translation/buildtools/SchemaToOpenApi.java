import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

/**
 * Build helper (run in JDK source-file mode by exec-maven-plugin).
 *
 * Wraps the DDI 4 JSON Schema (ddi-schema.json, draft 2020-12) into a minimal OpenAPI 3.1
 * document so that openapi-generator emits one Java class per "$defs" entry. OpenAPI 3.1 is
 * built on JSON Schema 2020-12, so all keywords ("$defs", "anyOf", ...) are understood.
 *
 * args[0] = path to ddi-schema.json (source of truth)
 * args[1] = path to the OpenAPI document to write
 */
public class SchemaToOpenApi {

    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);

        String text = Files.readString(input, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '﻿') {
            text = text.substring(1); // strip UTF-8 BOM
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = (ObjectNode) mapper.readTree(text);

        ObjectNode openapi = mapper.createObjectNode();
        openapi.put("openapi", "3.1.0");
        ObjectNode info = openapi.putObject("info");
        info.put("title", "DDI 4");
        info.put("version", "1.0.0");
        openapi.putObject("paths");
        ObjectNode schemas = openapi.putObject("components").putObject("schemas");

        // Only the "$defs" types become classes. The schema root (topLevelReferences + a
        // polymorphic "anyOf" over every type) is intentionally skipped: it would produce a giant
        // union class and collide with the existing DDIInstance definition.
        JsonNode defs = schema.get("$defs");
        for (Iterator<Map.Entry<String, JsonNode>> it = defs.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> def = it.next();
            schemas.set(def.getKey(), def.getValue());
        }

        // Map every polymorphic union (anyOf / oneOf) to a free-form object. openapi-generator
        // would otherwise synthesize unusable composite types (e.g. an "AnyOf<...>" class with a
        // huge name that is never emitted -> "cannot find symbol"). A union is not cleanly
        // representable as a Java POJO field anyway, so Object is the pragmatic mapping.
        stripUnions(openapi);

        // Doter chaque schema objet d'une Map additionalProperties cote Java
        // (@JsonAnySetter/@JsonAnyGetter). Dans la config openapi-generator du module, c'est
        // "additionalProperties": false (et non true) qui declenche la generation de cette Map
        // (cf. langString/cogsDate/reference). On l'impose donc partout, pour pouvoir porter sur
        // les POJOs generes les attributs DDI3 absents du schema (@isUniversallyUnique,
        // @versionDate) sans changer le contrat JSON du front. Voir plan-migration, etape 3.
        forceAdditionalPropertiesMap(openapi);

        String json = mapper.writeValueAsString(openapi).replace("#/$defs/", "#/components/schemas/");
        Files.createDirectories(output.getParent());
        Files.writeString(output, json, StandardCharsets.UTF_8);
        System.out.println("ddi-schema-to-openapi: wrote " + schemas.size() + " schemas to " + output);
    }

    private static void forceAdditionalPropertiesMap(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            if (obj.has("properties") && obj.path("type").asText("object").equals("object")
                    && !obj.has("additionalProperties")) {
                obj.put("additionalProperties", false);
            }
            for (JsonNode child : obj) {
                forceAdditionalPropertiesMap(child);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                forceAdditionalPropertiesMap(child);
            }
        }
    }

    private static void stripUnions(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            obj.remove("anyOf");
            obj.remove("oneOf");
            for (JsonNode child : obj) {
                stripUnions(child);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                stripUnions(child);
            }
        }
    }
}
