package fr.insee.rmes.model.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;

import java.util.Map;
import java.util.Objects;

public record RBAC(Map<RBACConfiguration.RoleName, ApplicationAccessPrivileges> applicationAccessPrivilegesByRoles) {

    public RBAC{
        Objects.requireNonNull(applicationAccessPrivilegesByRoles);
    }

}
