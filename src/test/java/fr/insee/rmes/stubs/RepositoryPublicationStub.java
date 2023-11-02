package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.repository.Repository;

public class RepositoryPublicationStub extends RepositoryPublication {

    public RepositoryPublicationStub(){
        super(null, null, null,null,new RepositoryUtilsStub(){

        });
    }

    @Override
    public String getResponse(String query) {
        return "NON_EMPTY";
    }

    @Override
    public String getResponseInternalPublication(String query) throws RmesException {
        return "NON_EMPTY";
    }

    private static class RepositoryUtilsStub extends RepositoryUtils {
        public RepositoryUtilsStub() {
            super(null, RepositoryInitiator.Type.DISABLED);
        }

        @Override
        public Repository initRepository(String rdfServer, String repositoryID) {
            return null;
        }
    }
}
