package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.indicators.series;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(IndicatorsQueries.checkPrefLabelUnicity("1", "Indice de prix des travaux d'entretien et d'amélioration de bâtiments", "fr"));
        assertTrue(result);
    }

    @Test
    void should_return_false_if_label_does_not_exist() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(IndicatorsQueries.checkPrefLabelUnicity("1", "label", "fr"));
        assertFalse(result);
    }
}
