package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfConnectionDetails;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;

public class OperationsImplStubContainer extends OperationsImpl {

    public OperationsImplStubContainer(RdfConnectionDetails rdfGestionConectionDetails) {
        repoGestion=new RepositoryGestion(rdfGestionConectionDetails, new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    }
}
