package fr.insee.rmes.modules.operations.families.webservice.response;

import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeriesWithReport;

public record OperationFamilySeriesWithReportResponse(
        String id,
        String labelLg1,
        String labelLg2,
        String idSims) {

    public static OperationFamilySeriesWithReportResponse fromDomain(OperationFamilySeriesWithReport series) {
        return new OperationFamilySeriesWithReportResponse(
                series.id(), series.labelLg1(), series.labelLg2(), series.idSims());
    }
}
