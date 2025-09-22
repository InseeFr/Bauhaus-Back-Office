package fr.insee.rmes.domain.services.operations;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.domain.port.clientside.operations.FamilyService;
import fr.insee.rmes.domain.port.serverside.OperationFamilyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FamilyServiceImpl implements FamilyService {
    static final Logger logger = LoggerFactory.getLogger(FamilyServiceImpl.class);

    private final OperationFamilyRepository operationFamilyRepository;

    public FamilyServiceImpl(OperationFamilyRepository operationFamilyRepository) {
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