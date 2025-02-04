package fr.insee.rmes.model.concepts;

public record PartialConcept(String id, String label, String altLabel){
    public static PartialConcept appendLabel(PartialConcept p1, PartialConcept p2){
        return new PartialConcept(
                p1.id(),
                p1.label(),
                p1.altLabel() + " || " + p2.altLabel()
        );
    }
}