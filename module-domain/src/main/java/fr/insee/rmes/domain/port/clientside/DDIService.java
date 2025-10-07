package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.model.ddi.Ddi4Response;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.domain.model.ddi.UpdatePhysicalInstanceRequest;

import java.util.List;

public interface DDIService {
    List<PartialPhysicalInstance> getPhysicalInstances();
    Ddi4Response getDdi4PhysicalInstance(String id);
    Ddi4Response updatePhysicalInstance(String id, UpdatePhysicalInstanceRequest request);
}