package fr.insee.rmes.modules.operations.documents.domain.model;

import org.jspecify.annotations.Nullable;

/**
 * Ce que l'utilisateur saisit pour un document ou un lien. L'URL d'un lien vient de lui ; celle d'un
 * document est posée par le stockage au dépôt du fichier.
 *
 * @param updatedDate date de mise à jour telle que saisie, au format accepté par le stockage RDF
 */
public record DocumentForm(
        @Nullable String labelLg1,
        @Nullable String labelLg2,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        @Nullable String updatedDate,
        @Nullable String lang,
        @Nullable String url) {

    public DocumentForm withUrl(String newUrl) {
        return new DocumentForm(labelLg1, labelLg2, descriptionLg1, descriptionLg2, updatedDate, lang, newUrl);
    }
}
