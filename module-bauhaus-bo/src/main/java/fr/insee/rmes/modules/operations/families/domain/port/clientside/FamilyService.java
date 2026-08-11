package fr.insee.rmes.modules.operations.families.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;

import java.util.List;

@ClientSidePort
public interface FamilyService {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;

}