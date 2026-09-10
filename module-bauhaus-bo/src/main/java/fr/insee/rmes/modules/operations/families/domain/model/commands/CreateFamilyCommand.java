package fr.insee.rmes.modules.operations.families.domain.model.commands;

/**
 * Création d'une famille d'opérations. L'identifiant n'en fait pas partie : il est généré par le
 * dépôt, comme pour les séries et les opérations.
 */
public record CreateFamilyCommand(String prefLabelLg1, String prefLabelLg2, String abstractLg1, String abstractLg2) {}
