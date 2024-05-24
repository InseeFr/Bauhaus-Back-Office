package fr.insee.rmes.model.dataset;

public class Temporal {

    private String startPeriod;

    private String endPeriod;

    public String getStartPeriod() {
        return startPeriod;
    }

    public String getEndPeriod() {
        return endPeriod;
    }

    public Temporal(String startPeriod, String endPeriod) {
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }
}
