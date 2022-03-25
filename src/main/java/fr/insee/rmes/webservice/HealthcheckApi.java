package fr.insee.rmes.webservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.StringJoiner;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
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
@RestController
@RequestMapping("healthcheck")
public class HealthcheckApi  extends GenericResources {
	
	private static final String CONNEXION_LDAP = "- Connexion LDAP";

	private static final String OK_STATE = ": OK \n";

	private static final String KO_STATE = ": KO \n";

	String sparlQuery = "SELECT * { ?s a ?t } LIMIT 1";
	
	@Autowired
	protected RepositoryGestion repoGestion;
	
	@Autowired
	protected UserRolesManagerService userService;
	
	private static final Logger logger = LogManager.getLogger(HealthcheckApi.class);

    @GetMapping("")
    @Produces({
        MediaType.TEXT_PLAIN
    })
    public ResponseEntity<Object> getHealthcheck() {
    	
    	StringJoiner errorMessage = new StringJoiner(" ");
    	StringJoiner stateResult = new StringJoiner(" ");
    	logger.info(" Begin healthCheck");

    	//Test database connexion
    	stateResult.add("Database connexion \n");   	
    	checkDatabaseConnexions(errorMessage, stateResult);
    	
    	//Test access to storage
    	stateResult = stateResult.add("Document storage \n");
    	checkDocumentStorage(config.getDocumentsStorageGestion(),"Gestion", stateResult, errorMessage);
    	checkDocumentStorage(config.getDocumentsStoragePublicationExterne(),"Publication Externe", stateResult, errorMessage);
    	checkDocumentStorage(config.getDocumentsStoragePublicationInterne(),"Publication Interne", stateResult, errorMessage);
    	
    	//Test LDAP connexion
    	stateResult = stateResult.add("LDAP connexion \n");
    	
    	try {
			String result = userService.checkSugoiConnexion();
	    	if ("OK".equals(result)) {
	    		stateResult.add(CONNEXION_LDAP+" - Sugoi").add(OK_STATE);
	    	}else {
				errorMessage.add("- Sugoi No functional error but return an empty string \n");
	    		stateResult.add(CONNEXION_LDAP+" - Sugoi").add(KO_STATE);
	    	}
		} catch (RmesException e) {
			errorMessage.add("- "+e.getMessage()+ " \n");
			stateResult.add(CONNEXION_LDAP+" - Sugoi").add(KO_STATE);
		}

    	
    	//print result in log
         logger.debug("{}",stateResult);
         logger.debug("End healthcheck");
         
    	if (!"".equals(errorMessage.toString())) {
    		logger.error("Errors message : \n {}",errorMessage);
    		return ResponseEntity.internalServerError().body(stateResult.merge(errorMessage).toString());
    	}
    	else {
    		return ResponseEntity.ok(stateResult.toString());
    	}
    }

	public void checkDatabaseConnexions(StringJoiner errorMessage, StringJoiner stateResult) {
		try {
			if (StringUtils.isEmpty(RepositoryPublication.getResponse(sparlQuery))){
				errorMessage.add("- Repository publication doesn't return statement \n");
				stateResult.add(" - Connexion publication Z").add(KO_STATE);
			}else {
				stateResult.add(" - Connexion publication Z").add(OK_STATE);
			}
			if (StringUtils.isEmpty(RepositoryPublication.getResponseInternalPublication(sparlQuery))){
				errorMessage.add("- Repository publication interne doesn't return statement \n");
				stateResult.add(" - Connexion publication I").add(KO_STATE);
			}else {
				stateResult.add(" - Connexion publication I").add(OK_STATE);
			}
	    	if (StringUtils.isEmpty( repoGestion.getResponse(sparlQuery))) {
	    		errorMessage.add("- Repository gestion doesn't return statement \n");
	    		stateResult.add(" - Connexion gestion").add(KO_STATE);
	    	}else {
	    		stateResult.add(" - Connexion gestion").add(OK_STATE);
	    	}
		} catch (RmesException e) {
			errorMessage.add("- "+e.getMessage()+ " \n");
			stateResult.add(" - Connexion database").add(KO_STATE);
		} catch (Exception e) {
			errorMessage.add("- "+e.getClass().getSimpleName() +e.getMessage()+ " \n");
			stateResult.add(" - Connexion database").add(KO_STATE);
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