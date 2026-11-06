package fr.insee.rmes.modules.operations.documents.domain.exceptions;

/** Une règle de gestion des documents et des liens refuse l'opération. */
public class DocumentRuleViolationException extends Exception {

    public enum Violation {
        LABEL_LG1_ALREADY_USED,
        LABEL_LG2_ALREADY_USED,
        LINK_EMPTY_URL,
        LINK_BAD_URL,
        LINK_URL_ALREADY_USED,
        FILE_EMPTY_NAME,
        FILE_FORBIDDEN_CHARACTERS,
        FILE_EXTENSION_NOT_ALLOWED,
        FILE_ALREADY_EXISTS,
        REFERENCED_BY_SIMS
    }

    private final Violation violation;
    private final String detail;

    public DocumentRuleViolationException(Violation violation, String message, String detail) {
        super(message);
        this.violation = violation;
        this.detail = detail;
    }

    public Violation violation() {
        return violation;
    }

    public String detail() {
        return detail;
    }
}
