package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.RBAC;

import java.util.Map;


public class AccessPrivileges {
    private final Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> privileges;
    private RBAC.Privilege action;
    private RBAC.Module resource;

    public AccessPrivileges(Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> privileges) {
        this.privileges = privileges;
    }

    public AccessPrivileges isGranted(RBAC.Privilege action) {
        this.action = action;
        return this;
    }

    public AccessPrivileges on(RBAC.Module resource) {
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
}
