package fr.insee.rmes.modules.commons.domain.service;

import fr.insee.rmes.modules.commons.domain.model.DisseminationStatus;
import fr.insee.rmes.modules.commons.domain.port.clientside.DisseminationStatusService;

import java.util.List;

public class DomainDisseminationStatusService implements DisseminationStatusService
{
    @Override
    public List<DisseminationStatus> getDisseminationStatus() {
        return List.of(DisseminationStatus.PRIVATE, DisseminationStatus.PUBLIC_GENERIC, DisseminationStatus.PUBLIC_SPECIFIC);
    }
}
