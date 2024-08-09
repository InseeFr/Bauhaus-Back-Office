package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.ModuleAccessPrivileges;
import fr.insee.rmes.model.rbac.RBAC;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An instance of AccessPrivileges gathers strategysByPrivileges for all modules of the application.
 *
 */
public record ApplicationAccessPrivileges(EnumMap<RBAC.Module , ModuleAccessPrivileges> privilegesByModules) {
    public static final ApplicationAccessPrivileges NO_PRIVILEGE = new ApplicationAccessPrivileges(new EnumMap<>(RBAC.Module.class));

    public static ApplicationAccessPrivileges of(Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> privilegesByModules) {
        return new ApplicationAccessPrivileges(new EnumMap<>(privilegesByModules.entrySet().stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey,
                                entry -> new ModuleAccessPrivileges(entry.getValue()),
                                ModuleAccessPrivileges::merge
                        )
                )));
    }

    public ApplicationAccessPrivileges merge(ApplicationAccessPrivileges other) {
        Objects.requireNonNull(other);
        var mergedMap = new EnumMap<RBAC.Module, ModuleAccessPrivileges>(RBAC.Module.class);
        for (RBAC.Module module : RBAC.Module.values()) {
            mergedMap.put(module, ModuleAccessPrivileges.merge(privilegesByModules.get(module), other.privilegesByModules.get(module)));
        }
        return new ApplicationAccessPrivileges(mergedMap);
    }

    public ModuleAccessPrivileges privilegesForModule(RBAC.Module module) {
        var privileges = privilegesByModules.get(module);
        return privileges==null?ModuleAccessPrivileges.NO_PRIVILEGE:privileges;
    }
}
