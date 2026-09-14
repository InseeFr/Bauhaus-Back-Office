package fr.insee.rmes.modules.commons.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Le healthcheck écrit puis supprime un fichier témoin dans chacun des trois stockages, et
 * interroge les trois dépôts RDF : le corps de la réponse dit, ligne par ligne, ce qui répond.
 */
@ExtendWith(MockitoExtension.class)
class HealthcheckResourcesTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    @TempDir
    Path storage;

    private HealthcheckResources healthcheck;

    @BeforeEach
    void setUp() {
        String directory = storage.toString() + "/";
        healthcheck = new HealthcheckResources(repoGestion, repositoryPublication, directory, directory, directory);
    }

    @Test
    void shouldReportEverythingUpWhenTheThreeRepositoriesAnswerAndTheStorageIsWritable() throws RmesException {
        when(repoGestion.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponsePublication(anyString())).thenReturn("statement");

        ResponseEntity<Object> response = healthcheck.getHealthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().toString()).doesNotContain(HealthcheckResources.KO_STATE.trim());
        assertThat(response.getBody().toString()).contains("Database connexion", "Document storage");
    }

    @Test
    void shouldFailWhenARepositoryReturnsNothing() throws RmesException {
        when(repoGestion.getResponse(anyString())).thenReturn("");
        when(repositoryPublication.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponsePublication(anyString())).thenReturn("statement");

        ResponseEntity<Object> response = healthcheck.getHealthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).contains("Gestion doesn't return statement");
    }

    @Test
    void shouldFailWhenARepositoryIsUnreachable() throws RmesException {
        when(repoGestion.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponse(anyString())).thenThrow(new RuntimeException("connexion refusée"));
        when(repositoryPublication.getResponsePublication(anyString())).thenReturn("statement");

        ResponseEntity<Object> response = healthcheck.getHealthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).contains("connexion refusée");
    }

    /** Un fichier témoin déjà présent signale que le healthcheck précédent n'a pas su le supprimer. */
    @Test
    void shouldFailWhenTheWitnessFileAlreadyExistsInTheStorage() throws RmesException, IOException {
        Files.createFile(storage.resolve("testHealthcheck.txt"));
        when(repoGestion.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponsePublication(anyString())).thenReturn("statement");

        ResponseEntity<Object> response = healthcheck.getHealthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).contains("File for healthcheck already exists");
    }
}
