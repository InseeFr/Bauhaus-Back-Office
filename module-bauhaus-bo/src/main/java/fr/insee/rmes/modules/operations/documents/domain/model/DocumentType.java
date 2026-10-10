package fr.insee.rmes.modules.operations.documents.domain.model;

import java.util.Set;

/**
 * Type d'un document ou d'un lien, déduit des rubriques de rapport qualité qui le citent.
 * <p>
 * Seul le nom local du concept de la rubrique compte : l'IRI de {@code COLLECTION_DOCUMENTS} change
 * de base d'un environnement à l'autre, celle de {@code DOC_METHOD} (Eurostat) non.
 */
public enum DocumentType {
    COLLECTION_DOCUMENTS,
    DOC_METHOD,
    OTHER;

    public static DocumentType fromRubricConcepts(Set<String> conceptIris) {
        if (conceptIris.size() != 1) {
            return OTHER;
        }
        String concept = conceptIris.iterator().next();
        if (concept.endsWith("/" + DOC_METHOD.name())) {
            return DOC_METHOD;
        }
        if (concept.endsWith("/" + COLLECTION_DOCUMENTS.name())) {
            return COLLECTION_DOCUMENTS;
        }
        return OTHER;
    }
}
