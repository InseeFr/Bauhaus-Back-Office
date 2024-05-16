package fr.insee.rmes.model.dataset;


public class PatchDataset {
    private String updated;

    private String issued;

    private Integer numObservations;

    private Integer numSeries;

    private Temporal temporal;



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

    public Integer getNumObservations() {
        return numObservations;
    }

    public void setNumObservations(Integer numObservations) {
        this.numObservations = numObservations;
    }

    public Integer getNumSeries() {
        return numSeries;
    }

    public void setNumSeries(Integer numSeries) {
        this.numSeries = numSeries;
    }

    public Temporal getTemporal() {
        return temporal;
    }

    public void setTemporal(Temporal temporal) {
        this.temporal = temporal;
    }
}
