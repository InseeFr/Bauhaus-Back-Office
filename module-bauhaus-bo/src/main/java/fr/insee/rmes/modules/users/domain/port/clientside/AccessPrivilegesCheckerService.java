package fr.insee.rmes.modules.users.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;

@ClientSidePort
public interface AccessPrivilegesCheckerService {

    boolean hasAccess(String module, String privilege, String id, Object user) throws MissingUserInformationException;


}
