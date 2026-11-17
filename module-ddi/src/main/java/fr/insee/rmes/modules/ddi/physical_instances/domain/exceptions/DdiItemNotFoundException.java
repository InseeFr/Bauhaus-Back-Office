package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

import java.util.Map;

/**
 * Un objet DDI demandé (groupe, étude, instance physique) n'existe pas dans Colectica.
 *
 * <p>Traduite en 404 {@code {message, code, params}} par le {@code DdiExceptionHandler} (ADR-1264) :
 * le front traduit le {@link Code}, le {@code message} (en anglais) ne sert que de repli et pour les
 * logs.
 */
public class DdiItemNotFoundException extends RuntimeException {

    /** Cas d'erreur, identifiants stables : le front en fait ses clés de traduction. */
    public enum Code {
        DDI_GROUP_NOT_FOUND,
        DDI_STUDY_UNIT_NOT_FOUND,
        DDI_PHYSICAL_INSTANCE_NOT_FOUND
    }

    private final Code code;
    private final Map<String, String> params;

    protected DdiItemNotFoundException(Code code, Map<String, String> params, String message) {
        super(message);
        this.code = code;
        this.params = params;
    }

    public static DdiItemNotFoundException group(String agencyId, String id) {
        return new DdiItemNotFoundException(
                Code.DDI_GROUP_NOT_FOUND, identity(agencyId, id), "Group not found: " + agencyId + "/" + id);
    }

    public static DdiItemNotFoundException studyUnit(String agencyId, String id) {
        return new DdiItemNotFoundException(
                Code.DDI_STUDY_UNIT_NOT_FOUND, identity(agencyId, id), "Study unit not found: " + agencyId + "/" + id);
    }

    public static DdiItemNotFoundException physicalInstance(String agencyId, String id) {
        return new DdiItemNotFoundException(
                Code.DDI_PHYSICAL_INSTANCE_NOT_FOUND,
                identity(agencyId, id),
                "Physical instance not found: " + agencyId + "/" + id);
    }

    public Code code() {
        return code;
    }

    /** Identité de l'objet introuvable : {@code agencyId}, {@code id}. */
    public Map<String, String> params() {
        return params;
    }

    private static Map<String, String> identity(String agencyId, String id) {
        return Map.of("agencyId", String.valueOf(agencyId), "id", String.valueOf(id));
    }
}
