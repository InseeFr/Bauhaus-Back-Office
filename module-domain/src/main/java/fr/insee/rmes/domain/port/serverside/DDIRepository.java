package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;

import java.util.List;

public interface DDIRepository {
    List<PartialPhysicalInstance> getPhysicalInstances();
    PhysicalInstance getPhysicalInstance(String id);
}