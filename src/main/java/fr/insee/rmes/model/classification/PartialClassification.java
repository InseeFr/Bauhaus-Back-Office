package fr.insee.rmes.model.classification;

public record PartialClassification(String id, String label, String altLabels) {
    public static PartialClassification appendLabel(PartialClassification p1, PartialClassification p2){
        return new PartialClassification(
                p1.id(),
                p1.label(),
                p1.altLabels() + " || " + p2.altLabels()
        );
    }
}
