package fr.insee.rmes.webservice;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.geography.GeoFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/geo")
@SecurityRequirement(name = "bearerAuth")
@Tag(name="Geography", description="Geography API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class GeographyResources  extends GenericResources {

	static final Logger logger = LogManager.getLogger(GeographyResources.class);

	@Autowired
	GeographyService geoService;


	/***************************************************************************************************
	 * COG
	 ******************************************************************************************************/
	@GetMapping(value = "/territories", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getGeoFeatures", summary = "List of geofeatures", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=GeoFeature.class))))})
	public ResponseEntity<Object> getGeoFeatures() throws RmesException {
		String jsonResultat = geoService.getGeoFeatures();
		return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
	}
	
	@GetMapping(value = "/territory/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getGeoFeature", summary = "Geofeature", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=GeoFeature.class)))})
	public ResponseEntity<Object> getGeoFeature(@PathVariable(Constants.ID) String id) throws RmesException {
		String jsonResultat = geoService.getGeoFeatureById(id).toString();
		return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
	}
	

	/**
	 * CREATE
	 * @param body
	 * @return response
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")
	@PostMapping(value = "/territory", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createGeograohy", summary = "Create feature")
	public ResponseEntity<Object> createGeography(
			@Parameter(description = "Geo Feature to create", required = true, 
            content = @Content(schema = @Schema(implementation = GeoFeature.class))) @RequestBody String body) {
		try {
			String id = geoService.createFeature(body);
			return ResponseEntity.status(HttpStatus.SC_OK).body(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")
	@PutMapping(value = "/territory/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "updateGeography", summary = "Update geography ")
	public ResponseEntity<Object> updateGeography(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Geo Feature to update", required = true, schema = @Schema(implementation= GeoFeature.class)) @RequestBody String body) {
		try {
			geoService.updateFeature(id, body);
			return ResponseEntity.ok(HttpStatus.SC_OK);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}
}
