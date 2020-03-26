package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.dsd.DSDById;
import fr.insee.rmes.config.swagger.model.dsd.DSDComponentById;
import fr.insee.rmes.config.swagger.model.dsd.IdLabelType;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.Constants;
import fr.insee.rmes.persistance.service.DSDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@Path("/dsds")
@Tag(name="Data structure definitions", description="DSD API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class DSDResources {
	
	private static final String TEXT_PLAIN = "text/plain";

	final static Logger logger = LogManager.getLogger(DSDResources.class);

	@Autowired
	DSDService DSDService;


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDSDs", summary = "List of DSDs", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})	
	public Response getDSDs() {
		String jsonResultat;
		try {
			jsonResultat = DSDService.getDSDs();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/dsd/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDSDById", summary = "DSD", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=DSDById.class)))})	
	public Response getDSDById(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = DSDService.getDSDById(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/dsd/{dsdId}/components")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDSDComponents", summary = "List of components of a DSD", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabelType.class))))})	
	public Response getDSDComponents(@PathParam("dsdId") String dsdId) {
		String jsonResultat;
		try {
			jsonResultat = DSDService.getDSDComponents(dsdId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/dsd/{dsdId}/detailed-components")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDSDComponents", summary = "List of detailed components of a DSD", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=DSDComponentById.class))))})	
	public Response getDSDDetailedComponents(@PathParam("dsdId") String dsdId) {
		String jsonResultat;
		try {
			jsonResultat = DSDService.getDSDDetailedComponents(dsdId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/dsd/{dsdId}/component/{componentId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDSDComponentById", summary = "DSD component", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=DSDComponentById.class)))})	
	public Response getDSDComponentById(@PathParam("dsdId") String dsdId, @PathParam("componentId") String componentId) {
		String jsonResultat;
		try {
			jsonResultat = DSDService.getDSDComponentById(dsdId, componentId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@POST
	@Path("/dsd")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDSD", summary = "Create DSD" )
	public Response setDSD(@RequestBody(description = "DSD", required = true) String body) {
		String id = null;
		try {
			id = DSDService.setDSD(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}
	
	@PUT
	@Path("/dsd/{dsdId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDSD", summary = "Update DSD" )
	public Response setDSD(@PathParam("dsdId") String dsdId, @RequestBody(description = "DSD", required = true) String body) {
		String id = null;
		try {
			id = DSDService.setDSD(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}
}
