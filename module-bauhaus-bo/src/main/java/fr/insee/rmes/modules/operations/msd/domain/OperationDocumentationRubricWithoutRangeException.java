package fr.insee.rmes.modules.operations.msd.domain;

public class OperationDocumentationRubricWithoutRangeException extends Exception {
    private final String id;

    public OperationDocumentationRubricWithoutRangeException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
