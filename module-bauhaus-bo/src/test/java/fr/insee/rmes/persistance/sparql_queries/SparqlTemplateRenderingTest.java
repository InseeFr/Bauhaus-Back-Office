package fr.insee.rmes.persistance.sparql_queries;

import fr.insee.rmes.freemarker.FreemarkerConfig;
import fr.insee.rmes.graphdb.QueryUtils;
import freemarker.template.Template;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rend chaque template de requête avec des paramètres factices et le soumet au parseur SPARQL.
 * <p>
 * C'est le filet qui verrouille la classe de bugs « requête syntaxiquement cassée en production » :
 * une requête qui ne parse plus échoue au build, pas à l'exécution.
 * <p>
 * Les valeurs factices imitent le contrat d'injection : ce que le Java passe à FreeMarker est un
 * <em>jeton SPARQL complet</em> produit par {@code SparqlLiterals} — d'où le {@code <…>} par défaut.
 * Un template qui entoure encore son interpolation de quotes échoue donc ici, ce qui est le but.
 */
class SparqlTemplateRenderingTest {

    private static final Path TEMPLATE_ROOT = Path.of("src/main/resources/request");

    /** Un jeton SPARQL valide partout où un terme est attendu (sujet, objet, argument de fonction). */
    private static final String TOKEN = "<http://example.org/x>";

    /** Interpolations attendues en position numérique (LIMIT, OFFSET, comparaison de compte). */
    private static final Set<String> NUMERIC = Set.of("PER_PAGE", "OFFSET", "NB_COMPONENT");

    /** Interpolations dont le jeton est une variable SPARQL complète ({@code ?code}). */
    private static final Set<String> VARIABLE_NAME = Set.of("SORT");

    /** Un littéral simple, pour les positions où seul un littéral est licite (avant {@code ^^}). */
    private static final String LITERAL_TOKEN = "\"x\"";

    /** {@code ${LG1}^^xsd:language} : une IRI ne peut pas porter de type, il faut un littéral. */
    private static final Pattern TYPED_LITERAL =
            Pattern.compile("\\$\\{\\s*([A-Za-z_][\\w.]*)[^}]*\\}\\s*\\^\\^");

    /** Les formes de mise à jour SPARQL relèvent de parseUpdate, pas de parseQuery. */
    private static final Pattern UPDATE_FORM =
            Pattern.compile("^\\s*(INSERT|DELETE|WITH|LOAD|CLEAR|DROP|CREATE|ADD|MOVE|COPY)\\b",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern INTERPOLATION = Pattern.compile("\\$\\{\\s*([A-Za-z_][\\w.]*)");
    private static final Pattern LIST_DIRECTIVE = Pattern.compile("<#list\\s+([A-Za-z_][\\w.]*)");
    private static final Pattern BOOLEAN_CONDITION = Pattern.compile("<#(?:if|elseif)\\s+!?\\s*([A-Za-z_][\\w.]*)\\s*>");
    private static final Pattern CONDITION_IDENTIFIER =
            Pattern.compile("<#(?:if|elseif)\\s+([^>]*)>");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][\\w.]*");
    private static final Pattern INCLUDE = Pattern.compile("<#include\\s+\"([^\"]+)\"");
    private static final Pattern LIST_ALIAS =
            Pattern.compile("<#list\\s+([A-Za-z_][\\w.]*)\\s+as\\s+([A-Za-z_]\\w*)");

    @TestFactory
    Stream<DynamicTest> everyTemplateRendersToAParsableQuery() throws Exception {
        Set<String> fragments = includedFragments();
        try (var files = Files.walk(TEMPLATE_ROOT)) {
            return files.filter(p -> p.toString().endsWith(".ftlh"))
                    .sorted()
                    .filter(p -> !fragments.contains(p.getFileName().toString()))
                    .map(path -> DynamicTest.dynamicTest(TEMPLATE_ROOT.relativize(path).toString(),
                            () -> assertRendersToAParsableQuery(path)))
                    .toList()
                    .stream();
        }
    }

    /**
     * Un fichier inclus par un autre n'est pas une requête autonome : il ne parse que dans son hôte.
     */
    private static Set<String> includedFragments() throws Exception {
        Set<String> fragments = new LinkedHashSet<>();
        try (var files = Files.walk(TEMPLATE_ROOT)) {
            files.filter(p -> p.toString().endsWith(".ftlh")).forEach(p -> {
                Matcher includes = INCLUDE.matcher(readString(p));
                while (includes.find()) {
                    fragments.add(Path.of(includes.group(1)).getFileName().toString());
                }
            });
        }
        return fragments;
    }

    /**
     * Un template qui redéclare un format de sortie réactiverait l'échappement de FreeMarker et
     * mangerait les jetons produits par SparqlLiterals. Le format est fixé une fois pour toutes dans
     * FreemarkerConfig.
     */
    @Test
    void noTemplateOverridesTheOutputFormat() throws Exception {
        try (var files = Files.walk(TEMPLATE_ROOT)) {
            List<Path> offenders = files.filter(p -> p.toString().endsWith(".ftlh"))
                    .filter(p -> readString(p).contains("output_format"))
                    .toList();

            assertTrue(offenders.isEmpty(), () -> "Ces templates redéclarent le format de sortie : " + offenders);
        }
    }

