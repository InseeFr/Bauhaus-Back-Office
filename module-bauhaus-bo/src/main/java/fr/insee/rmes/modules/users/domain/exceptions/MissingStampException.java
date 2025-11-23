package fr.insee.rmes.modules.users.domain.exceptions;

public class MissingStampException extends MissingUserInformationException {
    public MissingStampException(String id) {
        super("The User " + id + " does not have a stamp");
    }
}
