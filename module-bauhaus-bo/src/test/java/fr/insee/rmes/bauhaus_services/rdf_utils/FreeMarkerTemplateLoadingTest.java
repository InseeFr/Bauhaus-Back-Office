package fr.insee.rmes.bauhaus_services.rdf_utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Vérifie que les templates FreeMarker de <b>ce module</b> sont chargeables.
 * <p>
 * Ce n'est pas un doublon de {@code fr.insee.rmes.freemarker.FreeMarkerUtilsTest} (module-utility) :
 * {@code FreemarkerConfig} charge les templates via un {@code ClassTemplateLoader} sur {@code /request},
 * or seul module-bauhaus-bo embarque les templates de requêtes (module-utility n'y fournit que
 * {@code prefixes.ftlh}). Les tests de module-utility ne peuvent donc couvrir que les cas d'erreur ;
 * le cas passant se teste ici.
 */
class FreeMarkerTemplateLoadingTest {

    @Test
    void buildRequestTest() throws RmesException {
        assertThatCode(() -> FreeMarkerUtils.buildRequest("", "getAllGraphs.ftlh", Map.of()))
                .doesNotThrowAnyException();
        assertThat(withUnixLineSeparators(FreeMarkerUtils.buildRequest("", "getAllGraphs.ftlh", Map.of())))
                .contains("""
                        SELECT DISTINCT ?g
                        WHERE {
                          GRAPH ?g {?a ?b ?c }
                        }""");
    }

    /**
     * Le contenu rendu reprend les fins de ligne du fichier {@code .ftlh} tel qu'il est sur le disque,
     * or celles-ci dépendent de la configuration Git/IDE du poste (CRLF sous Windows). Les blocs de texte
     * Java, eux, sont toujours en LF : on normalise donc avant de comparer.
     */
    private static String withUnixLineSeparators(String request) {
        return request.replace("\r\n", "\n").replace("\r", "\n");
    }

    @Test
    void buildRequestTest_xdocreport() {
        // if not null, you should define a TemplateLoader for freemarker templates in xdocreport in FreemarkerConfig
        assertNull(FreeMarkerTemplateLoadingTest.class.getClassLoader().getResource("xdocreport"));
        assertNotNull(FreeMarkerTemplateLoadingTest.class.getClassLoader().getResource("request"));
    }
}
