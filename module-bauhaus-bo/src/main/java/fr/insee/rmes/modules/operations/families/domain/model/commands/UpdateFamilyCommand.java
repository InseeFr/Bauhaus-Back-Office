package fr.insee.rmes.modules.operations.families.domain.model.commands;

/**
 * Mise à jour d'une famille d'opérations.
 * <p>
 * {@code created} vient du corps de la requête : l'écriture RDF remplace tous les triplets de la
 * famille, la date de création doit donc être renvoyée par le client pour ne pas être perdue.
 */
public record UpdateFamilyCommand(
        String id, String prefLabelLg1, String prefLabelLg2, String abstractLg1, String abstractLg2, String created) {}
