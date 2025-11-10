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
}
