package fr.insee.rmes.model.dataset;

public record DatasetsForSearch(
        String labelLg1,
        String creator,
        String disseminationStatus,
        String validationStatus,
        String wasGeneratedIRIs,
        String created,
        String updated
){}