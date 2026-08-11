package fr.insee.rmes.modules.operations.families.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;


import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeries;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySubject;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;

import java.util.List;

@ServerSidePort
public interface OperationFamilyRepository {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFullFamily(String id) throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;
    List<OperationFamilySeries> getFamilySeries(String id) throws RmesException;
    List<OperationFamilySubject> getFamilySubjects(String id) throws RmesException;
}