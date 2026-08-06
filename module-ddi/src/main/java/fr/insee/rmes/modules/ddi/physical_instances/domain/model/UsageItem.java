package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * Un item DDI cité dans une réponse d'usage : son identité ({@code agencyId/id}) et son libellé
 * déjà résolu, pour que le front n'ait aucune requête supplémentaire à faire pour l'afficher.
 *
 * <p>Regroupe les trois champs qui vont toujours ensemble : sans lui, un usage à cinq niveaux se
 * déclare en quinze {@code String} positionnels, où l'interversion de deux champs voisins compile
 * et passe les tests.
 *
 * @param label {@code null} quand Colectica n'en expose aucun pour cet item.
 */
public record UsageItem(String agencyId, String id, String label) {
}
