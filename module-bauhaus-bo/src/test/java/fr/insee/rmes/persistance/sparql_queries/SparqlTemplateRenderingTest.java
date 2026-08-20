package fr.insee.rmes.persistance.sparql_queries;

import fr.insee.rmes.freemarker.FreemarkerConfig;
import freemarker.template.Template;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
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
 * Le périmètre est le réacteur, pas ce module : les templates de module-ddi et de module-operation
 * sont couverts au même titre que ceux de module-bauhaus-bo, et tout module qui en ajoutera le sera
 * aussi, sans rien à câbler ici.
 * <p>
 * C'est le filet qui verrouille la classe de bugs « requête syntaxiquement cassée en production » :
 * une requête qui ne parse plus échoue au build, pas à l'exécution.
 * <p>
 * Les valeurs factices imitent le contrat d'injection : ce que le Java passe à FreeMarker est un
 * <em>jeton SPARQL complet</em> produit par {@code SparqlLiterals} — d'où le {@code <…>} par défaut.
 * Un template qui entoure encore son interpolation de quotes échoue donc ici, ce qui est le but.
 */
class SparqlTemplateRenderingTest {

    /** Chemin des templates dans un module ; c'est aussi la racine des noms FreeMarker. */
    private static final Path TEMPLATE_ROOT = Path.of("src/main/resources/request");

    /** Le réacteur, vu depuis le répertoire du module où surefire place le répertoire courant. */
    private static final Path REACTOR_ROOT = Path.of("..");

    /** Le prologue de préfixes inséré par auto-include : seul, ce n'est pas une requête. */
    private static final String PREFIXES_TEMPLATE = "prefixes.ftlh";

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

    /** Un template : son nom FreeMarker (relatif à {@code request/}), sa source et son fichier. */
    private record SparqlTemplate(String name, String source, Path path) {
        String fileName() {
            return path.getFileName().toString();
        }
    }

    @TestFactory
    Stream<DynamicTest> everyTemplateRendersToAParsableQuery() throws Exception {
        List<SparqlTemplate> templates = templates();
        Set<String> fragments = includedFragments(templates);

        return templates.stream()
                .filter(template -> !fragments.contains(template.fileName()))
                .map(template -> DynamicTest.dynamicTest(template.name(),
                        () -> assertRendersToAParsableQuery(template)))
                .toList()
                .stream();
    }

    /**
     * Les templates de <em>tous</em> les modules du réacteur, pas seulement ceux d'ici : module-ddi et
     * module-operation en embarquent aussi, et un module qui en ajoutera sera couvert sans rien câbler.
     * <p>
     * La découverte part des sources et non du classpath : un {@code target/} non nettoyé garde les
     * templates supprimés par les commits précédents, qui repasseraient alors au parseur. Le rendu,
     * lui, passe par le template loader — un template qu'aucune dépendance de ce module n'expose
     * échoue donc ici, ce qui est le but.
     */
    private static List<SparqlTemplate> templates() throws IOException {
        if (!Files.isDirectory(TEMPLATE_ROOT)) {
            throw new IllegalStateException("Ce test doit tourner depuis le répertoire du module, or le "
                    + "répertoire courant est " + Path.of("").toAbsolutePath());
        }
        List<SparqlTemplate> templates = new ArrayList<>();
        for (Path root : templateRoots()) {
            try (var files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".ftlh"))
                        .forEach(path -> templates.add(new SparqlTemplate(
                                root.relativize(path).toString().replace('\\', '/'), readString(path), path)));
            }
        }
        templates.removeIf(template -> PREFIXES_TEMPLATE.equals(template.fileName()));
        templates.sort(Comparator.comparing(SparqlTemplate::name));
        return templates;
    }

    private static List<Path> templateRoots() throws IOException {
        try (var modules = Files.list(REACTOR_ROOT)) {
            return modules.map(module -> module.resolve(TEMPLATE_ROOT))
                    .filter(Files::isDirectory)
                    .sorted()
                    .toList();
        }
    }

    /**
     * Deux modules qui fourniraient le même nom de template se masqueraient l'un l'autre : le
     * template loader lit le classpath, et le premier trouvé gagne, silencieusement.
     */
    @Test
    void noTwoModulesProvideTheSameTemplate() throws Exception {
        Set<String> names = new LinkedHashSet<>();
        List<String> duplicates = templates().stream()
                .map(SparqlTemplate::name)
                .filter(name -> !names.add(name))
                .toList();

        assertTrue(duplicates.isEmpty(), () -> "Ces templates sont fournis par plusieurs modules : " + duplicates);
    }

    /**
     * Un fichier inclus par un autre n'est pas une requête autonome : il ne parse que dans son hôte.
     */
    private static Set<String> includedFragments(List<SparqlTemplate> templates) {
        Set<String> fragments = new LinkedHashSet<>();
        for (SparqlTemplate template : templates) {
            Matcher includes = INCLUDE.matcher(template.source());
            while (includes.find()) {
                fragments.add(Path.of(includes.group(1)).getFileName().toString());
            }
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
        List<String> offenders = templates().stream()
                .filter(template -> template.source().contains("output_format"))
                .map(SparqlTemplate::name)
                .toList();

        assertTrue(offenders.isEmpty(), () -> "Ces templates redéclarent le format de sortie : " + offenders);
    }

    /**
     * Les préfixes sont déclarés une seule fois, dans {@code request/prefixes.ftlh} (module-utility),
     * que FreeMarker insère en tête de chaque template (auto-include). Un template qui les redéclare
     * les dupliquerait dans la requête et ferait diverger les deux listes.
     */
    @Test
    void noTemplateRedeclaresThePrefixes() throws Exception {
        List<String> offenders = templates().stream()
                .filter(template -> !PREFIXES_TEMPLATE.equals(template.fileName()))
                .filter(template -> template.source().contains("PREFIX "))
                .map(SparqlTemplate::name)
                .toList();

        assertTrue(offenders.isEmpty(),
                () -> "Ces templates redéclarent des préfixes déjà fournis par prefixes.ftlh : " + offenders);
    }

    private static void assertRendersToAParsableQuery(SparqlTemplate sparqlTemplate) throws Exception {
        String name = sparqlTemplate.name();
        Template template = FreemarkerConfig.getCfg().getTemplate(name);

        for (Map<String, Object> parameters : dummyParameterSets(sparqlTemplate.source())) {
            StringWriter out = new StringWriter();
            template.process(parameters, out);
            String query = out.toString();
            assertDoesNotThrow(
                    () -> {
                        if (UPDATE_FORM.matcher(query).find()) {
                            QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, query, "http://example.org/");
                        } else {
                            QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, "http://example.org/");
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
        } catch (IOException e) {
            throw new UncheckedIOException("Template illisible : " + path, e);
        }
    }
}
