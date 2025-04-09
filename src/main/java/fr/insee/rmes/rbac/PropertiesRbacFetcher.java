package fr.insee.rmes.rbac;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PropertiesRbacFetcher implements RbacFetcher {

    private final Set<AllModuleAccessPrivileges> allModulesAccessPrivileges;

    public PropertiesRbacFetcher(RBACConfiguration rbacConfiguration) {
        this.allModulesAccessPrivileges = rbacConfiguration.allModulesAccessPrivileges();
    }

    @Override
    public Set<ModuleAccessPrivileges> getPrivilegesByRole(String roleName) {
        return this.allModulesAccessPrivileges.stream().filter(p -> p.roleName().role().equals(roleName)).findFirst().get().privileges();
    }

    @Override
    public Set<ModuleAccessPrivileges.Privilege> getApplicationPrivilegesByRole(String roleName, RBAC.Module application) {
        var moduleAccessPrivileges = this.getPrivilegesByRole(roleName);
        return moduleAccessPrivileges.stream().filter(p -> p.application().equals(application)).findFirst().get().privileges();
    }

    @Override
    public RBAC.Strategy getApplicationActionStrategyByRole(String roleName, RBAC.Module application, RBAC.Privilege privilege) {
        var privileges = getApplicationPrivilegesByRole(roleName, application);
        return privileges.stream().filter(p -> p.privilege().equals(privilege)).findFirst().get().strategy();
    }
}
