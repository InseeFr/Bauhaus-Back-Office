package fr.insee.rmes.modules.users.domain.exceptions;

public class EmptyUserInformationException extends MissingUserInformationException {
    public EmptyUserInformationException() {
        super("The claims are empty");
    }
}
