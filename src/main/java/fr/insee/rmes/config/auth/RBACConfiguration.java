package fr.insee.rmes.config.auth;

import fr.insee.rmes.external.services.rbac.ApplicationAccessPrivileges;
import fr.insee.rmes.model.rbac.ModuleAccessPrivileges;
import fr.insee.rmes.model.rbac.RBAC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@ConfigurationProperties("rbac")
public record RBACConfiguration (Map<RoleName, ApplicationAccessPrivileges> applicationAccessPrivilegesByRoles){

    @ConstructorBinding
    public RBACConfiguration(Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> config){
        this(toSetOfAllModulesAccessPrivileges(config));
    }

    private static Set<AllModuleAccessPrivileges> toSetOfAllModulesAccessPrivileges(Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> rbac) {
        return rbac.entrySet().stream()
                .map(RBACConfiguration::toAllModuleAccessPrivileges)
                .collect(Collectors.toSet());
    }

    private static AllModuleAccessPrivileges toAllModuleAccessPrivileges(Map.Entry<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> entry) {
        return new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName(entry.getKey()), toSetOfModuleAccessPrivileges(entry.getValue()));
    }

    private static Set<ModuleAccessPrivileges> toSetOfModuleAccessPrivileges(Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> privilegesForOneRole) {
        return privilegesForOneRole.entrySet().stream()
                .map(RBACConfiguration::toModuleAccessPrivileges)
                .collect(Collectors.toSet());
    }

    private static ModuleAccessPrivileges toModuleAccessPrivileges(Map.Entry<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> entry) {
        return new ModuleAccessPrivileges(entry.getKey(), toSetOfPrivileges(entry.getValue()));
    }

    private static Set<ModuleAccessPrivileges.Privilege> toSetOfPrivileges(Map<RBAC.Privilege, RBAC.Strategy> privilegesForOneModule) {
        return privilegesForOneModule.entrySet().stream()
                .map(RBACConfiguration::toPrivilege)
                .collect(Collectors.toSet());
    }

    private static ModuleAccessPrivileges.Privilege toPrivilege(Map.Entry<RBAC.Privilege, RBAC.Strategy> entry) {
        return new ModuleAccessPrivileges.Privilege(entry.getKey(), entry.getValue());
    }

    public Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> getRbac() {
        return allModulesAccessPrivileges.stream()
                .collect(Collectors.toMap(
                        privilege -> privilege.roleName().role(),
                        privilege -> privilege.privileges().stream()
                                .collect(Collectors.toMap(
                                        ModuleAccessPrivileges::application,
                                        moduleAccess -> moduleAccess.privileges().stream()
                                                .collect(Collectors.toMap(
                                                        ModuleAccessPrivileges.Privilege::privilege,
                                                        ModuleAccessPrivileges.Privilege::strategy
                                                ))
                                ))
                ));
    }

    public ApplicationAccessPrivileges accessPrivilegesForRole(RoleName roleName) {
    }

    public record RoleName(String role) {}
}
