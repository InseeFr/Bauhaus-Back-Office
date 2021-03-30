package fr.insee.rmes.webservice.operations;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.operations.documentation.Attribute;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Component
@Qualifier("Report")
@Path("/operations")
public class MetadataReportResources extends OperationsCommonResources {

	
	/***************************************************************************************************
	 * DOCUMENTATION
	 ******************************************************************************************************/

	@GET
	@Path("/metadataStructureDefinition")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMsd", summary = "Metadata structure definition", 
	responses = { @ApiResponse(content = @Content(/*mediaType = "application/json",*/ schema = @Schema(implementation = MAS.class)))})
	public Response getMSD(
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header
			) {
		MSD msd ;
		String jsonResultat = null ;

		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				msd = operationsService.getMSD();
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
			return Response.ok(XMLUtils.produceResponse(msd, header)).build();
		}

		else {
			try {
				jsonResultat = operationsService.getMSDJson();
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
			return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
		}
	}

	@GET
	@Path("/metadataAttribute/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMA", summary = "Metadata attribute specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Attribute.class)))})
	public Response getMetadataAttribute(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataAttribute(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/metadataAttributes")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMAs", summary = "Metadata attributes specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type="array",implementation = Attribute.class)))})
	public Response getMetadataAttributes() {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataAttributes();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


	@GET
	@Path("/metadataReport/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Metadata report for an id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public Response getMetadataReport(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataReport(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/metadataReport/default")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReportDefaultValue", summary = "Get default value for metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public Response getMetadataReportDefaultValue() throws IOException {
		return Response.status(HttpStatus.SC_OK).entity(operationsService.getMetadataReportDefaultValue()).build();
	}

	@GET
	@Path("/metadataReport/fullSims/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFullSims", summary = "Full sims for an id", 
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Documentation.class)
			))})
	public Response getFullSims(
			@Parameter(
					description = "Identifiant de la documentation (format : [0-9]{4})",
					required = true,
					schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathParam(Constants.ID) String id,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header
			) {
		Documentation fullsims;
		String jsonResultat;
		
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				fullsims = operationsService.getFullSimsForXml(id);
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}

			return Response.ok(XMLUtils.produceResponse(fullsims, header)).build();
		}

		else {
			try {
				jsonResultat = operationsService.getFullSimsForJson(id);
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
			return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
		}
		
		
		
	
	}

	/**
	 * GET
	 * @param id
	 * @return
	 */

	@GET
	@Path("/metadataReport/Owner/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Owner stamp for a Metadata report's id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public Response getMetadataReportOwner(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataReportOwner(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@POST
	@Path("/metadataReport")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReport", summary = "Create metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN))})
	public Response setMetadataReport(@RequestBody(description = "Metadata report to create", required = true,
	content = @Content(schema = @Schema(implementation = Documentation.class))) String body) {
		logger.info("POST Metadata report");
		String id = null;
		try {
			id = operationsService.createMetadataReport(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		if (id == null) {return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(id).build();}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR, Roles.SPRING_CNIS })
	@PUT
	@Path("/metadataReport/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReportById", summary = "Update metadata report")
	public Response setMetadataReportById(
			@PathParam(Constants.ID) String id, 
			@RequestBody(description = "Report to update", required = true,
			content = @Content(schema = @Schema(implementation = Documentation.class))) String body) {
		try {
			operationsService.setMetadataReport(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * DELETE
	 * @param id
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@DELETE
	@Path("/metadataReport/delete/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "deleteMetadataReportById", summary = "Delete metadata report")
	public Response deleteMetadataReportById(
			@PathParam(Constants.ID) String id) {
		Status result=Status.NO_CONTENT;
		try {
			result = operationsService.deleteMetadataReport(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(result).build();
	}



	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */	
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@PUT
	@Path("/metadataReport/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReportValidation", summary = "Sims validation")
	public Response setSimsValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			operationsService.publishMetadataReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}


	/**
	 * EXPORT
	 * @param id
	 * @param lg2
	 * @param includeEmptyMas
	 * @return response
	 */	

	@GET
	@Path("/metadataReport/export/{id}/{emptyMas}/{lg1}/{lg2}")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExport", summary = "Produce a document with a metadata report")
	public Response getSimsExport(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathParam(Constants.ID) String id
			,
			@Parameter(
					description = "Inclure les champs vides",
					required = false)  @QueryParam("emptyMas") Boolean includeEmptyMas
			,
			@Parameter(
					description = "Version française",
					required = false) @QueryParam("lg1")  Boolean lg1
			,
			@Parameter(
					description = "Version anglaise",
					required = false) @QueryParam("lg2")  Boolean lg2
			) throws RmesException {
		if (includeEmptyMas==null) {includeEmptyMas=true;}
		if (lg1==null) {lg1=true;}
		if (lg2==null) {lg2=true;}
		return operationsService.exportMetadataReport(id,includeEmptyMas,lg1,lg2);	
	}

	@GET
	@Path("/metadataReport/testExport")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExport", summary = "Produce a document with a metadata report")
	public Response getTestSimsExport() throws RmesException {
		return operationsService.exportTestMetadataReport();	
	}

}
