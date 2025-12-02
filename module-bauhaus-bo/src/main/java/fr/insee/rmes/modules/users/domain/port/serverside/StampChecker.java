package fr.insee.rmes.modules.users.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.RBAC;

import java.util.List;

@ServerSidePort
public interface StampChecker {
    List<String> getCreatorsStamps(RBAC.Module module, String id) throws UnsupportedModuleException, StampFetchException;
    List<String> getContributorsStamps(RBAC.Module module, String id) throws UnsupportedModuleException, StampFetchException;
}
