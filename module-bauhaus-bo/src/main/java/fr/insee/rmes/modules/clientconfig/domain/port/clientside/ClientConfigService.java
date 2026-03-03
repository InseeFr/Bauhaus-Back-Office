package fr.insee.rmes.modules.clientconfig.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.clientconfig.domain.model.ClientConfigProperties;

@ClientSidePort
public interface ClientConfigService {
    ClientConfigProperties getClientConfigProperties();
}
