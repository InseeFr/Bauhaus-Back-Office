package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;

public class RepositoryPublicationStub extends RepositoryPublication {

    @Override
    public String getResponse(String query) throws RmesException {
        return "NON_EMPTY";
    }

    @Override
    public String getResponseInternalPublication(String query) throws RmesException {
        return "NON_EMPTY";
    }
}
