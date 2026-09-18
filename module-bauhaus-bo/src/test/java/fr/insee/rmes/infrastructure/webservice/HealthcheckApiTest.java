package fr.insee.rmes.infrastructure.webservice;

import static fr.insee.rmes.modules.commons.webservice.HealthcheckResources.KO_STATE;
import static fr.insee.rmes.modules.commons.webservice.HealthcheckResources.OK_STATE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.modules.commons.webservice.HealthcheckResources;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.stubs.RepositoryGestionStub;
import fr.insee.rmes.stubs.RepositoryPublicationStub;
import fr.insee.rmes.stubs.RepositoryPublicationStubInternalError;
import java.util.StringJoiner;
import org.junit.jupiter.api.Test;

class HealthcheckApiTest {

    private String documentsStoragePublicationInterne;
    private String documentsStorageGestion;
    private String documentsStoragePublicationExterne;

    private final StringJoiner errorMessage = new StringJoiner(" ");
    private final StringJoiner stateResult = new StringJoiner(" ");

    @Test
    void checkDatabaseTest_success() {
        // Given
        RepositoryPublication repoPublicationStub = new RepositoryPublicationStub();

        // When
        checkDatabaseWith(repoPublicationStub);

        // Then
        assertThat(stateResult)
                .hasToString("Database connexion \n"
                        + " " + " - Publication Z" + " " + OK_STATE
                        + " " + " - Publication I" + " " + OK_STATE
                        + " " + " - Gestion" + " " + OK_STATE);
        assertThat(errorMessage.toString()).isEmpty();
    }

    @Test
    void checkDatabaseTest_withInternalPublicationError() {
        // Given
        RepositoryPublication repoPublicationStub = new RepositoryPublicationStubInternalError();

        // When
        checkDatabaseWith(repoPublicationStub);

        // Then
        assertThat(stateResult)
                .hasToString("Database connexion \n"
                        + " " + " - Publication Z" + " " + OK_STATE
                        + " " + " - Publication I" + " " + KO_STATE
                        + " " + " - Gestion" + " " + OK_STATE);
        assertThat(errorMessage).hasToString("- Publication I " + null + " \n");
    }

    private void checkDatabaseWith(RepositoryPublication repoPublicationStub) {
        RepositoryGestion repoGestionStub = new RepositoryGestionStub();
        var healthcheckApi = new HealthcheckResources(
                repoGestionStub,
                repoPublicationStub,
                documentsStoragePublicationInterne,
                documentsStoragePublicationExterne,
                documentsStorageGestion);
        healthcheckApi.checkDatabase(errorMessage, stateResult);
    }
}
