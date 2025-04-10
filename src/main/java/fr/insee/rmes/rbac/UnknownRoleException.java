package fr.insee.rmes.rbac;

public class UnknownRoleException extends RuntimeException {
    public UnknownRoleException(String roleName) {
        super("Unknown role: " + roleName);
    }
}