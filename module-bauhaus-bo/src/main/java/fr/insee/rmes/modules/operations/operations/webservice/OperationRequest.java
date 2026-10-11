package fr.insee.rmes.modules.operations.operations.webservice;

import fr.insee.rmes.modules.operations.operations.domain.model.commands.OperationCommand;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de requête de la mise à jour d'une opération.
 * <p>
 * Le dépôt réécrit l'opération entière à partir de ce corps : tout champ absent d'ici serait effacé
 * de l'opération existante. Ce record doit donc rester un miroir de ce que le dépôt écrit. En sont
 * exclus {@code id} (pris dans le chemin), {@code modified} (horodaté par le dépôt), et ce que le
 * front renvoie de sa lecture sans que le dépôt l'écrive ({@code validationState}…), ignoré.
 * <p>
 * {@code prefLabelLg1} est annoté ici pour que le client reçoive un 400 nommant le champ fautif ;
 * le même invariant est porté par {@link OperationCommand}, qui protège le domaine des appels ne
 * passant pas par cette ressource.
 */
public record OperationRequest(
        @NotBlank(message = "prefLabelLg1 is required") String prefLabelLg1,
        String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        SeriesRequest series,
        Integer year,
        String idSims,
        String created) {

    OperationCommand toCommand() {
        return new OperationCommand(
                prefLabelLg1,
                prefLabelLg2,
                altLabelLg1,
                altLabelLg2,
                series == null ? null : series.id(),
                year,
                idSims,
                created);
    }

    /** La série, dont seul l'identifiant compte : le front renvoie aussi ses libellés. */
    public record SeriesRequest(
            @NotBlank(message = "series.id is required") String id) {}
}
