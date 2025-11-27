package fr.insee.rmes.modules.users.domain.exceptions;

public class MissingUserInformationException extends Throwable {
    public MissingUserInformationException(String message) {
        super(message);
    }
}
