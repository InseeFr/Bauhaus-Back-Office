package fr.insee.rmes.testcontainers.documentations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.DocumentationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Rubriques d'un rapport SIMS, rejouées sur chaque triplestore (voir
 * {@link DocumentationRubricsQueryGraphDBIntegrationTest} et {@link DocumentationRubricsQueryFusekiIntegrationTest}).
 *
 * <p>La rubrique S.18.2 du rapport 1679 (fréquence de diffusion) est une liste de codes : sur Fuseki,
 * son identifiant d'attribut sortait vide quand le calcul précédait le motif qui lie l'attribut, et
 * la lecture du rapport échouait.
 */
interface DocumentationRubricsQueryScenario {

    String LANGUAGES = "http://id.insee.fr/codes/langue/";

    RepositoryGestion repositoryGestion();

    @Test
    default void every_rubric_carries_its_attribute_identifier() throws RmesException {
        DocumentationQueries queries =
                new DocumentationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

        List<JSONObject> rubrics = JSONUtils.stream(repositoryGestion()
                        .getResponseAsArray(
                                queries.getDocumentationRubricsQuery("1679", LANGUAGES + "fr", LANGUAGES + "en")))
                .toList();

        assertThat(rubrics)
                .isNotEmpty()
                .allSatisfy(rubric -> assertThat(rubric.has("idAttribute"))
                        .as("idAttribute de %s", rubric)
                        .isTrue());
        assertThat(rubrics)
                .filteredOn(rubric -> "S.18.2".equals(rubric.optString("idAttribute")))
                .extracting(rubric -> rubric.optString("value"))
                .containsExactly("M");
    }

    /**
     * Le rapport 1679 documente l'opération s1327 : sur Fuseki, chaque identifiant de cible sortait non
     * lié, calculé dans un {@code OPTIONAL} qui ne voit pas la cible, et la lecture des timbres
     * propriétaires du rapport échouait.
     */
    @Test
    default void the_target_of_a_report_is_read() throws RmesException {
        DocumentationQueries queries =
                new DocumentationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

        JSONObject target = repositoryGestion().getResponseAsObject(queries.getTargetByIdSims("1679"));

        assertThat(target.optString("idOperation")).isEqualTo("s1327");
        assertThat(target.has("idSeries")).isTrue();
        assertThat(target.has("idIndicator")).isTrue();
    }
}
