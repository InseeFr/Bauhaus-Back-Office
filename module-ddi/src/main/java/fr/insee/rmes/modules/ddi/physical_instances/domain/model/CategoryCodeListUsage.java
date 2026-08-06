package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * A usage of a Category: a CodeList whose codes reference the category, joined to one Variable
 * that uses this code list, with the Variable's PhysicalInstance, StudyUnit and Group parents.
 *
 * <p>One flat row per (CodeList, Variable) pair; the frontend groups them into a
 * Group / StudyUnit / PhysicalInstance / Variable / CodeList tree. Each level is a
 * {@link UsageItem} carrying its resolved label, and is {@code null} when the code list is used by
 * no variable or when that parent cannot be resolved — only {@link #codeList()} is always present.
 */
public record CategoryCodeListUsage(
        UsageItem group,
        UsageItem studyUnit,
        UsageItem physicalInstance,
        UsageItem variable,
        UsageItem codeList
) {

    /** Une liste qu'aucune variable n'utilise : seule la liste elle-même est connue. */
    public static CategoryCodeListUsage ofCodeListAlone(UsageItem codeList) {
        return new CategoryCodeListUsage(null, null, null, null, codeList);
    }
}
