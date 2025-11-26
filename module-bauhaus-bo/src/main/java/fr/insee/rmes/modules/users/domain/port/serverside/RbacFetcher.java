package fr.insee.rmes.modules.users.domain.port.serverside;

import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;

import java.util.List;
import java.util.Set;

public interface RbacFetcher {

    Set<ModuleAccessPrivileges> getPrivilegesByRole(String roleName);

    Set<ModuleAccessPrivileges.Privilege> getApplicationPrivilegesByRole(String roleName, RBAC.Module application);

    RBAC.Strategy getApplicationActionStrategyByRole(List<String> roles, RBAC.Module application, RBAC.Privilege privilege);

    Set<ModuleAccessPrivileges> computePrivileges(List<String> roles);
}
