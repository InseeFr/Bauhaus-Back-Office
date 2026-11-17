package fr.insee.rmes.modules.commons.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.healthcheck.DependencyProbe;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Le healthcheck écrit puis supprime un fichier témoin dans chacun des trois stockages, interroge
 * les trois dépôts RDF et sonde les services externes (MinIO, Colectica) : le corps de la réponse
 * dit, ligne par ligne, ce qui répond. L'endpoint est public : il ne révèle jamais le message
 * d'une exception.
 */
@ExtendWith(MockitoExtension.class)
class HealthcheckResourcesTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    /** Répertoire de stockage, configuré comme en production : sans séparateur final. */
    @TempDir
    Path storage;

    private HealthcheckResources healthcheck(DependencyProbe... probes) {
        String directory = storage.toString();
        return new HealthcheckResources(
                repoGestion, repositoryPublication, directory, directory, directory, List.of(probes));
    }

    /** Réponses des trois dépôts interrogés : gestion, publication, publication (requête de publication). */
    private void givenRepositoriesAnswer(String gestion, String publication, String publicationPublication)
            throws RmesException {
        when(repoGestion.getResponse(anyString())).thenReturn(gestion);
        when(repositoryPublication.getResponse(anyString())).thenReturn(publication);
        when(repositoryPublication.getResponsePublication(anyString())).thenReturn(publicationPublication);
    }

    private void givenRepositoriesAnswer() throws RmesException {
        givenRepositoriesAnswer("statement", "statement", "statement");
    }

    private static String assertHealthcheckFailsWith(ResponseEntity<Object> response, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        String body = response.getBody().toString();
        assertThat(body).contains(expectedMessage);
        return body;
    }

    @Test
    void shouldReportEverythingUpWhenTheThreeRepositoriesAnswerAndTheStorageIsWritable() throws RmesException {
        givenRepositoriesAnswer();

        ResponseEntity<Object> response = healthcheck().getHealthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().toString()).doesNotContain(HealthcheckResources.KO_STATE.trim());
        assertThat(response.getBody().toString()).contains("Database connexion", "Document storage");
    }

    @Test
    void shouldFailWhenARepositoryReturnsNothing() throws RmesException {
        givenRepositoriesAnswer("", "statement", "statement");

        assertHealthcheckFailsWith(healthcheck().getHealthcheck(), "Gestion doesn't return statement");
    }

    @Test
    void shouldFailWithoutRevealingTheCauseWhenARepositoryIsUnreachable() throws RmesException {
        when(repoGestion.getResponse(anyString())).thenReturn("statement");
        when(repositoryPublication.getResponse(anyString()))
                .thenThrow(new RuntimeException("Connection refused: graphdb-interne.insee.fr:7200"));
        when(repositoryPublication.getResponsePublication(anyString())).thenReturn("statement");

        String body = assertHealthcheckFailsWith(healthcheck().getHealthcheck(), "Publication Z unreachable");

        assertThat(body).doesNotContain("graphdb-interne");
    }

    /** Un fichier témoin déjà présent signale que le healthcheck précédent n'a pas su le supprimer. */
    @Test
    void shouldFailWhenTheWitnessFileAlreadyExistsInTheStorage() throws RmesException, IOException {
        Files.createFile(storage.resolve("testHealthcheck.txt"));
        givenRepositoriesAnswer();

        assertHealthcheckFailsWith(healthcheck().getHealthcheck(), "File for healthcheck already exists");
    }

    @Test
    void shouldFailWithoutRevealingTheStoragePathWhenTheStorageIsNotWritable() throws RmesException {
        storage = storage.resolve("missing-directory");
        givenRepositoriesAnswer();

        String body = assertHealthcheckFailsWith(healthcheck().getHealthcheck(), "Can't write test file in Gestion");

        assertThat(body).doesNotContain(storage.toString());
    }

    @Test
    void shouldReportTheExternalServicesThatAnswer() throws RmesException {
        givenRepositoriesAnswer();

        ResponseEntity<Object> response = healthcheck(
                        new DependencyProbe("MinIO", () -> {}), new DependencyProbe("Colectica", () -> {}))
                .getHealthcheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().toString())
                .contains("External services", " - MinIO " + HealthcheckResources.OK_STATE)
                .contains(" - Colectica " + HealthcheckResources.OK_STATE);
    }

    @Test
    void shouldFailWithoutRevealingTheCauseWhenAnExternalServiceIsUnreachable() throws RmesException {
        givenRepositoriesAnswer();

        ResponseEntity<Object> response = healthcheck(new DependencyProbe("Colectica", () -> {
                    throw new IllegalStateException("401 Unauthorized for user svc-bauhaus");
                }))
                .getHealthcheck();

        String body = assertHealthcheckFailsWith(response, "Colectica unreachable");
        assertThat(body)
                .contains(" - Colectica " + HealthcheckResources.KO_STATE)
                .doesNotContain("svc-bauhaus");
    }
}
