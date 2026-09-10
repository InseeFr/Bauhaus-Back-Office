package fr.insee.rmes.modules.organisations.domain;

/**
 * Formatting rules for organisation labels.
 */
public final class OrganisationLabel {

    private OrganisationLabel() {}

    /**
     * Appends the acronym in parentheses to the label when both are present
     * (e.g. {@code "Direction générale ... (DGAFP)"}). Falls back to the acronym
     * alone when the label is missing, or to the label when there is no acronym.
     *
     * @param label   the preferred label (may be {@code null}/blank)
     * @param acronym the {@code vaem:acronym} value (may be {@code null}/blank)
     * @return the label suffixed with the acronym, or {@code null} if both are absent
     */
    public static String withAcronym(String label, String acronym) {
        boolean hasLabel = label != null && !label.isBlank();
        boolean hasAcronym = acronym != null && !acronym.isBlank();
        if (hasLabel && hasAcronym) {
            return label + " (" + acronym + ")";
        }
        if (hasAcronym) {
            return acronym;
        }
        return label;
    }
}
