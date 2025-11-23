package fr.insee.rmes.modules.users.domain.exceptions;

import fr.insee.rmes.modules.users.domain.model.RBAC;

public class UnknownApplicationException extends RuntimeException {
    public UnknownApplicationException(RBAC.Module application, String roleName) {
        super("Unknown application '" + application + "' for role: " + roleName);
    }
}