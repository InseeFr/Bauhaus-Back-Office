package fr.insee.rmes.modules.users.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;

import java.util.List;
import java.util.Set;

@ClientSidePort
public interface UserService {
    Set<Stamp> findStampsFrom(Object principal) throws MissingUserInformationException;

    User getUser(Object principal) throws MissingUserInformationException;

    Set<ModuleAccessPrivileges> computePrivileges(Object principal) throws MissingUserInformationException;
}
