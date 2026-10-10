package fr.insee.rmes.modules.operations.documents.domain.exceptions;

public class PublishedFileNotFoundException extends Exception {

    public PublishedFileNotFoundException(String fileName) {
        super("File " + fileName + " is not published");
    }
}
