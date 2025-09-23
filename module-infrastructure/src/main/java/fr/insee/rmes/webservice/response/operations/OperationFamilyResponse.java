package fr.insee.rmes.webservice.response.operations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.webservice.response.BaseResponse;

import java.util.List;

@JsonIgnoreProperties(allowGetters = true)
public class OperationFamilyResponse extends BaseResponse<OperationFamilyResponse, OperationFamily> {

    private final List<OperationFamilySeriesResponse> series;
    private final List<OperationFamilySubjectResponse> subjects;

    private OperationFamilyResponse(OperationFamily domainObject, List<OperationFamilySeriesResponse> series, List<OperationFamilySubjectResponse> subjects) {
        super(domainObject);
        this.series = series;
        this.subjects = subjects;
    }
    
    public static OperationFamilyResponse fromDomain(
            OperationFamily family,
            List<OperationFamilySeriesResponse> series,
            List<OperationFamilySubjectResponse> subjects) {
        return new OperationFamilyResponse(family, series
                , subjects);
    }

    public List<OperationFamilySeriesResponse> getSeries(){
        return series;
    }

    public List<OperationFamilySubjectResponse> getSubjects() {
        return subjects;
    }
}