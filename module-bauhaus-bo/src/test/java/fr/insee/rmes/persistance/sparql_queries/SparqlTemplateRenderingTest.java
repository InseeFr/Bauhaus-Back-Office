package fr.insee.rmes.persistance.sparql_queries;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.insee.rmes.persistance.sparql_queries.SparqlTemplates.SparqlTemplate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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
 * Les paramètres factices sont des jetons SPARQL complets (voir {@link SparqlTemplates}) : un template
 * qui entoure encore son interpolation de quotes échoue donc ici, ce qui est le but.
 */
class SparqlTemplateRenderingTest {

    @TestFactory
    Stream<DynamicTest> everyTemplateRendersToAParsableQuery() throws Exception {
        return SparqlTemplates.queries().stream()
                .map(template ->
                        DynamicTest.dynamicTest(template.name(), () -> assertRendersToAParsableQuery(template)))
                .toList()
                .stream();
    }

    /**
     * Deux modules qui fourniraient le même nom de template se masqueraient l'un l'autre : le
     * template loader lit le classpath, et le premier trouvé gagne, silencieusement.
     */
    @Test
    void noTwoModulesProvideTheSameTemplate() throws Exception {
        Set<String> names = new LinkedHashSet<>();
        List<String> duplicates = SparqlTemplates.all().stream()
                .map(SparqlTemplate::name)
                .filter(name -> !names.add(name))
                .toList();

        assertTrue(duplicates.isEmpty(), () -> "Ces templates sont fournis par plusieurs modules : " + duplicates);
    }

    /**
     * Un template qui redéclare un format de sortie réactiverait l'échappement de FreeMarker et
     * mangerait les jetons produits par SparqlLiterals. Le format est fixé une fois pour toutes dans
     * FreemarkerConfig.
     */
    @Test
    void noTemplateOverridesTheOutputFormat() throws Exception {
        List<String> offenders = SparqlTemplates.all().stream()
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
        List<String> offenders = SparqlTemplates.all().stream()
                .filter(template -> !SparqlTemplates.PREFIXES_TEMPLATE.equals(template.fileName()))
                .filter(template -> template.source().contains("PREFIX "))
                .map(SparqlTemplate::name)
                .toList();

        assertTrue(
                offenders.isEmpty(),
                () -> "Ces templates redéclarent des préfixes déjà fournis par prefixes.ftlh : " + offenders);
    }

    private static void assertRendersToAParsableQuery(SparqlTemplate template) throws Exception {
        for (String query : SparqlTemplates.render(template)) {
            assertDoesNotThrow(
                    () -> {
                        if (SparqlTemplates.isUpdate(query)) {
                            QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, query, "http://example.org/");
                        } else {
                            QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, "http://example.org/");
                        }
                    },
                    () -> template.name() + " ne rend pas une requête SPARQL analysable :\n" + query);
        }
    }
}
