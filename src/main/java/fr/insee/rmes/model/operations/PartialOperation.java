package fr.insee.rmes.model.operations;

public record PartialOperation(String id, String label, String iri, String seriesIri, String altLabel) {
    public static PartialOperation appendLabel(PartialOperation p1, PartialOperation p2){
        return new PartialOperation(
                p1.id(),
                p1.label(),
                p1.iri(),
                p1.seriesIri,
                p1.altLabel() + " || " + p2.altLabel()
        );
    }
}
