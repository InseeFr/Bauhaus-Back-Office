package fr.insee.rmes.model.rbac;

import java.util.Set;

public record ModuleAccessPrivileges(RBAC.APPLICATION application, Set<Privilege> privileges) {

    public record Privilege(RBAC.PRIVILEGE privilege, RBAC.STRATEGY strategy) {
    }

}
