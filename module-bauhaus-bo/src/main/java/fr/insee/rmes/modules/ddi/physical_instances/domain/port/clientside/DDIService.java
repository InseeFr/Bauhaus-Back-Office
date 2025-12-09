package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;

import java.util.List;

public interface DDIService {
    List<PartialPhysicalInstance> getPhysicalInstances();
    List<PartialCodesList> getCodesLists();
    Ddi4Response getDdi4PhysicalInstance(String agencyId, String id);
    Ddi4Response updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request);
    Ddi4Response updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response);
    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request);
}