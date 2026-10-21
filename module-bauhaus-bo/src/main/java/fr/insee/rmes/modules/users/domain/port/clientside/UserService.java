package fr.insee.rmes.modules.users.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import java.util.Set;

@ClientSidePort
public interface UserService {
    Set<Stamp> findStampsFrom(Object principal) throws MissingUserInformationException;

    User getUser(Object principal) throws MissingUserInformationException;

    Set<ModuleAccessPrivileges> computePrivileges(Object principal) throws MissingUserInformationException;

    /** Stratégie d'un privilège pour cet utilisateur, source comprise (voir {@code RbacFetcher#computeStrategy}). */
    RBAC.Strategy computeStrategy(User user, RBAC.Module module, RBAC.Privilege privilege);
}
