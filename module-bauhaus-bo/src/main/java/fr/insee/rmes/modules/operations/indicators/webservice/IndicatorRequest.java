package fr.insee.rmes.modules.operations.indicators.webservice;

import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorLink;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.IndicatorCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Corps de requête d'un indicateur, en création comme en mise à jour.
 * <p>
 * Le dépôt réécrit l'indicateur entier à partir de ce corps : tout champ absent d'ici serait effacé
 * de l'indicateur existant. Ce record doit donc rester un miroir de
 * {@link fr.insee.rmes.model.operations.Indicator}, à deux exceptions près — {@code id}, qui vient
 * du chemin en mise à jour et est généré en création, et {@code updated}, que le dépôt horodate
 * lui-même.
 * <p>
 * {@code prefLabelLg1} est annoté ici pour que le client reçoive un 400 nommant le champ fautif ;
 * le même invariant est porté par {@link IndicatorCommand}, qui protège le domaine des appels ne
 * passant pas par cette ressource. Les règles qui demandent d'interroger le triplestore — série
 * liée, unicité des libellés, organisations connues — restent au dépôt.
 */
public record IndicatorRequest(
        @NotBlank(message = "prefLabelLg1 is required") String prefLabelLg1,
        String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String abstractLg1,
        String abstractLg2,
        String historyNoteLg1,
        String historyNoteLg2,
        String accrualPeriodicityCode,
        String accrualPeriodicityList,
        List<String> publishers,
        List<String> contributors,
        List<String> creators,
        List<LinkRequest> seeAlso,
        List<LinkRequest> replaces,
        List<LinkRequest> isReplacedBy,
        List<LinkRequest> wasGeneratedBy,
        String idSims,
        String created,
        String validationState) {

    IndicatorCommand toCommand() {
        return new IndicatorCommand(
                prefLabelLg1,
                prefLabelLg2,
                altLabelLg1,
                altLabelLg2,
                abstractLg1,
                abstractLg2,
                historyNoteLg1,
                historyNoteLg2,
                accrualPeriodicityCode,
                accrualPeriodicityList,
                publishers,
                contributors,
                creators,
                toDomain(seeAlso),
                toDomain(replaces),
                toDomain(isReplacedBy),
                toDomain(wasGeneratedBy),
                idSims,
                created,
                validationState);
    }

    private static List<IndicatorLink> toDomain(List<LinkRequest> links) {
        return links == null ? null : links.stream().map(LinkRequest::toDomain).toList();
    }

    /** Un lien tel que le front l'envoie : il renvoie les libellés reçus en lecture, on les ignore. */
    public record LinkRequest(String id, String type) {
        IndicatorLink toDomain() {
            return new IndicatorLink(id, type);
        }
    }
}
