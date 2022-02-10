package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.classifications.FamilyClass;
import fr.insee.rmes.config.swagger.model.classifications.Members;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * WebService class for resources of Classifications
 * 
 * 
 * @author N. Laval
 *
 */

@Component
@Path("/classifications")
@Tag(name ="Classifications",description = "Classification API")
@ApiResponses(value = { 
@ApiResponse(responseCode = "200", description = "Success"), 
@ApiResponse(responseCode = "204", description = "No Content"),
@ApiResponse(responseCode = "400", description = "Bad Request"), 
@ApiResponse(responseCode = "401", description = "Unauthorized"),
@ApiResponse(responseCode = "403", description = "Forbidden"), 
@ApiResponse(responseCode = "404", description = "Not found"),
@ApiResponse(responseCode = "406", description = "Not Acceptable"),
@ApiResponse(responseCode = "500", description = "Internal server error") })
public class ClassificationsResources {

	static final Logger logger = LogManager.getLogger(ClassificationsResources.class);

	@Autowired
	ClassificationsService classificationsService;
	
	@GET
	@Path("/families")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getClassificationFamilies", summary = "List of classification families", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getFamilies()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamilies();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getFamily", summary = "Classification family", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyClass.class)))})
	public Response getFamily(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamily(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/family/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getFamilyMembers", summary = "Members of family", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public Response getFamilyMembers(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamilyMembers(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getClassificationSeries", summary = "List of classification series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getSeries()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getSeries();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/series/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOneSeries(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getOneSeries(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/series/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getSeriesMembers", summary = "Members of series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public Response getSeriesMembers(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getSeriesMembers(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getClassifications", summary = "List of classifications", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getClassifications()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassifications();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassification(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassification(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@PUT
	@Path("/classification/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setClassifValidation", summary = "Classification validation")
	public Response setOperationValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			classificationsService.setClassificationValidation(id);
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}
	
	@GET
	@Path("/classification/{id}/items")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItems(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItems(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/levels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevels(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevels(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/level/{levelId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevel(@PathParam(Constants.ID) String id, @PathParam("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevel(id, levelId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/level/{levelId}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevelMembers(@PathParam("classificationId") String classificationId, @PathParam("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItem(@PathParam("classificationId") String classificationId, @PathParam("itemId") String itemId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItem(classificationId, itemId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNotes(@PathParam("classificationId") String classificationId,
			@PathParam("itemId") String itemId, @PathParam("conceptVersion") int conceptVersion)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}/narrowers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNarrowers(@PathParam("classificationId") String classificationId, @PathParam("itemId") String itemId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItemNarrowers(classificationId, itemId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondences")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondences()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondences();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondence/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondence(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondence(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondence/{id}/associations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondenceAssociations(@PathParam(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociations(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondence/{correspondenceId}/association/{associationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondenceItem(@PathParam("correspondenceId") String correspondenceId,
			@PathParam("associationId") String associationId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociation(correspondenceId, associationId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
