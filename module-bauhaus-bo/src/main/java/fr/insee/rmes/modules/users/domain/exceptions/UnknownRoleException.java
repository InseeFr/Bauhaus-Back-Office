package fr.insee.rmes.modules.users.domain.exceptions;

public class UnknownRoleException extends RuntimeException {
    public UnknownRoleException(String roleName) {
        super("Unknown role: " + roleName);
    }
}