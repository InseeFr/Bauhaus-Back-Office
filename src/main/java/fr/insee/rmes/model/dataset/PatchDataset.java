package fr.insee.rmes.model.dataset;


public class PatchDataset {
    private String updated;

    private String issued;

    private Integer observationNumber;

    private Integer timeSeriesNumber;

    private String temporalCoverageStartDate;

    private String temporalCoverageEndDate;

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public Integer getObservationNumber() {
        return observationNumber;
    }

    public void setObservationNumber(Integer observationNumber) {
        this.observationNumber = observationNumber;
    }

    public Integer getTimeSeriesNumber() {
        return timeSeriesNumber;
    }

    public void setTimeSeriesNumber(Integer timeSeriesNumber) {
        this.timeSeriesNumber = timeSeriesNumber;
    }

    public String getTemporalCoverageStartDate() {
        return temporalCoverageStartDate;
    }

    public void setTemporalCoverageStartDate(String temporalCoverageStartDate) {
        this.temporalCoverageStartDate = temporalCoverageStartDate;
    }

    public String getTemporalCoverageEndDate() {
        return temporalCoverageEndDate;
    }

    public void setTemporalCoverageEndDate(String temporalCoverageEndDate) {
        this.temporalCoverageEndDate = temporalCoverageEndDate;
    }

    public PatchDataset() {
    }
}
