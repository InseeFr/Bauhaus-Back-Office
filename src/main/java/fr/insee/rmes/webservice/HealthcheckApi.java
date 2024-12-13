package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesFileException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.StringJoiner;

@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Opération réussie"),
        @ApiResponse(responseCode = "400", description = "La syntaxe de la requête est incorrecte"),
        @ApiResponse(responseCode = "401", description = "Une authentification est nécessaire pour accéder à la ressource"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée"),
        @ApiResponse(responseCode = "406", description = "L'en-tête HTTP 'Accept' contient une valeur non acceptée"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
})
@RestController
@RequestMapping("healthcheck")
public class HealthcheckApi extends GenericResources {


    protected static final String OK_STATE = ": OK \n";

    protected static final String KO_STATE = ": KO \n";

    private static final String SPARL_QUERY = "SELECT * { ?s a ?t } LIMIT 1";

    private final RepositoryGestion repoGestion;

    private final RepositoryPublication repositoryPublication;

    private final FilesOperations filesOperations;

    private final String documentsStoragePublicationInterne;
    private final String documentsStoragePublicationExterne;
    private final  String documentsStorageGestion;

    private static final Logger logger = LoggerFactory.getLogger(HealthcheckApi.class);



    public HealthcheckApi(@Autowired RepositoryGestion repoGestion,
                          @Autowired RepositoryPublication repositoryPublication, FilesOperations filesOperations,
                          @Value("${fr.insee.rmes.bauhaus.storage.document.publication.interne}") String documentsStoragePublicationInterne,
                          @Value("${fr.insee.rmes.bauhaus.storage.document.publication}") String documentsStoragePublicationExterne,
                          @Value("${fr.insee.rmes.bauhaus.storage.document.gestion}") String documentsStorageGestion) {
        this.repoGestion = repoGestion;
        this.repositoryPublication = repositoryPublication;
        this.filesOperations = filesOperations;
        this.documentsStoragePublicationInterne = documentsStoragePublicationInterne;
        this.documentsStoragePublicationExterne = documentsStoragePublicationExterne;
        this.documentsStorageGestion = documentsStorageGestion;
    }

    @GetMapping(value = "",
            produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> getHealthcheck() {

        StringJoiner errorMessage = new StringJoiner(" ");
        StringJoiner stateResult = new StringJoiner(" ");
        logger.info(" Begin healthCheck");

        checkDatabase(errorMessage, stateResult);
        checkStrorage(stateResult);


        //print result in log
        logger.debug("{}", stateResult);
        logger.debug("End healthcheck");

        if (!"".equals(errorMessage.toString())) {
            logger.error("Errors message : \n {}", errorMessage);
            return ResponseEntity.internalServerError().body(stateResult.merge(errorMessage).toString());
        } else {
            return ResponseEntity.ok(stateResult.toString());
        }
    }


    private void checkStrorage(StringJoiner stateResult) {
        stateResult.add("Document storage \n");
        checkDocumentStorage(this.documentsStorageGestion, "Gestion", stateResult);
        checkDocumentStorage(this.documentsStoragePublicationExterne, "Publication Externe", stateResult);
        checkDocumentStorage(this.documentsStoragePublicationInterne, "Publication Interne", stateResult);
    }

    protected void checkDatabase(StringJoiner errorMessage, StringJoiner stateResult) {
        //Test database connexion
        stateResult.add("Database connexion \n");
        checkDatabaseConnexions(errorMessage, stateResult);
    }

    private void checkDatabaseConnexions(StringJoiner errorMessage, StringJoiner stateResult) {
        checkDatabaseConnexion(errorMessage, stateResult, repositoryPublication::getResponse, "Publication Z");
        checkDatabaseConnexion(errorMessage, stateResult, repositoryPublication::getResponseInternalPublication, "Publication I");
        checkDatabaseConnexion(errorMessage, stateResult, repoGestion::getResponse, "Gestion");
    }

    private void checkDatabaseConnexion(StringJoiner errorMessage, StringJoiner stateResult, RequestExecutor executeRequest, String repoName) {
        try {
            if (StringUtils.isEmpty(executeRequest.execute(SPARL_QUERY))) {
                errorMessage.add("-").add(repoName).add("doesn't return statement \n");
                stateResult.add(" -").add(repoName).add(KO_STATE);
            } else {
                stateResult.add(" -").add(repoName).add(OK_STATE);
            }
        } catch (Exception e) {
            errorMessage.add("-").add(repoName).add(e.getMessage()).add("\n");
            logger.error("Test connexion {}", repoName, e);
            stateResult.add(" -").add(repoName).add(KO_STATE);
        }
    }

    private void checkDocumentStorage(String pathToStorage, String storageType, StringJoiner stateResult) {
        String testFilename = "testHealthcheck.txt";
        Path testFile = Path.of(pathToStorage, testFilename);
            try {
                filesOperations.write(new ByteArrayInputStream("test".getBytes()), testFile);
                stateResult.add(" - File creation").add(storageType).add(OK_STATE);
            }catch (RmesFileException rfe){
                logger.error("While trying to write "+testFilename+" in "+storageType, rfe);
                stateResult.add(" - File creation").add(storageType).add(KO_STATE);
            }
            try{
                filesOperations.delete(testFile.toString());
                stateResult.add(" - File deletion").add(storageType).add(OK_STATE);
            }catch (RmesFileException rfe){
                logger.error("While trying to delete "+testFilename+" in "+storageType, rfe);
                stateResult.add(" - File deletion").add(storageType).add(KO_STATE);
            }
    }

    private interface RequestExecutor {
        String execute(String request) throws RmesException;
    }
}