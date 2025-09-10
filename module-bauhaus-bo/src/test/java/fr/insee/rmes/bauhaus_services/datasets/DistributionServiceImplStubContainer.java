package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionServiceImpl;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfConnectionDetails;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;


public class DistributionServiceImplStubContainer extends DistributionServiceImpl {

    public DistributionServiceImplStubContainer(RdfConnectionDetails rdfGestionConectionDetails) {
        repoGestion = new RepositoryGestion(rdfGestionConectionDetails, new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    }
}
