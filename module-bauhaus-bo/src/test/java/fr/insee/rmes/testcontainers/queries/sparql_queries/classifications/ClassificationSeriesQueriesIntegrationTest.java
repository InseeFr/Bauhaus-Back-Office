package fr.insee.rmes.testcontainers.queries.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Non-régression des requêtes de séries de nomenclatures après leur migration vers
 * {@code classifications/series/getOneSeries.ftlh} et {@code getSeriesMembers.ftlh}.
 *
 * <p>{@code getOneSeries.ftlh} est la plus riche des requêtes migrées : sous-requêtes
 * {@code group_concat} pour les altLabels, remarques traduites via
 * {@code dcterms:language}/{@code evoc:noteLiteral}, et rattachement à la famille. Seule
 * une exécution réelle prouve que ces morceaux fonctionnent encore ensemble.
 */
@Tag("integration")
class ClassificationSeriesQueriesIntegrationTest extends WithGraphDBContainer {

    private static final String SERIES_ID = "serMig";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final ClassificationSeriesQueries queries = new ClassificationSeriesQueries(
            new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("classifications-ftlh-migration-it.trig");
    }

    @Test
    void oneSeriesQuery_returns_labels_altlabels_metadata_and_parent_family() throws RmesException {
        JSONObject series = repositoryGestion.getResponseAsObject(queries.oneSeriesQuery(SERIES_ID));

        assertThat(series.getString("id")).isEqualTo(SERIES_ID);
        assertThat(series.getString("prefLabelLg1")).isEqualTo("Serie migration (test)");
        assertThat(series.getString("prefLabelLg2")).isEqualTo("Migration series (test)");

        assertThat(series.getString("altLabelLg1"))
                .as("altLabel lg1 agrégé par la sous-requête group_concat")
                .isEqualTo("SerMig court (test)");
        assertThat(series.getString("altLabelLg2")).isEqualTo("SerMig short (test)");

        assertThat(series.getString("subject")).isEqualTo("Sujet migration (test)");
        assertThat(series.getString("publishers")).isEqualTo("Insee migration (test)");
        assertThat(series.getString("covers")).isEqualTo("Champ migration (test)");

        assertThat(series.getString("scopeNoteLg1"))
                .as("remarque lg1 lue via dcterms:language + evoc:noteLiteral")
                .isEqualTo("Remarque migration (fr)");
        assertThat(series.getString("scopeNoteLg2")).isEqualTo("Remarque migration (en)");

        assertThat(series.getString("familyLg1")).isEqualTo("Famille migration (test)");
        assertThat(series.getString("familyLg2")).isEqualTo("Migration family (test)");
        assertThat(series.getString("idFamily"))
                .as("idFamily est extrait de l'IRI par BIND(STRAFTER(...,'/codes/familleDeNomenclatures/'))")
                .isEqualTo("famMig");
    }

    @Test
    void oneSeriesQuery_returns_nothing_for_an_unknown_series() throws RmesException {
        JSONObject series = repositoryGestion.getResponseAsObject(queries.oneSeriesQuery("serInconnue"));

        assertThat(series.isEmpty()).isTrue();
    }

    @Test
    void seriesMembersQuery_returns_the_classifications_attached_to_the_series() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(queries.seriesMembersQuery(SERIES_ID));

        assertThat(result.length()).isEqualTo(1);
        JSONObject classification = result.getJSONObject(0);
        assertThat(classification.getString("id"))
                .as("l'id est extrait par BIND(STRBEFORE(STRAFTER(...,'/codes/'), '/'))")
                .isEqualTo("clsMig");
        assertThat(classification.getString("labelLg1")).isEqualTo("Nomenclature migration (test)");
        assertThat(classification.getString("labelLg2")).isEqualTo("Migration classification (test)");
    }

    @Test
    void seriesMembersQuery_returns_nothing_for_an_unknown_series() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(queries.seriesMembersQuery("serInconnue"));

        assertThat(result.length()).isZero();
    }
}
