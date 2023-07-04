package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;

public class RepositoryGestionStub extends RepositoryGestion {

    @Override
    public String getResponse(String query) throws RmesException {
        return "NON_EMPTY";
    }
}
