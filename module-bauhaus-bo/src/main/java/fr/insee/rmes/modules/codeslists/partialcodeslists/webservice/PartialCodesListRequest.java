package fr.insee.rmes.modules.codeslists.partialcodeslists.webservice;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Corps des requêtes de création et de mise à jour d'une liste de codes partielle.
 * <p>
 * Le service réécrit la liste entière à partir de ce corps : tout champ que ce record ne déclare
 * pas serait effacé en base. Il reprend donc chaque champ qu'écrit
 * {@code CodeListServiceImpl.createOrUpdateCodeList}, {@code validationState} compris (il décide
 * entre « Modifiée » et « Provisoire »). Le reste de ce que renvoie le front (code parent, date de
 * création, état des codes dans le picker...) est ignoré.
 * <p>
 * Les champs obligatoires sont ceux du formulaire, plus l'IRI de la liste parente, dont le code
 * parent du formulaire est tiré. La règle « au moins un code » reste au service, qui la signale par
 * un code d'erreur dédié.
 *
 * @param contributor le front envoie le contributeur par défaut sous forme de chaîne à la création.
 * @param codes les membres de la liste, indexés par code : seule leur IRI est écrite.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartialCodesListRequest(
        @NotBlank(message = REQUIRED) String id,
        @NotBlank(message = REQUIRED) String labelLg1,
        @NotBlank(message = REQUIRED) String labelLg2,
        String descriptionLg1,
        String descriptionLg2,
        @NotBlank(message = REQUIRED) String creator,

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<String> contributor,

        @NotBlank(message = REQUIRED) String disseminationStatus,

        String validationState,
        @NotBlank(message = REQUIRED) String iriParent,
        Map<String, @Valid PartialCodeRequest> codes) {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    /** Le corps attendu par le service legacy, qui le désérialise lui-même. */
    String toLegacyJson() {
        return MAPPER.writeValueAsString(this);
    }

    public record PartialCodeRequest(
            @NotBlank(message = REQUIRED) String iri) {}
}
