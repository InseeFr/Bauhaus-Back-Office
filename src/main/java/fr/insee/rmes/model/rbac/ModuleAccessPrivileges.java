package fr.insee.rmes.model.rbac;

import java.util.Set;

public record ModuleAccessPrivileges(RBAC.Module application, Set<Privilege> privileges) {

    public record Privilege(RBAC.Privilege privilege, RBAC.Strategy strategy) {
    }

}
