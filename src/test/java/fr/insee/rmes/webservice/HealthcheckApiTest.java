package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.stubs.RepositoryGestionStub;
import fr.insee.rmes.stubs.RepositoryPublicationStub;
import fr.insee.rmes.stubs.RepositoryPublicationStubInternalError;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static fr.insee.rmes.webservice.HealthcheckApi.KO_STATE;
import static fr.insee.rmes.webservice.HealthcheckApi.OK_STATE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HealthcheckApiTest {

    private String documentsStoragePublicationInterne;
    private String documentsStorageGestion;
    private String documentsStoragePublicationExterne;

    @Test
    void checkDatabaseTest_success() {
        //Given
        RepositoryGestion repoGestionStub=new RepositoryGestionStub();
        RepositoryPublication repoPublicationStub=new RepositoryPublicationStub();
        var healthcheckApi=new HealthcheckApi(repoGestionStub, repoPublicationStub, null, documentsStoragePublicationInterne, documentsStoragePublicationExterne, documentsStorageGestion);
        StringJoiner errorMessage = new StringJoiner(" ");
        StringJoiner stateResult = new StringJoiner(" ");

        //When
        healthcheckApi.checkDatabase(errorMessage, stateResult);

        //Then
        assertThat(stateResult).hasToString(
                "Database connexion \n"
                +" "+" - Publication Z"+" "+OK_STATE
                +" "+" - Publication I"+" "+OK_STATE
                +" "+" - Gestion"+" "+OK_STATE

        );
        assertThat(errorMessage.toString()).isEmpty();
    }

    @Test
    void checkDatabaseTest_withInternalPublicationError() {
        //Given
        RepositoryGestion repoGestionStub=new RepositoryGestionStub();
        RepositoryPublication repoPublicationStub=new RepositoryPublicationStubInternalError();
        var healthcheckApi=new HealthcheckApi(repoGestionStub, repoPublicationStub, null, documentsStoragePublicationInterne, documentsStoragePublicationExterne, documentsStorageGestion);
        StringJoiner errorMessage = new StringJoiner(" ");
        StringJoiner stateResult = new StringJoiner(" ");

        //When
        healthcheckApi.checkDatabase(errorMessage, stateResult);

        //Then
        assertThat(stateResult).hasToString(
                "Database connexion \n"
                        +" "+" - Publication Z"+" "+OK_STATE
                        +" "+" - Publication I"+" "+KO_STATE
                        +" "+" - Gestion"+" "+OK_STATE

        );
        assertThat(errorMessage).hasToString("- Publication I "+null+ " \n");
    }


}