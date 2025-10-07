package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;

public class RepositoryGestionStub extends RepositoryGestion {

    public RepositoryGestionStub() {
        super(null, null);
    }

    @Override
    public String getResponse(String query) {
        return "NON_EMPTY";
    }
}
