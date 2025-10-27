package fr.insee.rmes.webservice.response.operations;

import fr.insee.rmes.domain.model.operations.families.OperationFamilySubject;
import fr.insee.rmes.webservice.response.BaseResponse;

public class OperationFamilySubjectResponse extends BaseResponse<OperationFamilySubjectResponse, OperationFamilySubject> {

    private OperationFamilySubjectResponse(OperationFamilySubject domainObject) {
        super(domainObject);
    }
    
    public static OperationFamilySubjectResponse fromDomain(OperationFamilySubject subject) {
        return new OperationFamilySubjectResponse(subject);
    }
}