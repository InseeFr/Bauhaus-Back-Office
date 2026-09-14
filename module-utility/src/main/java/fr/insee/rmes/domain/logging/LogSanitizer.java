package fr.insee.rmes.domain.logging;

/**
 * Neutralise les données contrôlées par l'utilisateur (path variables, corps de requête…)
 * avant de les écrire dans les logs.
 * <p>
 * Sans cela, une valeur contenant un retour à la ligne permet de forger une fausse entrée de
 * journal (log injection / log forging, cf. règle Sonar S5145). Tout caractère de contrôle est
 * remplacé par {@code _} ; les espaces et le reste du texte sont conservés pour que le log
 * garde sa valeur de diagnostic.
 */
public final class LogSanitizer {

    private static final String CONTROL_CHARACTERS = "[\\p{Cntrl}\\u0085\\u2028\\u2029]";

    private static final String REPLACEMENT = "_";

    private LogSanitizer() {}

    /**
     * @param value valeur potentiellement contrôlée par l'utilisateur
     * @return la même valeur, privée de ses caractères de contrôle ({@code null} reste {@code null})
     */
    public static String forLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(CONTROL_CHARACTERS, REPLACEMENT);
    }
}
