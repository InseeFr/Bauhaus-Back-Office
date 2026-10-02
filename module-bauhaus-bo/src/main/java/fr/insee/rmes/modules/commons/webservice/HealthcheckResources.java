package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.healthcheck.DependencyProbe;
import fr.insee.rmes.modules.commons.security.PublicEndpoint;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint public : la réponse dit ce qui répond, jamais pourquoi ça ne répond pas. Les causes
 * (messages d'exception, chemins, hôtes) ne vont que dans les logs.
 */
@RestController
@RequestMapping("healthcheck")
public class HealthcheckResources extends GenericResources {

    public static final String OK_STATE = ": OK \n";

    public static final String KO_STATE = ": KO \n";

    private static final String SPARQL_QUERY = "SELECT * { ?s a ?t } LIMIT 1";

    private static final String WITNESS_FILE = "testHealthcheck.txt";

    private final RepositoryGestion repoGestion;

    private final RepositoryPublication repositoryPublication;

    private final String documentsStoragePublicationInterne;
    private final String documentsStoragePublicationExterne;
    private final String documentsStorageGestion;

    private final List<DependencyProbe> dependencyProbes;

    private static final Logger logger = LoggerFactory.getLogger(HealthcheckResources.class);

    public HealthcheckResources(
            RepositoryGestion repoGestion,
            RepositoryPublication repositoryPublication,
            @Value("${fr.insee.rmes.bauhaus.storage.document.publication.interne}")
                    String documentsStoragePublicationInterne,
            @Value("${fr.insee.rmes.bauhaus.storage.document.publication}") String documentsStoragePublicationExterne,
            @Value("${fr.insee.rmes.bauhaus.storage.document.gestion}") String documentsStorageGestion,
            List<DependencyProbe> dependencyProbes) {
        this.repoGestion = repoGestion;
        this.repositoryPublication = repositoryPublication;
        this.documentsStoragePublicationInterne = documentsStoragePublicationInterne;
        this.documentsStoragePublicationExterne = documentsStoragePublicationExterne;
        this.documentsStorageGestion = documentsStorageGestion;
        this.dependencyProbes = dependencyProbes;
    }

    @PublicEndpoint
    @GetMapping(
            value = "",
            produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> getHealthcheck() {

        StringJoiner errorMessage = new StringJoiner(" ");
        StringJoiner stateResult = new StringJoiner(" ");
        logger.info(" Begin healthCheck");

        checkDatabase(errorMessage, stateResult);
        checkStrorage(errorMessage, stateResult);
        checkExternalServices(errorMessage, stateResult);

        // print result in log
        logger.debug("{}", stateResult);
        logger.debug("End healthcheck");

        if (!"".equals(errorMessage.toString())) {
            logger.error("Errors message : \n {}", errorMessage);
            return ResponseEntity.internalServerError()
                    .body(stateResult.merge(errorMessage).toString());
        } else {
            return ResponseEntity.ok(stateResult.toString());
        }
    }

    private void checkStrorage(StringJoiner errorMessage, StringJoiner stateResult) {
        stateResult.add("Document storage \n");
        checkDocumentStorage(this.documentsStorageGestion, "Gestion", stateResult, errorMessage);
        checkDocumentStorage(this.documentsStoragePublicationExterne, "Publication Externe", stateResult, errorMessage);
        checkDocumentStorage(this.documentsStoragePublicationInterne, "Publication Interne", stateResult, errorMessage);
    }

    public void checkDatabase(StringJoiner errorMessage, StringJoiner stateResult) {
        // Test database connexion
        stateResult.add("Database connexion \n");
        checkDatabaseConnexions(errorMessage, stateResult);
    }

    private void checkDatabaseConnexions(StringJoiner errorMessage, StringJoiner stateResult) {
        checkDatabaseConnexion(errorMessage, stateResult, repositoryPublication::getResponse, "Publication Z");
        checkDatabaseConnexion(
                errorMessage, stateResult, repositoryPublication::getResponsePublication, "Publication I");
        checkDatabaseConnexion(errorMessage, stateResult, repoGestion::getResponse, "Gestion");
    }

    private void checkDatabaseConnexion(
            StringJoiner errorMessage, StringJoiner stateResult, RequestExecutor executeRequest, String repoName) {
        try {
            if (StringUtils.isEmpty(executeRequest.execute(SPARQL_QUERY))) {
                errorMessage.add("-").add(repoName).add("doesn't return statement \n");
                stateResult.add(" -").add(repoName).add(KO_STATE);
            } else {
                stateResult.add(" -").add(repoName).add(OK_STATE);
            }
        } catch (Exception e) {
            logger.error("Test connexion {}", repoName, e);
            reportUnreachable(repoName, errorMessage, stateResult);
        }
    }

    private void checkExternalServices(StringJoiner errorMessage, StringJoiner stateResult) {
        if (dependencyProbes.isEmpty()) {
            return;
        }
        stateResult.add("External services \n");
        for (DependencyProbe probe : dependencyProbes) {
            try {
                probe.check().run();
                stateResult.add(" -").add(probe.name()).add(OK_STATE);
            } catch (Exception e) {
                logger.error("Test connexion {}", probe.name(), e);
                reportUnreachable(probe.name(), errorMessage, stateResult);
            }
        }
    }

    private static void reportUnreachable(String name, StringJoiner errorMessage, StringJoiner stateResult) {
        errorMessage.add("-").add(name).add("unreachable \n");
        stateResult.add(" -").add(name).add(KO_STATE);
    }

    private void checkDocumentStorage(
            String pathToStorage, String storageType, StringJoiner stateResult, StringJoiner errorMessage) {
        Path testFile = Path.of(pathToStorage, WITNESS_FILE);
        try {
            if (!testFile.toFile().createNewFile()) {
                errorMessage
                        .add("- File for healthcheck already exists in")
                        .add(storageType)
                        .add("\n");
                stateResult.add(" - File creation").add(storageType).add(KO_STATE);
            } else {
                stateResult.add(" - File creation").add(storageType).add(OK_STATE);
            }
            if (!Files.deleteIfExists(testFile)) {
                errorMessage.add("- Can't delete test file").add(storageType).add("\n");
                stateResult.add(" - File deletion").add(storageType).add(KO_STATE);
            } else {
                stateResult.add(" - File deletion").add(storageType).add(OK_STATE);
            }
        } catch (IOException e) {
            logger.error("Test document storage {} ({})", storageType, testFile, e);
            errorMessage.add("- Can't write test file in").add(storageType).add("\n");
            stateResult.add(" - Document storage").add(storageType).add(KO_STATE);
        }
    }

    private interface RequestExecutor {
        String execute(String request) throws RmesException;
    }
}
