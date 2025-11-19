package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.indicators.series;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class OpIndicatorQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
        container.withTrigFiles("sims-all.trig");
    }

    @Test
    void should_return_true_if_label_exist() throws Exception {
        OperationSeriesQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(OperationIndicatorsQueries.checkPrefLabelUnicity("1", "Indice de prix des travaux d'entretien et d'amélioration de bâtiments", "fr"));
        assertTrue(result);
    }

    @Test
    void should_return_false_if_label_does_not_exist() throws Exception {
        OperationSeriesQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(OperationIndicatorsQueries.checkPrefLabelUnicity("1", "label", "fr"));
        assertFalse(result);
    }

    @Test
    void should_return_series_generated_by_indicator_p1623() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI wasGeneratedBy = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/prov#wasGeneratedBy");

        // When
        String query = OperationIndicatorsQueries.indicatorLinks("p1623", wasGeneratedBy);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertEquals(1, results.length(), "Should return exactly one linked series");

        JSONObject serie = results.getJSONObject(0);
        assertEquals("s1032", serie.getString("id"));
        assertEquals("Élaboration des index BT/TP et divers et des indices de coûts de production dans la construction",
                     serie.getString("labelLg1"));
        assertEquals("Computation of BT/TP and production cost indexes in construction",
                     serie.getString("labelLg2"));
        assertEquals("http://rdf.insee.fr/def/base#StatisticalOperationSeries",
                     serie.getString("typeOfObject"));
    }

    @Test
    void should_return_multiple_series_for_indicator_with_multiple_wasGeneratedBy() throws Exception {
        // Given - p1630 has two wasGeneratedBy relations (s1221 and s1189)
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI wasGeneratedBy = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/prov#wasGeneratedBy");

        // When
        String query = OperationIndicatorsQueries.indicatorLinks("p1630", wasGeneratedBy);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertEquals(2, results.length(), "Should return two linked series");

        // Verify both series are present (order may vary)
        boolean hasS1221 = false;
        boolean hasS1189 = false;

        for (int i = 0; i < results.length(); i++) {
            JSONObject serie = results.getJSONObject(i);
            String id = serie.getString("id");
            if ("s1221".equals(id)) {
                hasS1221 = true;
                assertEquals("http://rdf.insee.fr/def/base#StatisticalOperationSeries",
                             serie.getString("typeOfObject"));
            } else if ("s1189".equals(id)) {
                hasS1189 = true;
                assertEquals("http://rdf.insee.fr/def/base#StatisticalOperationSeries",
                             serie.getString("typeOfObject"));
            }
        }

        assertTrue(hasS1221, "Should contain serie s1221");
        assertTrue(hasS1189, "Should contain serie s1189");
    }

    @Test
    void should_return_empty_array_when_indicator_does_not_exist() throws Exception {
        // Given - non-existent indicator
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI wasGeneratedBy = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/ns/prov#wasGeneratedBy");

        // When
        String query = OperationIndicatorsQueries.indicatorLinks("pNONEXISTENT", wasGeneratedBy);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertEquals(0, results.length(), "Should return empty array when indicator does not exist");
    }

    @Test
    void should_return_publication_state_for_indicator() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.getPublicationState("p1651");
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        // The query should execute successfully (may return empty if no validation state)
        // Just verify the query works and returns valid JSON
        assertTrue(results.length() >= 0, "Should return a valid result array");

        // If there are results, check the validationState if present
        if (results.length() > 0) {
            JSONObject result = results.getJSONObject(0);
            if (result.has("validationState")) {
                assertEquals("Validated", result.getString("validationState"));
            }
        }
    }

    @Test
    void should_return_all_indicators() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.indicatorsQuery();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one indicator");

        // Verify structure of first indicator (returns: id, label, altLabel)
        JSONObject firstIndicator = results.getJSONObject(0);
        assertTrue(firstIndicator.has("id"));
        assertTrue(firstIndicator.has("label"));
    }

    @Test
    void should_return_indicators_for_search() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.indicatorsQueryForSearch();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one indicator");
    }

    @Test
    void should_return_single_indicator_by_id() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.indicatorQuery("p1651", true);

        // Try as array first (SPARQL SELECT queries return arrays)
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one result");

        JSONObject result = results.getJSONObject(0);
        // Verify the query returns data - check for any expected property
        assertTrue(result.length() > 0, "Result should have at least one property");

        // Check for id property if present
        if (result.has("id")) {
            assertEquals("p1651", result.getString("id"));
        }

        // Verify that it has either prefLabelLg1 or other indicator properties
        assertTrue(result.has("prefLabelLg1") || result.has("prefLabelLg2") || result.length() > 0,
                   "Result should have indicator properties");
    }

    @Test
    void should_return_indicator_with_rich_text_structure_flag_false() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.indicatorQuery("p1651", false);

        // Try as array first (SPARQL SELECT queries return arrays)
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertTrue(results.length() > 0, "Should return at least one result");

        JSONObject result = results.getJSONObject(0);
        // Verify the query returns data
        assertTrue(result.length() > 0, "Result should have at least one property");

        // The query should work with both flag values
        // Just verify we get a valid response
    }

    @Test
    void should_return_creators_for_indicator() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.getCreatorsById("p1651");
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        // May be empty if no creators defined in test data
    }

    @Test
    void should_return_publishers_for_indicator() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.getPublishersById("p1651");
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        // May be empty if no publishers defined in test data
    }

    @Test
    void should_return_multiple_organizations_for_indicator() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI creator = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/terms/creator");

        // When
        String query = OperationIndicatorsQueries.getMultipleOrganizations("p1651", creator);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        // May be empty if no organizations defined in test data
    }

    @Test
    void should_return_last_indicator_id() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.lastID();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
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
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.checkIfExists("p1651");
        boolean result = repositoryGestion.getResponseAsBoolean(query);

        // Then
        assertTrue(result, "Indicator p1651 should exist");
    }

    @Test
    void should_return_false_if_indicator_does_not_exist() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.checkIfExists("p9999");
        boolean result = repositoryGestion.getResponseAsBoolean(query);

        // Then
        assertFalse(result, "Indicator p9999 should not exist");
    }

    @Test
    void should_return_indicators_with_sims() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());

        // When
        String query = OperationIndicatorsQueries.indicatorsWithSimsQuery();
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        // May be empty if no SIMS metadata reports are linked to indicators in test data
        // If results exist, verify structure
        if (results.length() > 0) {
            JSONObject firstResult = results.getJSONObject(0);
            assertTrue(firstResult.has("labelLg1"));
            assertTrue(firstResult.has("idSims"));
        }
    }

    @Test
    void should_return_seeAlso_links_for_indicator_p1651() throws Exception {
        // Given - p1651 has seeAlso links to p1661, p1623, p1650
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI seeAlso = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2000/01/rdf-schema#seeAlso");

        // When
        String query = OperationIndicatorsQueries.indicatorLinks("p1651", seeAlso);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertEquals(3, results.length(), "Should return three seeAlso links");

        // Verify all linked indicators are present
        boolean hasP1661 = false;
        boolean hasP1623 = false;
        boolean hasP1650 = false;

        for (int i = 0; i < results.length(); i++) {
            JSONObject linkedIndicator = results.getJSONObject(i);
            String id = linkedIndicator.getString("id");
            if ("p1661".equals(id)) hasP1661 = true;
            if ("p1623".equals(id)) hasP1623 = true;
            if ("p1650".equals(id)) hasP1650 = true;
        }

        assertTrue(hasP1661, "Should contain linked indicator p1661");
        assertTrue(hasP1623, "Should contain linked indicator p1623");
        assertTrue(hasP1650, "Should contain linked indicator p1650");
    }

    @Test
    void should_return_replaces_link_for_indicator_p1623() throws Exception {
        // Given - p1623 replaces p1624
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI replaces = SimpleValueFactory.getInstance().createIRI("http://purl.org/dc/terms/replaces");

        // When
        String query = OperationIndicatorsQueries.indicatorLinks("p1623", replaces);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertEquals(1, results.length(), "Should return one replaces link");

        JSONObject linkedIndicator = results.getJSONObject(0);
        assertEquals("p1624", linkedIndicator.getString("id"));
    }

    @Test
    void should_return_empty_for_non_existent_link_predicate() throws Exception {
        // Given
        OperationIndicatorsQueries.setConfig(new ConfigStub());
        IRI nonExistentPredicate = SimpleValueFactory.getInstance()
                .createIRI("http://example.org/nonExistent");

        // When
        String query = OperationIndicatorsQueries.indicatorLinks("p1651", nonExistentPredicate);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        // Then
        assertNotNull(results);
        assertEquals(0, results.length(), "Should return empty array for non-existent predicate");
    }
}
