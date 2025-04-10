package fr.insee.rmes.rbac;

public class UnknownPrivilegeException extends RuntimeException {
    public UnknownPrivilegeException(RBAC.Privilege privilege, String roleName, RBAC.Module application) {
        super("Unknown privilege '" + privilege + "' for role: " + roleName + " in application: " + application);
    }
}