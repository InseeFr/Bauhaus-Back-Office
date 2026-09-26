package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

import java.util.Map;

/**
 * Le conteneur (Group ou StudyUnit) d'une PhysicalInstance n'expose pas le scheme sous lequel ranger
 * ses objets — ou, pour une StudyUnit, pas exactement un LogicalProduct pour porter ses variables.
 * Les LogicalProducts et leurs schemes sont créés en amont (init, miroir des opérations) : le save
 * n'en crée jamais à la volée.
 *
 * <p>Traduite en 409 {@code {message, code, params}} par le {@code DdiExceptionHandler} : le front
 * traduit le {@link Code} avec ses {@code params}, le {@code message} (en français) ne sert que de
 * repli et pour les logs.
 */
public class MissingSchemeException extends RuntimeException {

    /** Cas d'erreur, identifiants stables : le front en fait ses clés de traduction. */
    public enum Code {
        GROUP_MISSING_CODE_LIST_SCHEME,
        GROUP_MISSING_CATEGORY_SCHEME,
        GROUP_MISSING_MANAGED_REPRESENTATION_SCHEME,
        STUDY_UNIT_MISSING_LOGICAL_PRODUCT,
        STUDY_UNIT_SEVERAL_LOGICAL_PRODUCTS,
        STUDY_UNIT_MISSING_VARIABLE_SCHEME
    }

    private final Code code;
    private final Map<String, String> params;

    public MissingSchemeException(Code code, Map<String, String> params, String message) {
        super(message);
        this.code = code;
        this.params = params;
    }

    public Code code() {
        return code;
    }

    /** Valeurs à interpoler dans la traduction : {@code group}, {@code studyUnit}, {@code count}. */
    public Map<String, String> params() {
        return params;
    }
}