    private static void assertRendersToAParsableQuery(Path path) throws Exception {
        String source = readString(path);
        String name = TEMPLATE_ROOT.relativize(path).toString().replace('\\', '/');
        Template template = FreemarkerConfig.getCfg().getTemplate(name);

        for (Map<String, Object> parameters : dummyParameterSets(source)) {
            StringWriter out = new StringWriter();
            template.process(parameters, out);
            String query = out.toString();
            String withPrefixes = QueryUtils.PREFIXES + query;
            assertDoesNotThrow(
                    () -> {
                        if (UPDATE_FORM.matcher(query).find()) {
                            QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, withPrefixes, "http://example.org/");
                        } else {
                            QueryParserUtil.parseQuery(QueryLanguage.SPARQL, withPrefixes, "http://example.org/");
                        }
                    },
                    () -> name + " ne rend pas une requête SPARQL analysable :\n" + query);
        }
    }

    /**
     * Un jeu de paramètres par combinaison des booléens du template, pour que les deux branches de
     * chaque {@code <#if>} structurel soient rendues et analysées.
     */
    private static List<Map<String, Object>> dummyParameterSets(String source) {
        Set<String> lists = matches(LIST_DIRECTIVE, source);
        Set<String> booleans = matches(BOOLEAN_CONDITION, source);
        booleans.removeAll(lists);

        Set<String> scalars = new LinkedHashSet<>(matches(INTERPOLATION, source));
        scalars.addAll(conditionIdentifiers(source));
        scalars.removeAll(lists);
        scalars.removeAll(booleans);

        Set<String> typed = matches(TYPED_LITERAL, source);

        Map<String, Object> base = new HashMap<>();
        lists.forEach(name -> base.put(name, List.of(listElement(source, name), listElement(source, name))));
        scalars.forEach(name -> put(base, name, typed.contains(name) ? LITERAL_TOKEN : dummyScalar(name)));

        List<Map<String, Object>> sets = new ArrayList<>();
        int combinations = 1 << booleans.size();
        for (int mask = 0; mask < combinations; mask++) {
            Map<String, Object> parameters = new HashMap<>(base);
            int bit = 0;
            for (String name : booleans) {
                parameters.put(name, (mask & (1 << bit++)) != 0);
            }
            sets.add(parameters);
        }
        return sets;
    }

    /**
     * Un élément de liste peut être un scalaire ({@code ${s}}) ou un objet ({@code ${s.iri}}) quand la
     * même valeur doit être injectée sous deux formes.
     */
    private static Object listElement(String source, String listName) {
        Matcher aliases = LIST_ALIAS.matcher(source);
        while (aliases.find()) {
            if (!aliases.group(1).equals(listName)) {
                continue;
            }
            String alias = aliases.group(2);
            Matcher fields = Pattern.compile("\\$\\{\\s*" + Pattern.quote(alias) + "\\.(\\w+)").matcher(source);
            Map<String, Object> element = new HashMap<>();
            while (fields.find()) {
                element.put(fields.group(1), TOKEN);
            }
            if (!element.isEmpty()) {
                return element;
            }
        }
        return TOKEN;
    }

    private static String dummyScalar(String name) {
        String root = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name;
        if (NUMERIC.contains(root)) {
            return "10";
        }
        if (VARIABLE_NAME.contains(root)) {
            return "?x";
        }
        return TOKEN;
    }

    /** {@code ${DATE.rdfType}} exige que {@code DATE} soit une map portant la clé {@code rdfType}. */
    @SuppressWarnings("unchecked")
    private static void put(Map<String, Object> parameters, String name, String value) {
        int dot = name.indexOf('.');
        if (dot < 0) {
            parameters.put(name, value);
            return;
        }
        Map<String, Object> nested = (Map<String, Object>) parameters
                .computeIfAbsent(name.substring(0, dot), key -> new HashMap<String, Object>());
        nested.put(name.substring(dot + 1), value);
    }

    private static Set<String> conditionIdentifiers(String source) {
        Set<String> identifiers = new LinkedHashSet<>();
        Matcher conditions = CONDITION_IDENTIFIER.matcher(source);
        while (conditions.find()) {
            Matcher words = IDENTIFIER.matcher(conditions.group(1));
            while (words.find()) {
                identifiers.add(words.group());
            }
        }
        identifiers.removeIf(word -> Set.of("true", "false", "size", "gt", "lt").contains(word));
        return identifiers;
    }

    private static Set<String> matches(Pattern pattern, String source) {
        Set<String> found = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private static String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception e) {
            throw new IllegalStateException("Template illisible : " + path, e);
        }
    }
}
