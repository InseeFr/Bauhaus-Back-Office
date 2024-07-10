package fr.insee.rmes.model.dataset;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;

public record PatchDataset(String updated, String issued, Integer numObservations, Integer numSeries,
                           Temporal temporal) {

    public PatchDataset {
        if (numObservations != null && numObservations <= 0) {
            throw new RmesRuntimeBadRequestException("observationNumber must be greater than zero");
        }
        if (updated == null &&
                issued == null &&
                numObservations == null &&
                numSeries == null &&
                temporal == null) {
            throw new RmesRuntimeBadRequestException("One of these attributes is required : updated, issued, numObservations, numSeries, temporal");
        }
    }
}


