package fr.insee.rmes.rbac;

public class UnknownApplicationException extends RuntimeException {
    public UnknownApplicationException(RBAC.Module application, String roleName) {
        super("Unknown application '" + application + "' for role: " + roleName);
    }
}