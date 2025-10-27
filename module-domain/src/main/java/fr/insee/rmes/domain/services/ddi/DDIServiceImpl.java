package fr.insee.rmes.domain.services.ddi;

import fr.insee.rmes.domain.model.ddi.Ddi4Response;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.domain.port.clientside.DDIService;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DDIServiceImpl implements DDIService {
    static final Logger logger = LoggerFactory.getLogger(DDIServiceImpl.class);

    private final DDIRepository ddiRepository;

    public DDIServiceImpl(DDIRepository ddiRepository) {
        this.ddiRepository = ddiRepository;
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Starting to get physical instances list");
        return ddiRepository.getPhysicalInstances();
    }


    @Override
    public Ddi4Response getDdi4PhysicalInstance(String id) {
        return this.ddiRepository.getPhysicalInstance(id);
    }

    @Override
    public Ddi4Response updatePhysicalInstance(String id, UpdatePhysicalInstanceRequest request) {
        ddiRepository.updatePhysicalInstance(id, request);
        return ddiRepository.getPhysicalInstance(id);
    }
}