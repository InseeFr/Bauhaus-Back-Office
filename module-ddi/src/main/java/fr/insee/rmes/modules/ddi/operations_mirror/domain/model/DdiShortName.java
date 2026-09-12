package fr.insee.rmes.modules.ddi.operations_mirror.domain.model;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Le {@code Name} DDI d'un objet dérivé du nom court d'une opération : le nom court sans espace ni
 * signe diacritique, en majuscules, précédé d'un préfixe qui dit le rôle de l'objet — {@code LP-}
 * pour le LogicalProduct, {@code VS-} pour la VariableScheme.
 */
public final class DdiShortName {

    private DdiShortName() {}

    /**
     * @return le nom préfixé, ou {@code null} si l'opération ne porte pas de nom court : le DDI 3.3
     *     n'accepte pas d'élément {@code Name} vide.
     */
    public static String prefixed(String prefix, String shortLabel) {
        if (shortLabel == null || shortLabel.isBlank()) {
            return null;
        }
        return prefix + normalize(shortLabel);
    }

    private static String normalize(String shortLabel) {
        return Normalizer.normalize(shortLabel, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s", "")
                .toUpperCase(Locale.ROOT);
    }
}
