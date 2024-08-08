package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.ModuleAccessPrivileges;
import fr.insee.rmes.model.rbac.RBAC;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An instance of AccessPrivileges gathers strategysByPrivileges for all modules of the application.
 *
 */
public record ApplicationAccessPrivileges(EnumMap<RBAC.Module , ModuleAccessPrivileges> privilegesByModules) {
    public static final ApplicationAccessPrivileges NO_PRIVILEGE = new ApplicationAccessPrivileges(new EnumMap<>(RBAC.Module.class));
    private RBAC.Privilege action;
    private RBAC.Module resource;

    public ApplicationAccessPrivileges(Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> privileges) {
        this.privileges = privileges;
    }

    public ApplicationAccessPrivileges isGranted(RBAC.Privilege action) {
        this.action = action;
        return this;
    }

    public ApplicationAccessPrivileges on(RBAC.Module resource) {
        this.resource = resource;
        return this;
    }

    public boolean withId(String id) {
        return checkPrivileges(id);
    }

    private boolean checkPrivileges(String id) {
        Map<RBAC.Privilege, RBAC.Strategy> resourcePrivileges = privileges.get(resource);
        if (resourcePrivileges == null) {
            return false;
        }

        RBAC.Strategy strategy = resourcePrivileges.get(action);
        if (strategy == null) {
            return false;
        }

        return strategy == RBAC.Strategy.ALL || (strategy == RBAC.Strategy.STAMP && checkStamp(id));
    }

    private boolean checkStamp(String id) {
        // Implémentez la logique pour vérifier le stamp
        return true; // Exemple simplifié
    }

    public ApplicationAccessPrivileges merge(ApplicationAccessPrivileges other) {
        Objects.requireNonNull(other);
        var mergedMap = new EnumMap<RBAC.Module, ModuleAccessPrivileges>(RBAC.Module.class);
        for (RBAC.Module module : RBAC.Module.values()) {
            var moduleMergedPrivileges = ModuleAccessPrivileges.merge(privilegesByModules.get(module), other.privilegesByModules.get(module));
            moduleMergedPrivileges.ifPresent(privilege->mergedMap.put(module,privilege));
        }
        return new ApplicationAccessPrivileges(mergedMap);
    }
}
