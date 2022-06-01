package fr.insee.rmes.webservice;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * WebService class to download/upload resources in databases
 * 
 *         schemes: - http
 * 
 *         consumes: - application/json
 * 
 *         produces: - application/json
 *
 */
@RestController
@RequestMapping("/loader")
@SecurityRequirement(name = "bearerAuth")
@Tag(name="Loader", description="User Management")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class LoaderResources  extends GenericResources {

	static final Logger logger = LogManager.getLogger(LoaderResources.class);


	@Autowired
	protected RepositoryGestion repoGestion;
	
	
	//TRIG TO DATABASE
	//@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")	
	@Operation(operationId = "uploadTrig", summary = "Upload a trig file in database"  )
	@PostMapping(value = "/upload/trig"
			,consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
			,produces = {MediaType.MULTIPART_FORM_DATA_VALUE}
	)
	public ResponseEntity<Object> uploadTrig(
//			@Parameter(schema = @Schema(type = "string", format = "String", description = "Content-Type"))
//			@RequestHeader(required=false) ContentType contentType, 						
			@Parameter(description = "Trig file", required = true, schema = @Schema(type = "string", format = "binary", description = "file"))
			@RequestParam MultipartFile  trigFile,
			@Parameter(description = "Database",
		            schema = @Schema(nullable = true, allowableValues = {"gestion","diffusion"},type = "string")) 
			@RequestParam("database") String database) {
		
		try {
			checkDatabase(database);
			uploadTrigFile(trigFile);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	private void checkDatabase(String database) throws RmesException {
		if (database == null)  throw new RmesException(HttpStatus.BAD_REQUEST,"Database is missing", "Database is null");
		if (!database.equals("gestion")||!database.equals("diffusion")) throw new RmesException(HttpStatus.BAD_REQUEST,"Database is unknown", "Database is "+database);
		
	}

	public void uploadTrigFile(MultipartFile trigFile) throws RmesException {
		String documentName = trigFile.getName();
		
		try (InputStream content = trigFile.getInputStream()){//= new FileInputStream(trigFile)
			repoGestion.persistFile(content, RDFFormat.TRIG);
		}  catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR,"IOException : file " + documentName, e.getMessage());
		}

		
	}
	
}