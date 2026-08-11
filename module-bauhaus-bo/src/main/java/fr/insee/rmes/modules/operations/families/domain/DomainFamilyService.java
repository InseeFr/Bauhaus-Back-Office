package fr.insee.rmes.modules.operations.families.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.operations.families.domain.port.clientside.FamilyService;
import fr.insee.rmes.modules.operations.families.domain.port.serverside.OperationFamilyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DomainFamilyService implements FamilyService {
    static final Logger logger = LoggerFactory.getLogger(DomainFamilyService.class);

    private final OperationFamilyRepository operationFamilyRepository;

    public DomainFamilyService(OperationFamilyRepository operationFamilyRepository) {
        this.operationFamilyRepository = operationFamilyRepository;
    }

    @Override
    public List<PartialOperationFamily> getFamilies() throws RmesException {
        logger.info("Starting to get families list");
        return operationFamilyRepository.getFamilies();
    }

    @Override
    public OperationFamily getFamily(String id) throws RmesException {
        logger.info("Starting to get family");
        return operationFamilyRepository.getFullFamily(id);
    }

}