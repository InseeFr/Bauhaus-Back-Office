package fr.insee.rmes.modules.datasets.datasets.model;

public record DatasetsForSearch(
        String id,
        String labelLg1,
        String creator,
        String disseminationStatus,
        String validationStatus,
        String wasGeneratedIRIs,
        String created,
        String updated
){}