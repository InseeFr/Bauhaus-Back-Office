package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.indicators;

import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("integration")
class IndicatorQueryGraphDBIntegrationTest extends WithGraphDBContainer implements IndicatorQueryScenario {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData() {
        fixtureLoader().withTrigFiles("all-operations-and-indicators.trig");
    }

    @Override
    public RepositoryGestion repositoryGestion() {
        return repositoryGestion;
    }
}
