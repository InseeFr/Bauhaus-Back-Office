package fr.insee.rmes.modules.operations.documents.domain.model;

import org.jspecify.annotations.Nullable;

/**
 * Un document ou un lien en gestion.
 *
 * @param size taille du fichier d'un document ; absente pour un lien ou un document déposé avant
 *     qu'on la mesure
 */
public record ManagedDocument(
        String id,
        DocumentKind kind,
        String uri,
        DocumentForm form,
        @Nullable FileSize size) {

    public ManagedDocument withForm(DocumentForm newForm) {
        return new ManagedDocument(id, kind, uri, newForm, size);
    }

    public ManagedDocument withFile(String url, FileSize newSize) {
        return new ManagedDocument(id, kind, uri, form.withUrl(url), newSize);
    }
}
