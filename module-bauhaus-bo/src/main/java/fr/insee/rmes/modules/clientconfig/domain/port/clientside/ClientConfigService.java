package fr.insee.rmes.modules.clientconfig.domain.port.clientside;

import fr.insee.rmes.modules.clientconfig.domain.model.ClientConfigProperties;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;

@ClientSidePort
public interface ClientConfigService {
    ClientConfigProperties getClientConfigProperties();
}
