package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * Une StudyUnit miroir d'une opération, avec le Group (miroir de la série de l'opération) sous
 * lequel elle est classée : de quoi y rattacher un nouveau fichier de données.
 */
public record OperationStudyUnit(
        String studyUnitAgency,
        String studyUnitId,
        String studyUnitLabel,
        String groupAgency,
        String groupId,
        String groupLabel) {}
