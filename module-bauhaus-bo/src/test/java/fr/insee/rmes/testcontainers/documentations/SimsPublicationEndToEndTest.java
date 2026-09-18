package fr.insee.rmes.testcontainers.documentations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/**
 * Test E2E reproduisant le bug : insee:html de la rubrique S.3.1 (RICHTEXT)
 * est stocké dans le graphe rapport/null au lieu du graphe rapport/8888.
 *
 * Cause : addHtmlVersionForRichText() utilise RdfUtils.simsGraph(null) comme
 * contexte du graphe au lieu de RdfUtils.simsGraph(simsId).
 *
 * Ce test ÉCHOUE intentionnellement tant que le bug n'est pas corrigé.
 */
@Tag("integration")
class SimsPublicationEndToEndTest extends AbstractSimsPublicationEndToEndTest {

    @BeforeAll
    static void initData() {
        container.withTrigFiles("sims-s31-richtext-publication-test.trig");
    }

    @Test
    void validate_htmlS31DevraitEtreDansLeGrapheRapport8888() throws Exception, MissingUserInformationException {
        ResponseEntity<String> response = validateSimsAsAdmin("8888");

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // BUG : addHtmlVersionForRichText() utilise simsGraph(null) → graphe rapport/null
        // au lieu de simsGraph("8888") → graphe rapport/8888

        String queryGrapheCorrect = """
                SELECT ?html WHERE {
                    GRAPH <http://rdf.insee.fr/graphes/qualite/rapport/8888> {
                        ?textResource <http://rdf.insee.fr/def/base#html> ?html .
                    }
                }
                """;

        var dansGrapheCorrect = repositoryPublication.getResponseAsArray(queryGrapheCorrect);

        assertThat(dansGrapheCorrect)
                .as("insee:html de S.3.1 devrait être dans le graphe rapport/8888, pas dans rapport/null")
                .hasSizeGreaterThan(0);
    }
}
