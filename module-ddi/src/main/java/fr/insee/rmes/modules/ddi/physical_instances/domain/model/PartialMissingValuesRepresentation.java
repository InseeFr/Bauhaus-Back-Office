package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

/**
 * Vue partielle d'une {@code ManagedMissingValuesRepresentation} réutilisable (valeurs sentinelles,
 * #1566), servie par {@code GET /ddi/groups/{agencyId}/{id}/missing-values-representations} pour
 * alimenter le sélecteur de réutilisation : identité (pour construire la {@code Reference} posée
 * sur la variable), libellé, et aperçu des codes de la CodeList de sentinelles référencée.
 */
public record PartialMissingValuesRepresentation(
        String id,
        String agency,
        String version,
        String label,
        String codeListId,
        List<String> codeValues
) {
}
