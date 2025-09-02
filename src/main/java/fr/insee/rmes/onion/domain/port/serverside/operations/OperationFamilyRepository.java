package fr.insee.rmes.onion.domain.port.serverside.operations;


import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;

import java.util.List;

public interface OperationFamilyRepository {
    List<PartialOperationFamily> getFamilies() throws RmesException;
}
