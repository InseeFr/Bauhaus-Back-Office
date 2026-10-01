package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

import java.util.Map;

/**
 * Payload de sauvegarde invalide côté valeurs sentinelles (#1566) : une
 * ManagedMissingValuesRepresentation ou sa CodeList de sentinelles sans label — la carte les rend
 * obligatoires. Traduite en 400 {@code {message, code, params}} par le {@code DdiExceptionHandler} :
 * le front traduit {@code code} avec {@code params}, {@code message} (anglais) sert de repli.
 */
public class InvalidSentinelValuesException extends RuntimeException {

    public enum Code {
        DDI_SENTINEL_REPRESENTATION_LABEL_REQUIRED,
        DDI_SENTINEL_CODE_LIST_LABEL_REQUIRED
    }

    private final Code code;

    private final Map<String, String> params;

    private InvalidSentinelValuesException(Code code, String message, String agency, String id) {
        super(message);
        this.code = code;
        this.params = Map.of("agency", agency, "id", id);
    }

    public static InvalidSentinelValuesException missingRepresentationLabel(String agency, String id) {
        return new InvalidSentinelValuesException(
                Code.DDI_SENTINEL_REPRESENTATION_LABEL_REQUIRED,
                "The label of the sentinel value list %s/%s is required".formatted(agency, id),
                agency,
                id);
    }

    public static InvalidSentinelValuesException missingCodeListLabel(String agency, String id) {
        return new InvalidSentinelValuesException(
                Code.DDI_SENTINEL_CODE_LIST_LABEL_REQUIRED,
                "The label of the sentinel code list %s/%s is required".formatted(agency, id),
                agency,
                id);
    }

    public Code code() {
        return code;
    }

    public Map<String, String> params() {
        return params;
    }
}
