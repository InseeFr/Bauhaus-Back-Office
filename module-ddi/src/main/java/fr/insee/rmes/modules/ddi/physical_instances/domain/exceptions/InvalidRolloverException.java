package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

import java.util.List;
import java.util.Map;

/**
 * Nouvelle période (#1672) incohérente avec la PhysicalInstance source : groupe qui n'est pas le
 * sien, étude cible identique à la sienne ou hors de son groupe, variable qu'elle n'utilise pas, ou
 * variable réutilisée qui n'est pas dérivée de la variable source dans l'étude cible.
 *
 * <p>Traduite en 400 {@code {message, code, params}} par le {@code DdiExceptionHandler} : le front
 * traduit le {@link Code}, le {@code message} (en anglais) ne sert que de repli et pour les logs.
 */
public class InvalidRolloverException extends RuntimeException {

    /** Cas d'erreur, identifiants stables : le front en fait ses clés de traduction. */
    public enum Code {
        ROLLOVER_GROUP_MISMATCH,
        ROLLOVER_SAME_STUDY_UNIT,
        ROLLOVER_STUDY_UNIT_NOT_IN_GROUP,
        ROLLOVER_VARIABLES_NOT_IN_SOURCE,
        ROLLOVER_INVALID_REUSE
    }

    private final Code code;
    private final Map<String, String> params;

    private InvalidRolloverException(Code code, Map<String, String> params, String message) {
        super(message);
        this.code = code;
        this.params = params;
    }

    public static InvalidRolloverException groupMismatch(String groupAgency, String groupId) {
        return new InvalidRolloverException(
                Code.ROLLOVER_GROUP_MISMATCH,
                Map.of("groupAgency", String.valueOf(groupAgency), "groupId", String.valueOf(groupId)),
                "Group " + groupAgency + "/" + groupId + " is not the group of the source physical instance");
    }

    public static InvalidRolloverException sameStudyUnit(String studyUnitAgency, String studyUnitId) {
        return new InvalidRolloverException(
                Code.ROLLOVER_SAME_STUDY_UNIT,
                Map.of("studyUnitAgency", String.valueOf(studyUnitAgency), "studyUnitId", String.valueOf(studyUnitId)),
                "Study unit " + studyUnitAgency + "/" + studyUnitId
                        + " is the study unit of the source physical instance");
    }

    public static InvalidRolloverException studyUnitNotInGroup(String studyUnitAgency, String studyUnitId) {
        return new InvalidRolloverException(
                Code.ROLLOVER_STUDY_UNIT_NOT_IN_GROUP,
                Map.of("studyUnitAgency", String.valueOf(studyUnitAgency), "studyUnitId", String.valueOf(studyUnitId)),
                "Study unit " + studyUnitAgency + "/" + studyUnitId
                        + " does not belong to the group of the source physical instance");
    }

    public static InvalidRolloverException notInSource(List<String> variableIds) {
        String variables = String.join(", ", variableIds);
        return new InvalidRolloverException(
                Code.ROLLOVER_VARIABLES_NOT_IN_SOURCE,
                Map.of("variables", variables),
                "Variables not used by the source physical instance: " + variables);
    }

    public static InvalidRolloverException invalidReuse(List<String> variableIds) {
        String variables = String.join(", ", variableIds);
        return new InvalidRolloverException(
                Code.ROLLOVER_INVALID_REUSE,
                Map.of("variables", variables),
                "Variables not derived from the source variable in the target study unit: " + variables);
    }

    public Code code() {
        return code;
    }

    public Map<String, String> params() {
        return params;
    }
}
