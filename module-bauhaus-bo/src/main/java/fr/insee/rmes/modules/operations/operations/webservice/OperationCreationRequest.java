package fr.insee.rmes.modules.operations.operations.webservice;

import fr.insee.rmes.modules.operations.operations.domain.model.commands.OperationCommand;
import fr.insee.rmes.modules.operations.operations.webservice.OperationRequest.SeriesRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de requête de la création d'une opération : celui de {@link OperationRequest}, avec la
 * série obligatoire, puisque c'est à la création, et seulement à elle, que l'opération y est
 * rattachée. Sans série, le dépôt échouait sur un {@code NullPointerException}.
 * <p>
 * {@code created} n'y figure pas : le dépôt horodate la création.
 */
public record OperationCreationRequest(
        @NotBlank(message = "prefLabelLg1 is required") String prefLabelLg1,
        String prefLabelLg2,
        String altLabelLg1,
        String altLabelLg2,
        @NotNull(message = "series is required") @Valid SeriesRequest series,
        Integer year,
        String idSims) {

    OperationCommand toCommand() {
        return new OperationCommand(
                prefLabelLg1, prefLabelLg2, altLabelLg1, altLabelLg2, series.id(), year, idSims, null);
    }
}
