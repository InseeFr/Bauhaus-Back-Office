package fr.insee.rmes.modules.users.domain.model;

import fr.insee.rmes.rbac.ModuleAccessPrivileges;

import java.util.Set;

public record AllModuleAccessPrivileges(RoleName roleName, Set<ModuleAccessPrivileges> privileges) {


    public record RoleName(String role) {}
}
