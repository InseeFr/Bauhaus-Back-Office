package fr.insee.rmes.modules.concepts.concept.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

/**
 * Corps des requêtes de création et de mise à jour d'un concept.
 * <p>
 * Le dépôt réécrit le concept entier à partir de ce corps : tout champ que ce record ne déclare
 * pas serait effacé en base. Il reprend donc chaque champ du modèle legacy
 * {@link fr.insee.rmes.model.concepts.Concept}, sauf ceux que le serveur fixe lui-même
 * ({@code id}, {@code modified}, {@code creation}). {@code created} est relu du corps : sans lui,
 * le dépôt doit relire la date de création en base.
 * <p>
 * Seuls le libellé principal et le statut de diffusion sont obligatoires : ce sont les deux
 * champs que le dépôt écrit sans garde.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptRequest(
        @NotBlank(message = REQUIRED) String prefLabelLg1,
        String prefLabelLg2,
        List<String> altLabelLg1,
        List<String> altLabelLg2,
        String creator,
        String contributor,
        @NotBlank(message = REQUIRED) String disseminationStatus,
        String additionalMaterial,
        String valid,
        String created,
        Boolean versioning,
        List<String> collections,
        List<@Valid Note> versionableNotes,
        List<@Valid Note> datableNotes,
        List<@Valid Link> links) {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Note(String noteType, String content) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Link(@NotBlank(message = REQUIRED) String typeOfLink, List<String> ids, List<String> urn) {}

    /** Le corps attendu par le service legacy, qui le désérialise lui-même. */
    String toLegacyJson() {
        return MAPPER.writeValueAsString(this);
    }
}
