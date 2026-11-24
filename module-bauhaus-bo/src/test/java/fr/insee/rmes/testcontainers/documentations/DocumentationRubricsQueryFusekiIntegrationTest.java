package fr.insee.rmes.testcontainers.documentations;

import fr.insee.rmes.graphdb.RdfBasicCredentials;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.FusekiContainer;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("integration")
class DocumentationRubricsQueryFusekiIntegrationTest extends WithFusekiContainer
        implements DocumentationRubricsQueryScenario {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(
                    null,
                    RepositoryInitiator.Type.DISABLED,
                    "fuseki",
                    new RdfBasicCredentials(FusekiContainer.ADMIN_USER, FusekiContainer.ADMIN_PASSWORD)));

    @BeforeAll
    static void initData() {
        fixtureLoader().withTrigFiles("sims-all.trig").withTrigFiles("sims-codes.trig");
    }

    @Override
    public RepositoryGestion repositoryGestion() {
        return repositoryGestion;
    }
}
