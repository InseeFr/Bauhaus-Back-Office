package fr.insee.rmes.modules.operations.families.domain.model;

/**
 * Série d'une famille qui porte déjà un rapport de métadonnées (SIMS). Le front s'en sert pour
 * proposer un SIMS existant comme modèle à la création d'un nouveau.
 */
public record OperationFamilySeriesWithReport(String id, String labelLg1, String labelLg2, String idSims) {}
