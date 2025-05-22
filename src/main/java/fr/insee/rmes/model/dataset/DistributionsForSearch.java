package fr.insee.rmes.model.dataset;

public record DistributionsForSearch(
        String distributionId,
        String distributionLabelLg1,
        String distributionValidationStatus,
        String distributionCreated,
        String distributionUpdated,
        String altIdentifier,
        String id,
        String labelLg1,
        String creator,
        String disseminationStatus,
        String validationStatus,
        String wasGeneratedIRIs,
        String created,
        String updated
) {}
