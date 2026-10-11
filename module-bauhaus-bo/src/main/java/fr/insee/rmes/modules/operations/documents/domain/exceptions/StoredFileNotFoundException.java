package fr.insee.rmes.modules.operations.documents.domain.exceptions;

public class StoredFileNotFoundException extends Exception {

    public StoredFileNotFoundException(String fileName) {
        super(fileName + " not found");
    }
}
