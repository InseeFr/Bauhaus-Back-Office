package fr.insee.rmes.modules.operations.operations.domain.model.commands;

import fr.insee.rmes.modules.operations.operations.domain.exceptions.InvalidOperationCommandException;
import org.apache.commons.lang3.StringUtils;

/**
 * Contenu d'une opération tel que le client le soumet, en création comme en mise à jour.
 * L'identifiant n'en fait pas partie : il est généré à la création, pris dans le chemin à la mise
 * à jour.
 * <p>
 * Le dépôt réécrit l'opération entière à partir de cette commande : un champ absent d'ici serait
 * effacé, pas laissé inchangé. C'est pourquoi {@code created} en fait partie. {@code seriesId}
 * rattache l'opération à sa série à la création ; en mise à jour, il n'accompagne que l'événement
 * {@code OperationSaved}.
 * <p>
 * Seul {@code prefLabelLg1} est un invariant : c'est le seul libellé que le dépôt écrit sans garde.
 * Les règles qui demandent d'interroger le triplestore — série connue, unicité des libellés —
 * restent au dépôt, qui seul peut les évaluer.
 */
public record OperationCommand(
        String prefLabelLg1,
        String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        String seriesId,
        Integer year,
        String idSims,
        String created) {

    public OperationCommand {
        if (StringUtils.isBlank(prefLabelLg1)) {
            throw new InvalidOperationCommandException("The prefLabelLg1 is blank");
        }
    }
}
