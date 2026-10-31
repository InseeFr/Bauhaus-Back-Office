package fr.insee.rmes.testcontainers.structures;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.Tag;

@Tag("integration")
class StructurePublicationGraphDBIntegrationTest extends WithGraphDBContainer implements StructurePublicationScenario {

    private final RepositoryUtils repositoryUtils = new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED);

    @Override
    public RdfConnectionDetails connectionDetails() {
        return getRdfGestionConnectionDetails();
    }

    @Override
    public RepositoryUtils repositoryUtils() {
        return repositoryUtils;
    }
}
