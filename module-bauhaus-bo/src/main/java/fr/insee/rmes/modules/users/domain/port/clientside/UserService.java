package fr.insee.rmes.modules.users.domain.port.clientside;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;

import java.util.List;
import java.util.Set;

public interface UserService {
    Stamp findStampFrom(Object principal) throws MissingUserInformationException;

    User getUser(Object principal) throws MissingUserInformationException;

    Set<ModuleAccessPrivileges> computePrivileges(Object principal) throws MissingUserInformationException;
}
