package fr.insee.rmes.onion.domain.port.clientside.operations;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.OperationFamily;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;

import java.util.List;

public interface FamilyService {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;

}
