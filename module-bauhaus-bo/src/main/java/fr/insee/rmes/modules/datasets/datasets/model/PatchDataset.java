package fr.insee.rmes.modules.datasets.datasets.model;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import jakarta.validation.constraints.Positive;

public record PatchDataset(String updated, String issued, @Positive Integer numObservations, Integer numSeries,
                           Temporal temporal) {

    public PatchDataset {
        if (updated == null &&
                issued == null &&
                numObservations == null &&
                numSeries == null &&
                temporal == null) {
            throw new RmesRuntimeBadRequestException("One of these attributes is required : updated, issued, numObservations, numSeries, temporal");
        }
    }
}
