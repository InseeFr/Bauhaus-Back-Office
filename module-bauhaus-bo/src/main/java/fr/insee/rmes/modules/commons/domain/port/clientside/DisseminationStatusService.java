package fr.insee.rmes.modules.commons.domain.port.clientside;

import fr.insee.rmes.modules.commons.domain.model.DisseminationStatus;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;

import java.util.List;

@ClientSidePort
public interface DisseminationStatusService {
    List<DisseminationStatus> getDisseminationStatus();
}
