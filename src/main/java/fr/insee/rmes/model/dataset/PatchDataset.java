package fr.insee.rmes.model.dataset;

import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;

import java.util.List;

public record PatchDataset (String updated, String issued, Integer observationNumber, Integer timeSeriesNumber,
                            String temporalCoverageStartDate, String temporalCoverageEndDate){

    public PatchDataset {
        if (observationNumber!=null && observationNumber<=0){
            throw new RmesRuntimeBadRequestException("observationNumber must be greater than zero");
        }
    }

    public List<String> listNonNullProperties(){
        if (this instanceof PatchDataset(String updated, String issued, Integer observationNumber, Integer timeSeriesNumber,
                String temporalCoverageStartDate, String temporalCoverageEndDate)){

        }
    }
}


