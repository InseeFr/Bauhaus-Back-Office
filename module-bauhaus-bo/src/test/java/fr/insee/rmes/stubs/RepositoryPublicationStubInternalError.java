package fr.insee.rmes.stubs;

<<<<<<< HEAD
import fr.insee.rmes.onion.domain.exceptions.RmesException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/stubs/RepositoryPublicationStubInternalError.java
=======
=======
import fr.insee.rmes.domain.exceptions.RmesException;
>>>>>>> 895fe5ae (refactor: migrate getFamily et getFamilies to the hexagonale architecture (#995))

>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/stubs/RepositoryPublicationStubInternalError.java
public class RepositoryPublicationStubInternalError extends RepositoryPublicationStub {

    public static final String ERROR_MESSAGE="error";
    private static final String DETAIL_ERROR_MESSAGE = "detail error";


    @Override
    public String getResponsePublication(String query) throws RmesException {
        throw new RmesException(500,ERROR_MESSAGE, DETAIL_ERROR_MESSAGE);
    }

}
