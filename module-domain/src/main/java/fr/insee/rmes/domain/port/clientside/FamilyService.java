package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;

import java.util.List;

public interface FamilyService {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;

}