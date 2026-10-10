package fr.insee.rmes.modules.operations.indicators.domain.model;

/**
 * Lien d'un indicateur vers un autre objet des opérations (série, indicateur remplacé…).
 * <p>
 * Seuls l'identifiant et le type portent de l'information en écriture : c'est d'eux que le dépôt
 * reconstruit l'IRI. Les libellés que le client renvoie sont ignorés.
 */
public record IndicatorLink(String id, String type) {}
