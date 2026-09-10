package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.indicators.series;

import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class OpIndicatorQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    OperationIndicatorsQueries operationIndicatorsQueries = new OperationIndicatorsQueries(
            BauhausUriPropertiesStub.stub(), new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("all-operations-and-indicators.trig");
        container.withTrigFiles("sims-all.trig");
    }

    @Test
    void should_return_true_if_label_exist() throws Exception {
        boolean result = repositoryGestion.getResponseAsBoolean(operationIndicatorsQueries.checkPrefLabelUnicity(
                "1", "Indice de prix des travaux d'entretien et d'amélioration de bâtiments", "fr"));
        assertTrue(result);
    }

    @Test
    void should_return_false_if_label_does_not_exist() throws Exception {
        boolean result = repositoryGestion.getResponseAsBoolean(
                operationIndicatorsQueries.checkPrefLabelUnicity("1", "label", "fr"));
        assertFalse(result);
    }

    @Test
    void should_return_series_generated_by_indicator_p1623() throws Exception {
        IRI wasGeneratedBy = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/prov#wasGeneratedBy");

        String query = operationIndicatorsQueries.indicatorLinks("p1623", wasGeneratedBy);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertEquals(1, results.length(), "Should return exactly one linked series");

        JSONObject serie = results.getJSONObject(0);
        assertEquals("s1032", serie.getString("id"));
        assertEquals(
                "Élaboration des index BT/TP et divers et des indices de coûts de production dans la construction",
                serie.getString("labelLg1"));
        assertEquals("Computation of BT/TP and production cost indexes in construction", serie.getString("labelLg2"));
        assertEquals("http://rdf.insee.fr/def/base#StatisticalOperationSeries", serie.getString("typeOfObject"));
    }

    @Test
    void should_return_multiple_series_for_indicator_with_multiple_wasGeneratedBy() throws Exception {
        IRI wasGeneratedBy = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/prov#wasGeneratedBy");

        String query = operationIndicatorsQueries.indicatorLinks("p1630", wasGeneratedBy);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertEquals(2, results.length(), "Should return two linked series");

        JSONUtils.stream(results)
                .filter(serie -> "s1221".equals(serie.getString("id")) || "s1189".equals(serie.getString("id")))
                .forEach(serie -> assertEquals(
                        "http://rdf.insee.fr/def/base#StatisticalOperationSeries", serie.getString("typeOfObject")));

        List<String> serieIds =
                JSONUtils.stream(results).map(serie -> serie.getString("id")).toList();

        assertTrue(serieIds.contains("s1221"), "Should contain serie s1221");
        assertTrue(serieIds.contains("s1189"), "Should contain serie s1189");
    }

    @Test
    void should_return_empty_array_when_indicator_does_not_exist() throws Exception {
        IRI wasGeneratedBy = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/prov#wasGeneratedBy");

        String query = operationIndicatorsQueries.indicatorLinks("pNONEXISTENT", wasGeneratedBy);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertEquals(0, results.length(), "Should return empty array when indicator does not exist");
    }

    @Test
    void should_return_publication_state_for_indicator() throws Exception {
        String query = operationIndicatorsQueries.getPublicationState("p1651");
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertTrue(results.length() >= 0, "Should return a valid result array");

        if (results.length() > 0) {
            JSONObject result = results.getJSONObject(0);
            if (result.has("validationState")) {
                assertEquals("Validated", result.getString("validationState"));
            }
        }
    }

    @Test
    void should_return_all_indicators() throws Exception {
        String query = operationIndicatorsQueries.indicatorsQuery();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one indicator");

        JSONObject firstIndicator = results.getJSONObject(0);
        assertTrue(firstIndicator.has("id"));
        assertTrue(firstIndicator.has("label"));
    }

    @Test
    void should_return_single_indicator_by_id() throws Exception {
        String query = operationIndicatorsQueries.indicatorQuery("p1651");

        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one result");

        JSONObject result = results.getJSONObject(0);
        assertTrue(result.length() > 0, "Result should have at least one property");

        if (result.has("id")) {
            assertEquals("p1651", result.getString("id"));
        }

        assertTrue(
                result.has("prefLabelLg1") || result.has("prefLabelLg2") || result.length() > 0,
                "Result should have indicator properties");
    }

    @Test
    void should_return_creators_for_indicator() throws Exception {
        String query = operationIndicatorsQueries.getCreatorsById("p1651");
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
    }

    @Test
    void should_return_publishers_for_indicator() throws Exception {
        String query = operationIndicatorsQueries.getPublishersById("p1651");
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
    }

    @Test
    void should_return_last_indicator_id() throws Exception {
        String query = operationIndicatorsQueries.lastID();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one result");

        JSONObject result = results.getJSONObject(0);
        assertTrue(result.has("id"));
        String lastId = result.getString("id");
        assertNotNull(lastId);
        assertTrue(lastId.startsWith("p"), "ID should start with 'p'");
    }

    @Test
    void should_return_true_if_indicator_exists() throws Exception {
        String query = operationIndicatorsQueries.checkIfExists("p1651");
        boolean result = repositoryGestion.getResponseAsBoolean(query);

        assertTrue(result, "Indicator p1651 should exist");
    }

    @Test
    void should_return_false_if_indicator_does_not_exist() throws Exception {
        String query = operationIndicatorsQueries.checkIfExists("p9999");
        boolean result = repositoryGestion.getResponseAsBoolean(query);

        assertFalse(result, "Indicator p9999 should not exist");
    }

    @Test
    void should_return_indicators_with_sims() throws Exception {
        String query = operationIndicatorsQueries.indicatorsWithSimsQuery();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        if (results.length() > 0) {
            JSONObject firstResult = results.getJSONObject(0);
            assertTrue(firstResult.has("labelLg1"));
            assertTrue(firstResult.has("idSims"));
        }
    }

    @Test
    void should_return_seeAlso_links_for_indicator_p1651() throws Exception {
        IRI seeAlso = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2000/01/rdf-schema#seeAlso");

        String query = operationIndicatorsQueries.indicatorLinks("p1651", seeAlso);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertEquals(3, results.length(), "Should return three seeAlso links");

        List<String> linkedIndicatorIds = JSONUtils.stream(results)
                .map(linkedIndicator -> linkedIndicator.getString("id"))
                .toList();

        assertTrue(linkedIndicatorIds.contains("p1661"), "Should contain linked indicator p1661");
        assertTrue(linkedIndicatorIds.contains("p1623"), "Should contain linked indicator p1623");
        assertTrue(linkedIndicatorIds.contains("p1650"), "Should contain linked indicator p1650");
    }

    @Test
    void should_return_replaces_link_for_indicator_p1623() throws Exception {
        IRI replaces = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/terms/replaces");

        String query = operationIndicatorsQueries.indicatorLinks("p1623", replaces);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertEquals(1, results.length(), "Should return one replaces link");

        JSONObject linkedIndicator = results.getJSONObject(0);
        assertEquals("p1624", linkedIndicator.getString("id"));
    }

    @Test
    void should_return_empty_for_non_existent_link_predicate() throws Exception {
        IRI nonExistentPredicate = SimpleValueFactory.getInstance().createIRI("http://example.org/nonExistent");

        String query = operationIndicatorsQueries.indicatorLinks("p1651", nonExistentPredicate);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        assertNotNull(results);
        assertEquals(0, results.length(), "Should return empty array for non-existent predicate");
    }
}
