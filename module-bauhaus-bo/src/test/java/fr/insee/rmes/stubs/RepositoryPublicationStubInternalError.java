package fr.insee.rmes.stubs;

import fr.insee.rmes.domain.exceptions.RmesException;

public class RepositoryPublicationStubInternalError extends RepositoryPublicationStub {

    public static final String ERROR_MESSAGE="error";
    private static final String DETAIL_ERROR_MESSAGE = "detail error";


    @Override
    public String getResponsePublication(String query) throws RmesException {
        throw new RmesException(500,ERROR_MESSAGE, DETAIL_ERROR_MESSAGE);
    }

}
