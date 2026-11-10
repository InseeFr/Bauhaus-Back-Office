package fr.insee.rmes.modules.classifications.nomenclatures.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

/**
 * Corps de la requête de modification d'un poste de nomenclature.
 * <p>
 * Le service réécrit le poste à partir de ce corps : tout champ que ce record ne déclare pas serait
 * effacé en base. Il reprend donc chaque champ qu'écrit
 * {@code ClassificationItemRepository.updateClassificationItem}. Le reste de ce que renvoie le front
 * est ignoré : l'identifiant (il vient du chemin), le poste parent par son identifiant (seul
 * {@code broaderURI} est écrit), les postes enfants et le statut de publication, que le service
 * recalcule à partir de l'état en base.
 *
 * @param altLabels les libellés courts du poste, un par longueur : chacun est réécrit sous son IRI.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClassificationItemRequest(
        @NotBlank(message = REQUIRED) String prefLabelLg1,
        @NotBlank(message = REQUIRED) String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String broaderURI,
        List<@Valid ShortLabelRequest> altLabels,
        String definitionLg1Uri,
        String definitionLg1,
        String definitionLg2Uri,
        String definitionLg2,
        String scopeNoteLg1Uri,
        String scopeNoteLg1,
        String scopeNoteLg2Uri,
        String scopeNoteLg2,
        String coreContentNoteLg1Uri,
        String coreContentNoteLg1,
        String coreContentNoteLg2Uri,
        String coreContentNoteLg2,
        String additionalContentNoteLg1Uri,
        String additionalContentNoteLg1,
        String additionalContentNoteLg2Uri,
        String additionalContentNoteLg2,
        String exclusionNoteLg1Uri,
        String exclusionNoteLg1,
        String exclusionNoteLg2Uri,
        String exclusionNoteLg2,
        String changeNoteLg1Uri,
        String changeNoteLg1,
        String changeNoteLg2Uri,
        String changeNoteLg2) {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    /** Le corps attendu par le service legacy, qui le désérialise lui-même. */
    String toLegacyJson() {
        return MAPPER.writeValueAsString(this);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ShortLabelRequest(
            String length,
            String shortLabelLg1,
            String shortLabelLg2,
            @NotBlank(message = REQUIRED) String shortLabelUri) {}
}
