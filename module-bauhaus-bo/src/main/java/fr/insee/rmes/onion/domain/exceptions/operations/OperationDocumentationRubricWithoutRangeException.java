package fr.insee.rmes.onion.domain.exceptions.operations;

public class OperationDocumentationRubricWithoutRangeException extends Exception {
    private final String id;

    public OperationDocumentationRubricWithoutRangeException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
