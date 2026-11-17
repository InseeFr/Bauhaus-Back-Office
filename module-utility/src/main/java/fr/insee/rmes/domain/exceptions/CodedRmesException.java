package fr.insee.rmes.domain.exceptions;

import org.json.JSONObject;

/**
 * {@link RmesException} dont le corps suit l'ADR-1264 : un {@code code} chaîne, clé de traduction
 * côté front ({@code errors.<code>}), et un {@code message} en anglais, repli sans traduction. Le
 * détail technique reste dans la cause, pour les logs.
 */
public class CodedRmesException extends RmesException {

    private final String code;

    private final String clientMessage;

    public CodedRmesException(int status, String code, String message, Throwable cause) {
        super(status, message, null, cause);
        this.code = code;
        this.clientMessage = message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getDetails() {
        // getMessage() peut être redéfini pour les logs : le corps garde le message écrit pour le client
        return new JSONObject().put("message", clientMessage).put("code", code).toString();
    }
}
