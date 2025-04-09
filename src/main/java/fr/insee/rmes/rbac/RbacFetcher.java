package fr.insee.rmes.rbac;

import java.util.Set;

public interface RbacFetcher {

    Set<ModuleAccessPrivileges> getPrivilegesByRole(String roleName);

    Set<ModuleAccessPrivileges.Privilege> getApplicationPrivilegesByRole(String roleName, RBAC.Module application);

    RBAC.Strategy getApplicationActionStrategyByRole(String roleName, RBAC.Module application, RBAC.Privilege privilege);
}
