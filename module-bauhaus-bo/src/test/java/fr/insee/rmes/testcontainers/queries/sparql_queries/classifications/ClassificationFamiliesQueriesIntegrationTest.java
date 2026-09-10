package fr.insee.rmes.testcontainers.queries.sparql_queries.classifications;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationFamiliesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Non-régression des requêtes de familles de nomenclatures après leur migration de la
 * concaténation Java vers les templates FreeMarker {@code classifications/families/*.ftlh}.
 *
 * <p>Les tests unitaires figent le texte SPARQL produit ; ceux-ci vérifient que ce texte
 * s'exécute réellement contre un triplestore et ramène les bonnes lignes — notamment les
 * {@code BIND}/{@code FILTER REGEX} sur la forme des IRIs, qu'une simple comparaison de
 * chaînes ne peut pas valider.
 */
@Tag("integration")
class ClassificationFamiliesQueriesIntegrationTest extends WithGraphDBContainer {

    private static final String FAMILY_ID = "famMig";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final ClassificationFamiliesQueries queries =
            new ClassificationFamiliesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("classifications-ftlh-migration-it.trig");
    }

    @Test
    void familyQuery_returns_the_french_label_of_the_seeded_family() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(queries.familyQuery(FAMILY_ID));

        assertThat(result.getString("prefLabelLg1")).isEqualTo("Famille migration (test)");
    }

    @Test
    void familyQuery_returns_nothing_for_an_unknown_family() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(queries.familyQuery("famInconnue"));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void familyMembersQuery_returns_the_series_attached_to_the_family() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(queries.familyMembersQuery(FAMILY_ID));

        assertThat(result.length()).isEqualTo(1);
        JSONObject series = result.getJSONObject(0);
        assertThat(series.getString("id"))
                .as("l'id est extrait de l'IRI par BIND(STRAFTER(...,'/codes/serieDeNomenclatures/'))")
                .isEqualTo("serMig");
        assertThat(series.getString("labelLg1")).isEqualTo("Serie migration (test)");
        assertThat(series.getString("labelLg2")).isEqualTo("Migration series (test)");
    }

    @Test
    void familyMembersQuery_returns_nothing_for_an_unknown_family() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(queries.familyMembersQuery("famInconnue"));

        assertThat(result.length()).isZero();
    }
}
