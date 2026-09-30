package fr.insee.rmes.domain.exceptions;

import org.json.JSONObject;

/**
 * {@link RmesException} dont le corps suit l'ADR-1264 : un {@code code} chaîne, clé de traduction
 * côté front ({@code errors.<code>}), et un {@code message} en anglais, repli sans traduction. Le
 * détail technique reste dans la cause, pour les logs.
 */
public class CodedRmesException extends RmesException {

    private final String code;

    public CodedRmesException(int status, String code, String message, Throwable cause) {
        super(status, message, null, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getDetails() {
        return new JSONObject().put("message", getMessage()).put("code", code).toString();
    }
}
