package fr.insee.rmes.modules.operations.series.webservice;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.rmes.modules.operations.series.domain.model.SeriesLink;
import fr.insee.rmes.modules.operations.series.domain.model.commands.SeriesCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Corps de requête d'une série, en création comme en mise à jour.
 * <p>
 * Le dépôt réécrit la série entière à partir de ce corps : tout champ absent d'ici serait effacé de
 * la série existante. Ce record doit donc rester un miroir de ce que le dépôt écrit depuis
 * {@link fr.insee.rmes.modules.operations.series.domain.model.Series}. En sont exclus {@code id}
 * (chemin en mise à jour, généré en création), {@code updated} (horodaté par le dépôt), et ce que
 * le front renvoie de sa lecture sans que le dépôt l'écrive ({@code operations}, {@code generate},
 * {@code validationState}…), ignoré.
 * <p>
 * {@code prefLabelLg1} est annoté ici pour que le client reçoive un 400 nommant le champ fautif ;
 * le même invariant est porté par {@link SeriesCommand}, qui protège le domaine des appels ne
 * passant pas par cette ressource.
 */
public record SeriesRequest(
        @NotBlank(message = "prefLabelLg1 is required") String prefLabelLg1,
        String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String abstractLg1,
        String abstractLg2,
        String historyNoteLg1,
        String historyNoteLg2,
        FamilyRequest family,
        String typeCode,
        String typeList,
        String accrualPeriodicityCode,
        String accrualPeriodicityList,
        List<LinkRequest> publishers,
        List<LinkRequest> contributors,
        List<LinkRequest> dataCollectors,
        // Le dépôt acceptait un créateur seul, hors tableau : on garde cette tolérance.
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<String> creators,

        List<LinkRequest> seeAlso,
        List<LinkRequest> replaces,
        List<LinkRequest> isReplacedBy,
        List<String> themes,
        String idSims,
        String created) {

    SeriesCommand toCommand() {
        return new SeriesCommand(
                prefLabelLg1,
                prefLabelLg2,
                altLabelLg1,
                altLabelLg2,
                abstractLg1,
                abstractLg2,
                historyNoteLg1,
                historyNoteLg2,
                family == null ? null : family.id(),
                typeCode,
                typeList,
                accrualPeriodicityCode,
                accrualPeriodicityList,
                toDomain(publishers),
                toDomain(contributors),
                toDomain(dataCollectors),
                creators,
                toDomain(seeAlso),
                toDomain(replaces),
                toDomain(isReplacedBy),
                themes,
                idSims,
                created);
    }

    private static List<SeriesLink> toDomain(List<LinkRequest> links) {
        return links == null ? null : links.stream().map(LinkRequest::toDomain).toList();
    }

    /** La famille, dont seul l'identifiant compte : le front renvoie aussi ses libellés. */
    public record FamilyRequest(String id) {}

    /** Un lien tel que le front l'envoie : il renvoie les libellés reçus en lecture, on les ignore. */
    public record LinkRequest(String id, String type) {
        SeriesLink toDomain() {
            return new SeriesLink(id, type);
        }
    }
}
