package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionServiceImpl;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;


public class DistributionServiceImplStubContainer extends DistributionServiceImpl {

    public DistributionServiceImplStubContainer(RdfConnectionDetails rdfGestionConectionDetails) {
        repoGestion = new RepositoryGestion(rdfGestionConectionDetails, new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    }
}
