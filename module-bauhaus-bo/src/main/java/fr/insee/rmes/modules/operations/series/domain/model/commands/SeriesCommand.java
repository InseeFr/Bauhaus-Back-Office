package fr.insee.rmes.modules.operations.series.domain.model.commands;

import fr.insee.rmes.modules.operations.series.domain.exceptions.InvalidSeriesCommandException;
import fr.insee.rmes.modules.operations.series.domain.model.SeriesLink;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Contenu d'une série tel que le client le soumet, en création comme en mise à jour. L'identifiant
 * n'en fait pas partie : il est généré à la création, pris dans le chemin à la mise à jour.
 * <p>
 * Le dépôt réécrit la série entière à partir de cette commande : un champ absent d'ici serait
 * effacé, pas laissé inchangé. C'est pourquoi {@code created} en fait partie. {@code familyId} ne
 * sert qu'à la création, seul moment où la série est rattachée à sa famille.
 * <p>
 * Seul {@code prefLabelLg1} est un invariant : c'est le seul libellé que le dépôt écrit sans garde.
 * Les règles qui demandent d'interroger le triplestore — famille connue, unicité des libellés,
 * organisations connues — restent au dépôt, qui seul peut les évaluer.
 */
public record SeriesCommand(
        String prefLabelLg1,
        String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String abstractLg1,
        String abstractLg2,
        String historyNoteLg1,
        String historyNoteLg2,
        String familyId,
        String typeCode,
        String typeList,
        String accrualPeriodicityCode,
        String accrualPeriodicityList,
        List<SeriesLink> publishers,
        List<SeriesLink> contributors,
        List<SeriesLink> dataCollectors,
        List<String> creators,
        List<SeriesLink> seeAlso,
        List<SeriesLink> replaces,
        List<SeriesLink> isReplacedBy,
        List<String> themes,
        String idSims,
        String created) {

    public SeriesCommand {
        if (StringUtils.isBlank(prefLabelLg1)) {
            throw new InvalidSeriesCommandException("The prefLabelLg1 is blank");
        }
    }
}
