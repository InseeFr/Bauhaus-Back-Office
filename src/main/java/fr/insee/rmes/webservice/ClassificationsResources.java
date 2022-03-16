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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/classifications")
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

	@GetMapping("/families")
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
	
	@GetMapping("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getFamily", summary = "Classification family", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyClass.class)))})
	public Response getFamily(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamily(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/family/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getFamilyMembers", summary = "Members of family", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public Response getFamilyMembers(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamilyMembers(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/series")
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
	
	@GetMapping("/series/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOneSeries(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getOneSeries(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/series/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getSeriesMembers", summary = "Members of series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public Response getSeriesMembers(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getSeriesMembers(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
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
	
	@GetMapping("/classification/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassification(@PathVariable(Constants.ID) String id)  {
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
	@PutMapping("/classification/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setClassifValidation", summary = "Classification validation")
	public Response setOperationValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			classificationsService.setClassificationValidation(id);
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}
	
	@GetMapping("/classification/{id}/items")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItems(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItems(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/classification/{id}/levels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevels(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevels(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/classification/{id}/level/{levelId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevel(@PathVariable(Constants.ID) String id, @PathVariable("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevel(id, levelId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/classification/{classificationId}/level/{levelId}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevelMembers(@PathVariable("classificationId") String classificationId, @PathVariable("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/classification/{classificationId}/item/{itemId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItem(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItem(classificationId, itemId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNotes(@PathVariable("classificationId") String classificationId,
			@PathVariable("itemId") String itemId, @PathVariable("conceptVersion") int conceptVersion)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/classification/{classificationId}/item/{itemId}/narrowers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNarrowers(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItemNarrowers(classificationId, itemId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/correspondences")
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
	
	@GetMapping("/correspondence/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondence(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondence(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/correspondence/{id}/associations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondenceAssociations(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociations(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GetMapping("/correspondence/{correspondenceId}/association/{associationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondenceItem(@PathVariable("correspondenceId") String correspondenceId,
			@PathVariable("associationId") String associationId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociation(correspondenceId, associationId);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
