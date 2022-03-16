package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.geography.GeoFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/geo")
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

	static final Logger logger = LogManager.getLogger(GeographyResources.class);

	@Autowired
	GeographyService geoService;


	/***************************************************************************************************
	 * COG
	 ******************************************************************************************************/
	@GetMapping("/territories")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getGeoFeatures", summary = "List of geofeatures", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=GeoFeature.class))))})
	public Response getGeoFeatures() throws RmesException {
		String jsonResultat = geoService.getGeoFeatures();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/territory/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getGeoFeature", summary = "Geofeature", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=GeoFeature.class)))})
	public Response getGeoFeature(@PathVariable(Constants.ID) String id) throws RmesException {
		String jsonResultat = geoService.getGeoFeatureById(id).toString();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	

	/**
	 * CREATE
	 * @param body
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@PostMapping("/territory")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createGeograohy", summary = "Create feature")
	public Response createGeography(
			@RequestBody(description = "Geo Feature to create", required = true, 
            content = @Content(schema = @Schema(implementation = GeoFeature.class))) String body) {
		String id = null;
		try {
			id = geoService.createFeature(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@Secured({ Roles.SPRING_ADMIN })
	@PutMapping("/territory/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updateGeography", summary = "Update geography ")
	public Response updateGeography(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@RequestBody(description = "Geo Feature to update", required = true)
			@Parameter(schema = @Schema(implementation= GeoFeature.class)) String body) {
		try {
			geoService.updateFeature(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Response.Status.OK).build();
	}
}
