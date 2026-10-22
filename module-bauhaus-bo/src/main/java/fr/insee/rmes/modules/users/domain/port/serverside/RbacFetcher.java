package fr.insee.rmes.modules.users.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Source;
import java.util.List;
import java.util.Set;

@ServerSidePort
public interface RbacFetcher {

    Set<ModuleAccessPrivileges> getPrivilegesByRole(String roleName);

    Set<ModuleAccessPrivileges.Privilege> getApplicationPrivilegesByRole(String roleName, RBAC.Module application);

    RBAC.Strategy getApplicationActionStrategyByRole(
            List<String> roles, RBAC.Module application, RBAC.Privilege privilege);

    Set<ModuleAccessPrivileges> computePrivileges(List<String> roles, Source source);

    /**
     * Stratégie d'un privilège telle que la donne {@link #computePrivileges} (lecture {@code ALL}
     * pour la source INSEE comprise), {@code NONE} si aucun rôle ne l'accorde.
     */
    RBAC.Strategy computeStrategy(List<String> roles, Source source, RBAC.Module module, RBAC.Privilege privilege);
}
