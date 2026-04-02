package fr.insee.rmes.modules.commons.domain.exceptions;

public class ThemeFetchException extends Exception {
    public ThemeFetchException(Throwable cause) {
        super("Failed to fetch themes", cause);
    }
}
