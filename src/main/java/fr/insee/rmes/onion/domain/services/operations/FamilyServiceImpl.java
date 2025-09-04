package fr.insee.rmes.onion.domain.services.operations;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.model.operations.OperationFamily;
import fr.insee.rmes.onion.domain.model.operations.PartialOperationFamily;
import fr.insee.rmes.onion.domain.port.clientside.operations.FamilyService;
import fr.insee.rmes.onion.domain.port.serverside.operations.OperationFamilyRepository;
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
        logger.info("Starting to get family " + id);
        return operationFamilyRepository.getFullFamily(id);
    }

}
