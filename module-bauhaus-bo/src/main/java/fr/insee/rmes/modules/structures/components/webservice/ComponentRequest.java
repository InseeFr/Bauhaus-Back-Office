package fr.insee.rmes.modules.structures.components.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Corps des requêtes de création et de mise à jour d'un composant de structure.
 * <p>
 * Le dépôt réécrit le composant entier à partir de ce corps : tout champ que ce record ne déclare
 * pas serait effacé en base. Il reprend donc chaque champ du modèle legacy
 * {@link fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent} qu'écrit le
 * dépôt, sauf {@code updated} que le serveur fixe lui-même. {@code id} reste lu : le dépôt le
 * compare à celui du chemin en mise à jour, et le refuse en création. {@code created} est relu du
 * corps en mise à jour.
 * <p>
 * Les attributs libres arrivent en paires de clés dynamiques {@code attribute_N} /
 * {@code attributeValue_N}, que le dépôt relit dans le JSON brut : elles sont conservées telles
 * quelles dans {@link #attributes()}.
 * <p>
 * La notation, les deux libellés et le type sont obligatoires, comme dans le formulaire.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComponentRequest(
        String id,
        @NotBlank(message = REQUIRED) String identifiant,
        @NotBlank(message = REQUIRED) String labelLg1,
        @NotBlank(message = REQUIRED) String labelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String descriptionLg1,
        String descriptionLg2,
        @NotBlank(message = REQUIRED) String type,
        String concept,
        String codeList,
        String fullCodeListValue,
        String range,
        String created,
        String creator,
        List<String> contributor,
        String disseminationStatus,
        String minLength,
        String maxLength,
        String minInclusive,
        String maxInclusive,
        String pattern,
        @JsonAnySetter Map<String, Object> attributes) {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public ComponentRequest {
        // Le dépôt parcourt les contributeurs sans garde.
        contributor = contributor == null ? List.of() : contributor;
        attributes = attributes == null ? Map.of() : onlyFreeAttributes(attributes);
    }

    @Override
    @JsonAnyGetter
    public Map<String, Object> attributes() {
        return attributes;
    }

    private static Map<String, Object> onlyFreeAttributes(Map<String, Object> unknownFields) {
        Map<String, Object> freeAttributes = new LinkedHashMap<>();
        unknownFields.forEach((key, value) -> {
            if (key.startsWith("attribute_") || key.startsWith("attributeValue_")) {
                freeAttributes.put(key, value);
            }
        });
        return freeAttributes;
    }

    /** Le corps attendu par le service legacy, qui le désérialise lui-même. */
    String toLegacyJson() {
        return MAPPER.writeValueAsString(this);
    }
}
