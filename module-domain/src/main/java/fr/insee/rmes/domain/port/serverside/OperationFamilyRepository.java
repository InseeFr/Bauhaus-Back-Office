package fr.insee.rmes.domain.port.serverside;


import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.OperationFamilySeries;
import fr.insee.rmes.domain.model.operations.families.OperationFamilySubject;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;

import java.util.List;

public interface OperationFamilyRepository {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFullFamily(String id) throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;
    List<OperationFamilySeries> getFamilySeries(String id) throws RmesException;
    List<OperationFamilySubject> getFamilySubjects(String id) throws RmesException;
}