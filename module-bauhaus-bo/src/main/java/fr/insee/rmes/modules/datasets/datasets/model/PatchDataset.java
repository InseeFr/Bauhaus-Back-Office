package fr.insee.rmes.modules.datasets.datasets.model;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import jakarta.validation.constraints.Positive;

public record PatchDataset(
        String updated,
        String issued,

        @Positive(message = "La valeur doit être strictement positive.")
        Integer numObservations,

        Integer numSeries,
        Temporal temporal) {

    public PatchDataset {
        if (updated == null && issued == null && numObservations == null && numSeries == null && temporal == null) {
            throw new RmesRuntimeBadRequestException(
                    "Renseignez au moins un de ces champs : updated, issued, numObservations, numSeries, temporal.");
        }
    }
}
