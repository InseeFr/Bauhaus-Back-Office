package fr.insee.rmes.model.rbac;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An instance of AccessPrivileges gathers strategysByPrivileges for all modules of the application.
 *
 */
public record ApplicationAccessPrivileges(EnumMap<Module, ModuleAccessPrivileges> privilegesByModules) {

    public ApplicationAccessPrivileges {
        Objects.requireNonNull(privilegesByModules);
    }

    public static final ApplicationAccessPrivileges NO_PRIVILEGE = new ApplicationAccessPrivileges(new EnumMap<>(Module.class));

    public static ApplicationAccessPrivileges of(Map<Module, Map<Privilege, Strategy>> privilegesByModules) {
        return new ApplicationAccessPrivileges(new EnumMap<>(privilegesByModules.entrySet().stream()
                .collect(
                        Collectors.toMap(Map.Entry<Module, Map<Privilege, Strategy>>::getKey,
                                entry -> new ModuleAccessPrivileges(entry.getValue()),
                                ModuleAccessPrivileges::merge
                        )
                )));
    }

    public ApplicationAccessPrivileges merge(ApplicationAccessPrivileges other) {
        Objects.requireNonNull(other);
        var mergedMap = new EnumMap<Module, ModuleAccessPrivileges>(Module.class);
        for (Module module : Module.values()) {
            mergedMap.put(module, ModuleAccessPrivileges.merge(privilegesByModules.get(module), other.privilegesByModules.get(module)));
        }
        return new ApplicationAccessPrivileges(mergedMap);
    }

    public ModuleAccessPrivileges privilegesForModule(Module module) {
        var privileges = privilegesByModules.get(module);
        return privileges==null?ModuleAccessPrivileges.NO_PRIVILEGE:privileges;
    }
}
