package fr.insee.rmes.modules.commons.domain.port.clientside;

import fr.insee.rmes.modules.commons.domain.model.DisseminationStatus;

import java.util.List;

public interface DisseminationStatusService {
    List<DisseminationStatus> getDisseminationStatus();
}
