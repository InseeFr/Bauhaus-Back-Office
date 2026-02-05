package fr.insee.rmes.modules.users.domain.exceptions;

import fr.insee.rmes.modules.users.domain.model.RBAC;

public class UnknownPrivilegeException extends RuntimeException {
    public UnknownPrivilegeException(RBAC.Privilege privilege, String roleName, RBAC.Module application) {
        super("Unknown privilege '" + privilege + "' for role: " + roleName + " in application: " + application);
    }
}