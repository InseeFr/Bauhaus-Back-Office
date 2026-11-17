package fr.insee.rmes.modules.operations.documents.domain.exceptions;

import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;

public class DocumentNotFoundException extends Exception {

    public DocumentNotFoundException(DocumentKind kind, String id) {
        super((kind == DocumentKind.LINK ? "Link " : "Document ") + id + " doesn't exist");
    }
}
