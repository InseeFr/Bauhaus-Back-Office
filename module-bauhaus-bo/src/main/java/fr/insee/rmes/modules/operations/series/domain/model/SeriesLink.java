package fr.insee.rmes.modules.operations.series.domain.model;

/**
 * Lien d'une série vers un autre objet : série, indicateur ou organisation.
 * <p>
 * Seuls l'identifiant et le type portent de l'information en écriture : c'est d'eux que le dépôt
 * reconstruit l'IRI (le type est inutile pour une organisation, résolue par son seul identifiant).
 * Les libellés que le client renvoie sont ignorés.
 */
public record SeriesLink(String id, String type) {}
