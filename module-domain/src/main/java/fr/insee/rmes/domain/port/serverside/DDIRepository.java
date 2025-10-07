package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.model.ddi.Ddi4Response;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.UpdatePhysicalInstanceRequest;

import java.util.List;

public interface DDIRepository {
    List<PartialPhysicalInstance> getPhysicalInstances();
    Ddi4Response getPhysicalInstance(String id);
    void updatePhysicalInstance(String id, UpdatePhysicalInstanceRequest request);
}