package fr.insee.rmes.onion.domain.port.serverside.operations;


import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.OperationFamily;
import fr.insee.rmes.onion.domain.model.operations.OperationFamilySeries;
import fr.insee.rmes.onion.domain.model.operations.OperationFamilySubject;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;

import java.util.List;

public interface OperationFamilyRepository {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFullFamily(String id) throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;
    List<OperationFamilySeries> getFamilySeries(String id) throws RmesException;
    List<OperationFamilySubject> getFamilySubjects(String id) throws RmesException;
}
