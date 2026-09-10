package fr.insee.rmes.modules.operations.families.domain.exceptions;

import fr.insee.rmes.modules.shared_kernel.domain.model.Language;

/**
 * Deux familles ne peuvent pas porter le même libellé dans une même langue. La langue fautive est
 * portée par l'exception : le front affiche un message traduit différent selon qu'il s'agit du
 * libellé principal ou du secondaire.
 */
public class FamilyPrefLabelAlreadyUsedException extends Exception {

    private final Language language;

    public FamilyPrefLabelAlreadyUsedException(Language language) {
        super("This prefLabel%s is already used by another family."
                .formatted(language == Language.lg1 ? "Lg1" : "Lg2"));
        this.language = language;
    }

    public Language language() {
        return language;
    }
}
