package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

import java.util.List;
import java.util.Map;

/**
 * Réutilisation de variables (#1674) incohérente avec la PhysicalInstance source : groupe qui n'est
 * pas le sien, ou variable qu'elle n'utilise pas.
 *
 * <p>Traduite en 400 {@code {message, code, params}} par le {@code DdiExceptionHandler} : le front
 * traduit le {@link Code}, le {@code message} (en anglais) ne sert que de repli et pour les logs.
 */
public class InvalidReuseVariablesException extends RuntimeException {

    /** Cas d'erreur, identifiants stables : le front en fait ses clés de traduction. */
    public enum Code {
        REUSE_VARIABLES_GROUP_MISMATCH,
        REUSE_VARIABLES_NOT_IN_SOURCE
    }

    private final Code code;
    private final Map<String, String> params;

    private InvalidReuseVariablesException(Code code, Map<String, String> params, String message) {
        super(message);
        this.code = code;
        this.params = params;
    }

    public static InvalidReuseVariablesException groupMismatch(String groupAgency, String groupId) {
        return new InvalidReuseVariablesException(
                Code.REUSE_VARIABLES_GROUP_MISMATCH,
                Map.of("groupAgency", String.valueOf(groupAgency), "groupId", String.valueOf(groupId)),
                "Group " + groupAgency + "/" + groupId + " is not the group of the source physical instance");
    }

    public static InvalidReuseVariablesException notInSource(List<String> variableIds) {
        String variables = String.join(", ", variableIds);
        return new InvalidReuseVariablesException(
                Code.REUSE_VARIABLES_NOT_IN_SOURCE,
                Map.of("variables", variables),
                "Variables not used by the source physical instance: " + variables);
    }

    public Code code() {
        return code;
    }

    public Map<String, String> params() {
        return params;
    }
}
