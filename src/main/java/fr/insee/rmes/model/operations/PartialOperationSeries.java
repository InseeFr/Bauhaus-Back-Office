package fr.insee.rmes.model.operations;

import fr.insee.rmes.model.concepts.PartialConcept;

public record PartialOperationSeries(String id, String iri, String label, String altLabel) {
    public static PartialOperationSeries appendLabel(PartialOperationSeries p1, PartialOperationSeries p2){
        return new PartialOperationSeries(
                p1.id(),
                p1.iri(),
                p1.label(),
                p1.altLabel() + " || " + p2.altLabel()
        );
    }
}
