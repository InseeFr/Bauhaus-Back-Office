package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

import java.util.List;

public interface DDIRepository {
    List<PartialPhysicalInstance> getPhysicalInstances();
    Ddi4Response getPhysicalInstance(String agencyId, String id);
    void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);
    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request);
}