package fr.insee.rmes.webservice;

import javax.ws.rs.GET;
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
import fr.insee.rmes.config.swagger.model.classifications.FamilyClass;
import fr.insee.rmes.config.swagger.model.classifications.Members;
import fr.insee.rmes.persistance.service.ClassificationsService;
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

	final static Logger logger = LogManager.getLogger(ClassificationsResources.class);

	@Autowired
	ClassificationsService classificationsService;
	
	@GET
	@Path("/families")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getClassificationFamilies", summary = "List of classification families", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getFamilies() throws Exception {
		String jsonResultat = classificationsService.getFamilies();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getFamily", summary = "Classification family", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyClass.class)))})
	public Response getFamily(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getFamily(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/family/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getFamilyMembers", summary = "Members of family", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public Response getFamilyMembers(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getFamilyMembers(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getClassificationSeries", summary = "List of classification series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getSeries() throws Exception {
		String jsonResultat = classificationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/series/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOneSeries(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getOneSeries(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/series/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getSeriesMembers", summary = "Members of series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public Response getSeriesMembers(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getSeriesMembers(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getClassifications", summary = "List of classifications", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getClassifications() throws Exception {
		String jsonResultat = classificationsService.getClassifications();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassification(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getClassification(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/items")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItems(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getClassificationItems(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/levels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevels(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getClassificationLevels(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/level/{levelId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevel(@PathParam("id") String id, @PathParam("levelId") String levelId) throws Exception {
		String jsonResultat = classificationsService.getClassificationLevel(id, levelId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/level/{levelId}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevelMembers(@PathParam("classificationId") String classificationId, @PathParam("levelId") String levelId) throws Exception {
		String jsonResultat = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItem(@PathParam("classificationId") String classificationId, @PathParam("itemId") String itemId) throws Exception {
		String jsonResultat = classificationsService.getClassificationItem(classificationId, itemId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNotes(@PathParam("classificationId") String classificationId,
			@PathParam("itemId") String itemId, @PathParam("conceptVersion") int conceptVersion) throws Exception {
		String jsonResultat = classificationsService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}/narrowers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNarrowers(@PathParam("classificationId") String classificationId, @PathParam("itemId") String itemId) throws Exception {
		String jsonResultat = classificationsService.getClassificationItemNarrowers(classificationId, itemId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondences")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondences() throws Exception {
		String jsonResultat = classificationsService.getCorrespondences();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondence/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondence(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getCorrespondence(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondence/{id}/associations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondenceAssociations(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getCorrespondenceAssociations(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondence/{correspondenceId}/association/{associationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondenceItem(@PathParam("correspondenceId") String correspondenceId,
			@PathParam("associationId") String associationId) throws Exception {
		String jsonResultat = classificationsService.getCorrespondenceAssociation(correspondenceId, associationId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
