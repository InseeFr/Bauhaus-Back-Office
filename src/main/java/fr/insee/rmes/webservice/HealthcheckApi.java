package fr.insee.rmes.webservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
	
	String sparlQuery = "SELECT * { ?s a ?t } LIMIT 1";
	
	@Autowired
	protected RepositoryGestion repoGestion;
	
	private static final Logger logger = LogManager.getLogger(HealthcheckApi.class);

    @GET
    @Produces({
        MediaType.TEXT_PLAIN
    })
    public Response getHealthcheck() {
    	String errorMessage = "";
    	String stateResult = "Database connexion \n";
    	logger.info("Begin healthCheck");
    	
    	//Test database connexion
    	try {
			if (StringUtils.isEmpty(RepositoryPublication.getResponse(sparlQuery))){
				errorMessage = errorMessage.concat("- Repository publication doesn't return statement \n");
				stateResult = stateResult.concat(" - Connexion publication : KO \n");
			}else {
				stateResult = stateResult.concat(" - Connexion publication : OK \n");
			}
	    	if (StringUtils.isEmpty( 	repoGestion.getResponse(sparlQuery))) {
	    		errorMessage = errorMessage.concat("- Repository gestion doesn't return statement \n");
	    		stateResult = stateResult.concat(" - Connexion gestion : KO \n");
	    	}else {
	    		stateResult = stateResult.concat(" - Connexion gestion : OK \n");
	    	}
		} catch (RmesException e) {
			errorMessage = errorMessage.concat("- "+e.getMessage()+ " \n");
			stateResult = stateResult.concat(" - Connexion database : KO \n");
		}
    	
    	stateResult = stateResult.concat("Document storage \n");
    	//Test access to storage
         String dirPath = Config.DOCUMENTS_STORAGE + "testHealthcheck.txt";
         File testFile = new File(dirPath);
         try {
			if (!testFile.createNewFile()) {
				errorMessage = errorMessage.concat("- File for healthcheck already exists \n");
				stateResult = stateResult.concat(" - File creation : KO \n");
			}else {
				stateResult = stateResult.concat(" - File creation : OK \n");
			}
			if (!Files.deleteIfExists(testFile.toPath())) {
				errorMessage = errorMessage.concat("- Can't delete test file \n");
				stateResult = stateResult.concat(" - File deletion : KO \n");
			}else {
				stateResult = stateResult.concat(" - File deletion : OK \n");
			}
 		} catch (IOException e) {
 			errorMessage = errorMessage.concat("- IOException to save file in  "+Config.DOCUMENTS_STORAGE+" - "+e.getMessage()+ " \n");
 			stateResult = stateResult.concat(" - Document storage : KO \n");
		}
    	
         logger.info(stateResult);
         logger.info("End healthcheck");
         
    	if (!"".equals(errorMessage)) {
    		logger.error(errorMessage);
    		return Response.serverError().entity(errorMessage).build();
    	}
    	else {
    		return Response.ok(stateResult).build();
    	}
    }
}