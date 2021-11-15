package fr.insee.rmes.webservice;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.insee.rmes.model.operations.documentations.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.geography.GeoFeature;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Hidden
@Component
@Path("/geo")
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
	@GET
	@Path("/territories")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getGeoFeatures", summary = "List of geofeatures", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=GeoFeature.class))))})
	public Response getGeoFeatures() throws RmesException {
		String jsonResultat = geoService.getGeoFeatures();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/territory/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getGeoFeature", summary = "Geofeature", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=GeoFeature.class)))})
	public Response getGeoFeature(@PathParam(Constants.ID) String id) throws RmesException {
		String jsonResultat = geoService.getGeoFeatureById(id).toString();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	

	/**
	 * CREATE
	 * @param body
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@POST
	@Path("/territory")
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
	@PUT
	@Path("/territory/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updateGeography", summary = "Update geography ")
	public Response updateGeography(
			@Parameter(description = "Id", required = true) @PathParam(Constants.ID) String id,
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
