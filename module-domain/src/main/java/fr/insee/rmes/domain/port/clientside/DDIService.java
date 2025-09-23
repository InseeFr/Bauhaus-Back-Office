package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;

import java.util.List;

public interface DDIService {
    List<PartialPhysicalInstance> getPhysicalInstances();
    PhysicalInstance getPhysicalInstance(String id);
}