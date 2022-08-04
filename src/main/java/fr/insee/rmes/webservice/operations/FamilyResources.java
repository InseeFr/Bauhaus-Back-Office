package fr.insee.rmes.webservice.operations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


/***************************************************************************************************
 * FAMILY
 ******************************************************************************************************/

@Qualifier("Family")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value = "/operations",  produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
public class FamilyResources extends OperationsCommonResources {

	
	@GetMapping("/families")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilies", summary = "List of families", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getFamilies() throws RmesException {
		String jsonResultat = operationsService.getFamilies();
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
	}

	@GetMapping("/families/advanced-search")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamiliesForSearch", summary = "List of families for search",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Family.class))))})
	public ResponseEntity<Object> getFamiliesForSearch() throws RmesException {
		String jsonResultat = operationsService.getFamiliesForSearch();
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
	}

	@GetMapping("/families/{id}/seriesWithReport")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesWithReport", summary = "Series with metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation= Operation.class)))})
	public ResponseEntity<Object> getSeriesWithReport(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getSeriesWithReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
	}

	@GetMapping("/family/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilyByID", summary = "Get a family", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Family.class)))}
			)
	public ResponseEntity<Object> getFamilyByID(@PathVariable(Constants.ID) String id) throws RmesException {
		String jsonResultat = operationsService.getFamilyByID(id);
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
	@PutMapping("/family/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyById", summary = "Update an existing family" )
	public ResponseEntity<Object> setFamilyById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Family to update", required = true, content = @Content(schema = @Schema(implementation = Family.class))) @RequestBody String body) {
		try {
			operationsService.setFamily(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	@PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
	@PostMapping("/family")
	@io.swagger.v3.oas.annotations.Operation(operationId = "createFamily", summary = "Create a new family")
	public ResponseEntity<Object> createFamily(
			@Parameter(description = "Family to create", required = true, content = @Content(schema = @Schema(implementation = Family.class))) 
			@RequestBody String body) {
		String id = null;
		try {
			id = operationsService.createFamily(body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
	@PutMapping("/family/validate/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyValidation", summary = "Validate a family")
	public ResponseEntity<Object> setFamilyValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setFamilyValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


}
