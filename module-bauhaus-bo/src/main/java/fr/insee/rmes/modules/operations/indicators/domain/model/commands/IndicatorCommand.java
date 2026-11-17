package fr.insee.rmes.modules.operations.indicators.domain.model.commands;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorLink;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Contenu d'un indicateur tel que le client le soumet, en création comme en mise à jour : les deux
 * opérations portent exactement la même charge utile, seul l'identifiant les distingue (généré à la
 * création, pris dans le chemin à la mise à jour) — il n'apparaît donc pas ici.
 * <p>
 * Le dépôt réécrit l'indicateur entier à partir de cette commande : un champ absent d'ici serait
 * effacé, pas laissé inchangé. C'est pourquoi {@code created} en fait partie.
 * <p>
 * Seul {@code prefLabelLg1} est un invariant : c'est le seul libellé que le dépôt écrit sans garde.
 * Les règles qui demandent d'interroger le triplestore — série liée, unicité des libellés,
 * organisations connues — restent au dépôt, qui seul peut les évaluer.
 */
public record IndicatorCommand(
        String prefLabelLg1,
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
        List<IndicatorLink> seeAlso,
        List<IndicatorLink> replaces,
        List<IndicatorLink> isReplacedBy,
        List<IndicatorLink> wasGeneratedBy,
        String idSims,
        String created,
        String validationState) {

    public IndicatorCommand {
        if (StringUtils.isBlank(prefLabelLg1)) {
            throw new InvalidIndicatorCommandException("The prefLabelLg1 is blank");
        }
    }
}
