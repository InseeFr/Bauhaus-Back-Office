package fr.insee.rmes.rbac;

import fr.insee.rmes.domain.auth.User;

import java.util.List;
import java.util.Set;

public interface RbacFetcher {

    Set<ModuleAccessPrivileges> getPrivilegesByRole(String roleName);

    Set<ModuleAccessPrivileges.Privilege> getApplicationPrivilegesByRole(String roleName, RBAC.Module application);

    RBAC.Strategy getApplicationActionStrategyByRole(List<String> roles, RBAC.Module application, RBAC.Privilege privilege);

    Set<ModuleAccessPrivileges> computePrivileges(User roles);
}
