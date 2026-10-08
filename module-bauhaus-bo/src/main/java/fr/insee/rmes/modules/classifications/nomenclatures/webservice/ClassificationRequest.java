package fr.insee.rmes.modules.classifications.nomenclatures.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.INVALID_URL;
import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import tools.jackson.databind.json.JsonMapper;

/**
 * Corps de la requête de modification d'une nomenclature.
 * <p>
 * Le service réécrit la nomenclature à partir de ce corps : tout champ que ce record ne déclare pas
 * serait effacé en base. Il reprend donc chaque champ qu'écrit
 * {@code ClassificationRepository.updateClassification}, {@code validationState} compris (il décide
 * entre « Modifiée » et « Provisoire »). Le reste de ce que renvoie le front (dates, libellés des
 * nomenclatures liées...) est ignoré, l'identifiant compris : il vient du chemin.
 * <p>
 * Les libellés sont obligatoires, comme à l'écran. Les trois liens sont écrits comme des IRI : une
 * valeur qui n'est pas une URL est refusée, la chaîne vide (lien effacé) est acceptée.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClassificationRequest(
        @NotBlank(message = REQUIRED) String prefLabelLg1,
        @NotBlank(message = REQUIRED) String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String descriptionLg1,
        String descriptionLg2,
        String changeNoteLg1,
        String changeNoteLg2,
        String changeNoteUriLg1,
        String changeNoteUriLg2,
        String scopeNoteLg1,
        String scopeNoteLg2,
        String scopeNoteUriLg1,
        String scopeNoteUriLg2,
        String idSeries,
        String idBefore,
        String idAfter,
        String idVariant,
        String disseminationStatus,
        @URL(message = INVALID_URL) String additionalMaterial,
        @URL(message = INVALID_URL) String legalMaterial,
        @URL(message = INVALID_URL) String homepage,
        String creator,
        String contributor,
        String validationState) {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    /** Le corps attendu par le service legacy, qui le désérialise lui-même. */
    String toLegacyJson() {
        return MAPPER.writeValueAsString(this);
    }
}
