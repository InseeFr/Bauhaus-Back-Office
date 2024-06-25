package fr.insee.rmes.model.rbac;

import java.util.Set;

public record AllModuleAccessPrivileges(RoleName roleName, Set<ModuleAccessPrivileges> privileges) {


    public record RoleName(String role) {}
}
