package fr.insee.rmes.rbac;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@ConfigurationProperties("rbac")
public record RBACConfiguration (Set<AllModuleAccessPrivileges> allModulesAccessPrivileges){

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

}
