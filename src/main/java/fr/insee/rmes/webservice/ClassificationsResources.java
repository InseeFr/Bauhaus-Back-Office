package fr.insee.rmes.webservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	@GetMapping(value = "/families", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getClassificationFamilies", summary = "List of classification families", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getFamilies()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamilies();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/family/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getFamily", summary = "Classification family", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyClass.class)))})
	public ResponseEntity<Object> getFamily(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamily(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/family/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getFamilyMembers", summary = "Members of family", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public ResponseEntity<Object> getFamilyMembers(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getFamilyMembers(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/series", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getClassificationSeries", summary = "List of classification series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getSeries()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getSeries();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/series/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getOneSeries(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getOneSeries(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/series/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getSeriesMembers", summary = "Members of series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public ResponseEntity<Object> getSeriesMembers(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getSeriesMembers(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="",produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getClassifications", summary = "List of classifications", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getClassifications()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassifications();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassification(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassification(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@PutMapping(value="/classification/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setClassifValidation", summary = "Classification validation")
	public ResponseEntity<Object> setOperationValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			classificationsService.setClassificationValidation(id);
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	@GetMapping(value="/classification/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItems(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItems(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{id}/levels", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevels(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevels(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{id}/level/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevel(@PathVariable(Constants.ID) String id, @PathVariable("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevel(id, levelId);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{classificationId}/level/{levelId}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevelMembers(@PathVariable("classificationId") String classificationId, @PathVariable("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItem(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItem(classificationId, itemId);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNotes(@PathVariable("classificationId") String classificationId,
			@PathVariable("itemId") String itemId, @PathVariable("conceptVersion") int conceptVersion)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/narrowers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNarrowers(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationItemNarrowers(classificationId, itemId);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/correspondences", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondences()  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondences();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/correspondence/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondence(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondence(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/correspondence/{id}/associations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceAssociations(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociations(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/correspondence/{correspondenceId}/association/{associationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceItem(@PathVariable("correspondenceId") String correspondenceId,
			@PathVariable("associationId") String associationId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociation(correspondenceId, associationId);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

}
