package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.classifications.FamilyClass;
import fr.insee.rmes.config.swagger.model.classifications.Members;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.classification.Classification;
import fr.insee.rmes.model.classification.ClassificationItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classifications")
@Tag(name ="Classifications",description = "Classification API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('classifications')")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = { 
@ApiResponse(responseCode = "200", description = "Success"), 
@ApiResponse(responseCode = "204", description = "No Content"),
@ApiResponse(responseCode = "400", description = "Bad Request"), 
@ApiResponse(responseCode = "401", description = "Unauthorized"),
@ApiResponse(responseCode = "403", description = "Forbidden"), 
@ApiResponse(responseCode = "404", description = "Not found"),
@ApiResponse(responseCode = "406", description = "Not Acceptable"),
@ApiResponse(responseCode = "500", description = "Internal server error") })
public class ClassificationsResources extends GenericResources {

	public enum Language {
		lg1, lg2;
	}
	static final Logger logger = LoggerFactory.getLogger(ClassificationsResources.class);

	@Autowired
	ClassificationsService classificationsService;

	@Autowired
	ClassificationItemService classificationItemService;

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
		try {
			String series = classificationsService.getSeries();
			return ResponseEntity.status(HttpStatus.OK).body(series);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}
	
	@GetMapping(value="/series/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getOneSeries(@PathVariable(Constants.ID) String id)  {
		try {
			String serie = classificationsService.getOneSeries(id);
            return ResponseEntity.status(HttpStatus.OK).body(serie);
        } catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
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
		try {
			String classification = classificationsService.getClassification(id);
			return ResponseEntity.status(HttpStatus.OK).body(classification);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
	@PutMapping(value="/classification/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "updateClassification", summary = "Update an existing classification" )
	public ResponseEntity<Object> updateClassification(
			@PathVariable(Constants.ID) String id,
			@Parameter(description = "Classification to update", required = true, content = @Content(schema = @Schema(implementation = Classification.class))) @org.springframework.web.bind.annotation.RequestBody String body)  {
		try {
			classificationsService.updateClassification(id, body);
			return ResponseEntity.status(HttpStatus.OK).body(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
	}
	
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
	@PutMapping(value="/classification/{id}/validate")
	@io.swagger.v3.oas.annotations.Operation(operationId = "publishClassification", summary = "Publish a classification")
	public ResponseEntity<Object> publishClassification(
			@PathVariable(Constants.ID) String id) {
		try {
			classificationsService.setClassificationValidation(id);
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	@GetMapping(value="/classification/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItems(@PathVariable(Constants.ID) String id)  {
		try {
			String items = classificationItemService.getClassificationItems(id);
			return ResponseEntity.status(HttpStatus.OK).body(items);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value="/classification/{id}/levels", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevels(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevels(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{id}/level/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevel(@PathVariable(Constants.ID) String id, @PathVariable("levelId") String levelId)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getClassificationLevel(id, levelId);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	@GetMapping(value="/classification/{classificationId}/level/{levelId}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevelMembers(@PathVariable("classificationId") String classificationId, @PathVariable("levelId") String levelId)  {
		try {
			String levelMembers = classificationsService.getClassificationLevelMembers(classificationId, levelId);
			return ResponseEntity.status(HttpStatus.OK).body(levelMembers);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItem(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId)  {
		try {
			String classificationItem = classificationItemService.getClassificationItem(classificationId, itemId);
			return ResponseEntity.status(HttpStatus.OK).body(classificationItem);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@PutMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateClassificationItem(
			@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId,
			@Parameter(description = "Classification to update", required = true, content = @Content(schema = @Schema(implementation = ClassificationItem.class))) @org.springframework.web.bind.annotation.RequestBody String body)  {
		try {
			classificationItemService.updateClassificationItem(classificationId, itemId, body);
			return ResponseEntity.status(HttpStatus.OK).body(itemId);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNotes(@PathVariable("classificationId") String classificationId,
			@PathVariable("itemId") String itemId, @PathVariable("conceptVersion") int conceptVersion)  {
		try {
			String notes = classificationItemService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
			return ResponseEntity.status(HttpStatus.OK).body(notes);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/narrowers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNarrowers(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId)  {
		try {
			String narrowers = classificationItemService.getClassificationItemNarrowers(classificationId, itemId);
			return ResponseEntity.status(HttpStatus.OK).body(narrowers);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value="/correspondences", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondences()  {
		try {
			String correspondences = classificationsService.getCorrespondences();
			return ResponseEntity.status(HttpStatus.OK).body(correspondences);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value="/correspondence/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondence(@PathVariable(Constants.ID) String id)  {
		try {
			String correspondence = classificationsService.getCorrespondence(id);
			return ResponseEntity.status(HttpStatus.OK).body(correspondence);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value="/correspondence/{id}/associations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceAssociations(@PathVariable(Constants.ID) String id)  {
		String jsonResultat;
		try {
			jsonResultat = classificationsService.getCorrespondenceAssociations(id);
		} catch (RmesException e) {
			return returnRmesException(e);
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
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
}
