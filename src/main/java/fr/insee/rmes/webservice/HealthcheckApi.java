package fr.insee.rmes.webservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.StringJoiner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Opération réussie"),
	    @ApiResponse(responseCode = "400", description = "La syntaxe de la requête est incorrecte"),
	    @ApiResponse(responseCode = "401", description = "Une authentification est nécessaire pour accéder à la ressource"),
	    @ApiResponse(responseCode = "404", description = "Ressource non trouvée"),
	    @ApiResponse(responseCode = "406", description = "L'en-tête HTTP 'Accept' contient une valeur non acceptée"),
	    @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
	})
@Path("/healthcheck")
public class HealthcheckApi {
	
	private static final String OK_STATE = ": OK \n";

	private static final String KO_STATE = ": KO \n";

	String sparlQuery = "SELECT * { ?s a ?t } LIMIT 1";
	
	@Autowired
	protected RepositoryGestion repoGestion;
	
	private static final Logger logger = LogManager.getLogger(HealthcheckApi.class);

    @GET
    @Produces({
        MediaType.TEXT_PLAIN
    })
    public Response getHealthcheck() {
    	
    	StringJoiner errorMessage = new StringJoiner(" ");
    	StringJoiner stateResult = new StringJoiner(" ");
    	stateResult.add("Database connexion \n");
    	logger.info(" Begin healthCheck");
    	
    	//Test database connexion
    	try {
			if (StringUtils.isEmpty(RepositoryPublication.getResponse(sparlQuery))){
				errorMessage.add("- Repository publication doesn't return statement \n");
				stateResult = stateResult.add(" - Connexion publication").add(KO_STATE);
			}else {
				stateResult = stateResult.add(" - Connexion publication").add(OK_STATE);
			}
	    	if (StringUtils.isEmpty( 	repoGestion.getResponse(sparlQuery))) {
	    		errorMessage.add("- Repository gestion doesn't return statement \n");
	    		stateResult = stateResult.add(" - Connexion gestion").add(KO_STATE);
	    	}else {
	    		stateResult = stateResult.add(" - Connexion gestion").add(OK_STATE);
	    	}
		} catch (RmesException e) {
			errorMessage.add("- "+e.getMessage()+ " \n");
			stateResult = stateResult.add(" - Connexion database").add(KO_STATE);
		}
    	
    	stateResult = stateResult.add("Document storage \n");
    	//Test access to storage
    	checkDocumentStorage(Config.DOCUMENTS_STORAGE_GESTION,"Gestion", stateResult, errorMessage);
    	checkDocumentStorage(Config.DOCUMENTS_STORAGE_PUBLICATION_EXTERNE,"Publication Externe", stateResult, errorMessage);
    	checkDocumentStorage(Config.DOCUMENTS_STORAGE_PUBLICATION_INTERNE,"Publication Interne", stateResult, errorMessage);
    	
         logger.info("{}",stateResult);
         logger.info("End healthcheck");
         
    	if (!"".equals(errorMessage.toString())) {
    		logger.error("{}",errorMessage);
    		return Response.serverError().entity(errorMessage.toString()).build();
    	}
    	else {
    		return Response.ok(stateResult.toString()).build();
    	}
    }
    
    private void checkDocumentStorage(String pathToStorage, String storageType, StringJoiner stateResult, StringJoiner errorMessage) {
        String dirPath = pathToStorage + "testHealthcheck.txt";
        File testFile = new File(dirPath);
        try {
			if (!testFile.createNewFile()) {
				errorMessage.add("- File for healthcheck already exists in").add(storageType).add("\n");
				stateResult.add(" - File creation").add(storageType).add(KO_STATE);
			}else {
				stateResult.add(" - File creation").add(storageType).add(OK_STATE);
			}
			if (!Files.deleteIfExists(testFile.toPath())) {
				errorMessage.add("- Can't delete test file").add(storageType).add("\n");
				stateResult.add(" - File deletion").add(storageType).add(KO_STATE);
			}else {
				stateResult.add(" - File deletion").add(storageType).add(OK_STATE);
			}
		} catch (IOException e) {
			errorMessage.add("- IOException to save file in").add(pathToStorage).add("-").add(e.getMessage()).add("\n");
			stateResult.add(" - Document storage").add(storageType).add(KO_STATE);
		}
    }
    
}