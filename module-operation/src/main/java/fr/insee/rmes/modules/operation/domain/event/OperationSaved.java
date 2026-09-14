package fr.insee.rmes.modules.operation.domain.event;

/**
 * Publié après qu'une opération statistique a été écrite dans le dépôt RDF, qu'elle vienne d'être
 * créée ou modifiée. Pendant de {@link SeriesSaved} pour les opérations.
 *
 * @param iri l'IRI de publication de l'opération, son identifiant canonique hors de Bauhaus
 * @param seriesIri l'IRI de publication de la série dont l'opération relève
 * @param prefLabel le libellé long ({@code skos:prefLabel})
 * @param altLabel le libellé court ({@code skos:altLabel}), éventuellement vide
 */
public record OperationSaved(
        String iri, String id, String seriesIri, BilingualLabel prefLabel, BilingualLabel altLabel) {}
