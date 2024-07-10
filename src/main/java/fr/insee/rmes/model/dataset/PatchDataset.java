package fr.insee.rmes.model.dataset;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;

public record PatchDataset(String updated, String issued, Integer numObservations, Integer numSeries,
                           Temporal temporal) {

    public PatchDataset {
        if (observationNumber != null && observationNumber <= 0) {
            throw new RmesRuntimeBadRequestException("observationNumber must be greater than zero");
        }
        if (updated == null &&
                issued == null &&
                observationNumber == null &&
                timeSeriesNumber == null &&
                temporal == null) {
            throw new RmesRuntimeBadRequestException(DATASET_PATCH_INCORRECT_BODY,"One of these attributes is required : updated, issued, numObservations, numSeries, temporal");
        }
    }
}


