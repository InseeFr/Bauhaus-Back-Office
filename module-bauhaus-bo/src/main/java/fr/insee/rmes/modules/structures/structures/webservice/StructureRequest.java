package fr.insee.rmes.modules.structures.structures.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

/**
 * Corps des requêtes de création et de mise à jour d'une structure.
 * <p>
 * Le dépôt réécrit la structure entière, spécifications de composants comprises, à partir de ce
 * corps : tout champ que ce record ne déclare pas serait effacé en base. Il reprend donc chaque
 * champ du modèle legacy {@link fr.insee.rmes.modules.structures.structures.domain.model.Structure}
 * qu'écrit le dépôt, sauf {@code id} (celui du chemin, ou généré en création) et {@code updated}
 * (fixé par le serveur). {@code created} est relu du corps en mise à jour.
 * <p>
 * La notation et les deux libellés sont obligatoires, comme dans le formulaire. Chaque définition
 * porte un composant typé : le dépôt lit son type sans garde.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StructureRequest(
        @NotBlank(message = REQUIRED) String identifiant,
        @NotBlank(message = REQUIRED) String labelLg1,

        @NotBlank(message = REQUIRED) String labelLg2,
        String descriptionLg1,
        String descriptionLg2,
        String created,
        String creator,
        List<String> contributor,
        String disseminationStatus,
        List<@Valid ComponentDefinitionRequest> componentDefinitions) {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public StructureRequest {
        // Le dépôt parcourt les contributeurs et les définitions sans garde.
        contributor = contributor == null ? List.of() : contributor;
        componentDefinitions = componentDefinitions == null ? List.of() : componentDefinitions;
    }

    /** Le corps attendu par le service legacy, qui le désérialise lui-même. */
    String toLegacyJson() {
        return MAPPER.writeValueAsString(this);
    }

    /**
     * Miroir de {@link fr.insee.rmes.modules.structures.structures.domain.model.ComponentDefinition}.
     * {@code id} et {@code modified} sont recalculés par le dépôt, mais relus comme le reste.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ComponentDefinitionRequest(
            String id,
            String created,
            String modified,
            String order,
            List<String> attachment,
            Boolean required,
            String notation,
            String labelLg1,
            String labelLg2,
            @NotNull(message = REQUIRED) @Valid ComponentRequest component) {}

    /**
     * Miroir de {@link fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent}
     * : un composant sans {@code id} est créé par le dépôt à partir de ces champs.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ComponentRequest(
            String id,
            String identifiant,
            String labelLg1,
            String labelLg2,
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
            String pattern) {}
}
