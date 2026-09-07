package fr.insee.rmes.modules.operations.families.webservice.response;

import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySubject;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;

public class OperationFamilySubjectResponse extends BaseResponse<OperationFamilySubjectResponse, OperationFamilySubject> {

    private OperationFamilySubjectResponse(OperationFamilySubject domainObject) {
        super(domainObject);
    }
    
    public static OperationFamilySubjectResponse fromDomain(OperationFamilySubject subject) {
        return new OperationFamilySubjectResponse(subject);
    }
}