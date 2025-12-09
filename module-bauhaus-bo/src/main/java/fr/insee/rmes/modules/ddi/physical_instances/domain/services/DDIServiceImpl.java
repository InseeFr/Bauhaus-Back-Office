package fr.insee.rmes.modules.ddi.physical_instances.domain.services;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
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
    public List<PartialCodesList> getCodesLists() {
        logger.info("Starting to get codes lists");
        return ddiRepository.getCodesLists();
    }


    @Override
    public Ddi4Response getDdi4PhysicalInstance(String agencyId, String id) {
        return this.ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        ddiRepository.updatePhysicalInstance(agencyId, id, request);
        return ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        logger.info("Updating full physical instance with agencyId: {} and id: {}", agencyId, id);
        ddiRepository.updateFullPhysicalInstance(agencyId, id, ddi4Response);
        return ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        logger.info("Creating new physical instance with label: {}", request.physicalInstanceLabel());
        return ddiRepository.createPhysicalInstance(request);
    }
}