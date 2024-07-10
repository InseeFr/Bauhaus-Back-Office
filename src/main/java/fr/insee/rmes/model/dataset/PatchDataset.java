package fr.insee.rmes.model.dataset;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;

public record PatchDataset(String updated, String issued, Integer observationNumber, Integer timeSeriesNumber,
                           String temporalCoverageStartDate, String temporalCoverageEndDate) {

    public PatchDataset {
        if (observationNumber != null && observationNumber <= 0) {
            throw new RmesRuntimeBadRequestException("observationNumber must be greater than zero");
        }
        if (updated == null &&
                issued == null &&
                observationNumber == null &&
                timeSeriesNumber == null &&
                temporalCoverageStartDate == null &&
                temporalCoverageEndDate == null) {
            throw new RmesRuntimeBadRequestException("At least one field of a patch dataset must be non null");
        }
    }
}


