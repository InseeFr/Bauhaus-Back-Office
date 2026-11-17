package fr.insee.rmes.modules.operations.documents.domain.model;

public record DocumentDescription(DocumentMetadata metadata, DocumentType type) {

    /** Taille du fichier ; 0 quand aucune n'est stockée (lien, document pas encore repris). */
    public FileSize size() {
        FileSize size = metadata.size();
        return size == null ? FileSize.ZERO : size;
    }
}
