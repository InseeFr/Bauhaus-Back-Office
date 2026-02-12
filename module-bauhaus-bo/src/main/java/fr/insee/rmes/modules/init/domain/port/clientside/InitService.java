package fr.insee.rmes.modules.init.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.init.domain.model.InitProperties;

@ClientSidePort
public interface InitService {
    InitProperties getInitProperties();
}
