package fr.insee.rmes.model.classification;

public record PartialClassificationSeries(String id, String label, String altLabels) {
    public static PartialClassificationSeries appendLabel(PartialClassificationSeries p1, PartialClassificationSeries p2){
        return new PartialClassificationSeries(
                p1.id(),
                p1.label(),
                p1.altLabels() + " || " + p2.altLabels()
        );
    }
}
