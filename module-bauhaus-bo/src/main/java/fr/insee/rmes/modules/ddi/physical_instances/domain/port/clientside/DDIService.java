package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

import java.util.List;

public interface DDIService {
    List<PartialPhysicalInstance> getPhysicalInstances();
    Ddi4Response getDdi4PhysicalInstance(String id);
    Ddi4Response updatePhysicalInstance(String id, UpdatePhysicalInstanceRequest request);
}