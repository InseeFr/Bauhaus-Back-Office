package fr.insee.rmes.model.operations;

public record PartialOperationIndicator(String id, String label, String altLabel) {
    public static PartialOperationIndicator appendLabel(PartialOperationIndicator p1, PartialOperationIndicator p2){
        return new PartialOperationIndicator(
                p1.id(),
                p1.label(),
                p1.altLabel() + " || " + p2.altLabel()
        );
    }
}
