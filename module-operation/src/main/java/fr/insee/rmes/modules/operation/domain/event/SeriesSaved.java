package fr.insee.rmes.modules.operation.domain.event;

/**
 * Publié après qu'une série d'opérations statistiques a été écrite dans le dépôt RDF, qu'elle vienne
 * d'être créée ou modifiée.
 * <p>
 * L'événement ne décrit que la série telle que le domaine « opérations » la connaît ; il ignore
 * délibérément ce que ses consommateurs en feront. C'est ce qui permet de tenir un miroir DDI des
 * séries sans que le domaine « opérations » dépende du module DDI.
 *
 * @param iri l'IRI de publication de la série, son identifiant canonique hors de Bauhaus
 * @param prefLabel le libellé long ({@code skos:prefLabel})
 * @param altLabel le libellé court ({@code skos:altLabel}), éventuellement vide
 */
public record SeriesSaved(String iri, String id, BilingualLabel prefLabel, BilingualLabel altLabel) {}
