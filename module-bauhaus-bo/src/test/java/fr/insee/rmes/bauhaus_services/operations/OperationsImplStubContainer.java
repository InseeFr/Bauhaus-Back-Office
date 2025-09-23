package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;

public class OperationsImplStubContainer extends OperationsImpl {

    public OperationsImplStubContainer(RdfConnectionDetails rdfGestionConectionDetails) {
        repoGestion=new RepositoryGestion(rdfGestionConectionDetails, new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    }
}
