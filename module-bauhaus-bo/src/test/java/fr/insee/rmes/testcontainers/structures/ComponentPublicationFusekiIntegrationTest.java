package fr.insee.rmes.testcontainers.structures;

import fr.insee.rmes.graphdb.RdfBasicCredentials;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.testcontainers.FusekiContainer;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import org.junit.jupiter.api.Tag;

@Tag("integration")
class ComponentPublicationFusekiIntegrationTest extends WithFusekiContainer implements ComponentPublicationScenario {

    private final RepositoryUtils repositoryUtils = new RepositoryUtils(
            null,
            RepositoryInitiator.Type.DISABLED,
            "fuseki",
            new RdfBasicCredentials(FusekiContainer.ADMIN_USER, FusekiContainer.ADMIN_PASSWORD));

    @Override
    public RdfConnectionDetails connectionDetails() {
        return getRdfGestionConnectionDetails();
    }

    @Override
    public RepositoryUtils repositoryUtils() {
        return repositoryUtils;
    }
}
