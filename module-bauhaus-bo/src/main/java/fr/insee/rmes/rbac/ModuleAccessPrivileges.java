package fr.insee.rmes.rbac;

import fr.insee.rmes.modules.users.domain.model.RBAC;

import java.util.Set;

public record ModuleAccessPrivileges(RBAC.Module application, Set<Privilege> privileges) {

    public record Privilege(RBAC.Privilege privilege, RBAC.Strategy strategy) {
    }

}
