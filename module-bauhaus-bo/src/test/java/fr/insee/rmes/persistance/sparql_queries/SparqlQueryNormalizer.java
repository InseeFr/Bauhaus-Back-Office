package fr.insee.rmes.persistance.sparql_queries;

import java.util.regex.Pattern;

/**
 * Met une requête SPARQL sous une forme comparable à un attendu écrit à la main.
 * <p>
 * Deux normalisations : les blancs sont uniformisés (l'indentation des templates n'a pas de sens
 * sémantique), et le prologue de préfixes est retiré. Ce prologue vient de
 * {@code request/prefixes.ftlh}, que FreeMarker insère en tête de chaque requête ; le vérifier ici
 * ne dirait rien du corps de la requête, et c'est déjà le rôle de {@code FreemarkerConfigTest}.
 */
public final class SparqlQueryNormalizer {

    private static final Pattern PREFIX_PROLOGUE = Pattern.compile("^(PREFIX\\s+\\S+\\s*<[^>]*>\\s*)+");

    private SparqlQueryNormalizer() {
        throw new IllegalStateException("Utility class");
    }

    public static String normalize(String sparql) {
        String singleSpaced = sparql.replaceAll("\\s+", " ").trim();
        return PREFIX_PROLOGUE.matcher(singleSpaced).replaceFirst("").trim();
    }
}
