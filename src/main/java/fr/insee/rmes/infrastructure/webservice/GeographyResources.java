package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.geography.GeoFeature;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
public class GeographyResources {

	final
	GeographyService geoService;

	public GeographyResources(GeographyService geoService) {
		this.geoService = geoService;
	}


	/***************************************************************************************************
	 * COG
	 ******************************************************************************************************/
	@GetMapping(value = "/territories", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.READ)
	@Operation(summary = "List of geofeatures",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=GeoFeature.class))))})
	public ResponseEntity<Object> getGeoFeatures() throws RmesException {
		String jsonResultat = geoService.getGeoFeatures();
		return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
	}
	
	@GetMapping(value = "/territory/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.READ)
	@Operation(summary = "Geofeature",
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=GeoFeature.class)))})
	public ResponseEntity<Object> getGeoFeature(@PathVariable(Constants.ID) String id) throws RmesException {
		String jsonResultat = geoService.getGeoFeatureById(id).toString();
		return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
	}
	

	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value = "/territory", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create feature")
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

	@HasAccess(module = RBAC.Module.GEOGRAPHY, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value = "/territory/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update geography ")
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
