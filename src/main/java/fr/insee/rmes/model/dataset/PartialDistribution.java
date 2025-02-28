package fr.insee.rmes.model.dataset;
public record PartialDistribution(
        String id,
        String idDataset,
        String labelLg1,
        String labelLg2,
        String descriptionLg1,
        String descriptionLg2,
        String created,
        String updated,
        String format,
        String byteSize,
        String url
) {
}
