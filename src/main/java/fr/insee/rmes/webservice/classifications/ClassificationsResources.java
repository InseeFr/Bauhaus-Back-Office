package fr.insee.rmes.webservice.classifications;

import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.config.swagger.model.Id;
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
public class ClassificationsResources {


	public enum Language {
		lg1, lg2
	}
	final
	ClassificationsService classificationsService;

	final
	ClassificationItemService classificationItemService;

	public ClassificationsResources(ClassificationsService classificationsService, ClassificationItemService classificationItemService) {
		this.classificationsService = classificationsService;
		this.classificationItemService = classificationItemService;
	}

	@GetMapping(value = "/families", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getClassificationFamilies", summary = "List of classification families", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getFamilies() throws RmesException {
		String families = classificationsService.getFamilies();
		return ResponseEntity.status(HttpStatus.OK).body(families);
	}
	
	@GetMapping(value="/family/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getFamily", summary = "Classification family", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyClass.class)))})
	public ResponseEntity<Object> getFamily(@PathVariable(Constants.ID) String id) throws RmesException {
		String family = classificationsService.getFamily(id);
		return ResponseEntity.status(HttpStatus.OK).body(family);
	}
	
	@GetMapping(value="/family/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getFamilyMembers", summary = "Members of family", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public ResponseEntity<Object> getFamilyMembers(@PathVariable(Constants.ID) String id) throws RmesException {
		String familyMembers = classificationsService.getFamilyMembers(id);
		return ResponseEntity.status(HttpStatus.OK).body(familyMembers);
	}

	@GetMapping(value="/series", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getClassificationSeries", summary = "List of classification series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getSeries() throws RmesException {
		String series = classificationsService.getSeries();
		return ResponseEntity.status(HttpStatus.OK).body(series);
	}
	
	@GetMapping(value="/series/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getOneSeries(@PathVariable(Constants.ID) String id) throws RmesException {
		String serie = classificationsService.getOneSeries(id);
		return ResponseEntity.status(HttpStatus.OK).body(serie);
	}
	
	@GetMapping(value="/series/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getSeriesMembers", summary = "Members of series", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public ResponseEntity<Object> getSeriesMembers(@PathVariable(Constants.ID) String id) throws RmesException {
		String seriesMembers = classificationsService.getSeriesMembers(id);
		return ResponseEntity.status(HttpStatus.OK).body(seriesMembers);
	}
	
	@GetMapping(value="",produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getClassifications", summary = "List of classifications", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getClassifications() throws RmesException {
		String classifications = classificationsService.getClassifications();
		return ResponseEntity.status(HttpStatus.OK).body(classifications);
	}
	
	@GetMapping(value="/classification/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassification(@PathVariable(Constants.ID) String id) throws RmesException {
		String classification = classificationsService.getClassification(id);
		return ResponseEntity.status(HttpStatus.OK).body(classification);
	}

	@PreAuthorize("isAdmin()")
	@PutMapping(value="/classification/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "updateClassification", summary = "Update an existing classification" )
	public ResponseEntity<Id> updateClassification(
			@PathVariable(Constants.ID) Id id,
			@Parameter(description = "Classification to update", required = true, content = @Content(schema = @Schema(implementation = Classification.class))) @org.springframework.web.bind.annotation.RequestBody String body) throws RmesException {
		classificationsService.updateClassification(id.identifier(), body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PreAuthorize("isAdmin()")
	@PutMapping(value="/classification/{id}/validate")
	@io.swagger.v3.oas.annotations.Operation(operationId = "publishClassification", summary = "Publish a classification")
	public ResponseEntity<Id> publishClassification(
			@PathVariable(Constants.ID) Id id) throws RmesException {
		classificationsService.setClassificationValidation(id.identifier());
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	@GetMapping(value="/classification/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItems(@PathVariable(Constants.ID) String id) throws RmesException {
		String items = classificationItemService.getClassificationItems(id);
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}
	
	@GetMapping(value="/classification/{id}/levels", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevels(@PathVariable(Constants.ID) String id) throws RmesException {
		String classificationLevels = classificationsService.getClassificationLevels(id);
		return ResponseEntity.status(HttpStatus.OK).body(classificationLevels);
	}
	
	@GetMapping(value="/classification/{id}/level/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevel(@PathVariable(Constants.ID) String id, @PathVariable("levelId") String levelId) throws RmesException {
		String classificationLevel =  classificationsService.getClassificationLevel(id, levelId);
		return ResponseEntity.status(HttpStatus.OK).body(classificationLevel);
	}
	
	@GetMapping(value="/classification/{classificationId}/level/{levelId}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevelMembers(@PathVariable("classificationId") String classificationId, @PathVariable("levelId") String levelId) throws RmesException {
		String levelMembers = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		return ResponseEntity.status(HttpStatus.OK).body(levelMembers);
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItem(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId) throws RmesException {
		String classificationItem = classificationItemService.getClassificationItem(classificationId, itemId);
		return ResponseEntity.status(HttpStatus.OK).body(classificationItem);
	}

	@PutMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateClassificationItem(
			@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId,
			@Parameter(description = "Classification to update", required = true, content = @Content(schema = @Schema(implementation = ClassificationItem.class))) @org.springframework.web.bind.annotation.RequestBody String body) throws RmesException {
		classificationItemService.updateClassificationItem(classificationId, itemId, body);
		return ResponseEntity.status(HttpStatus.OK).body(itemId);
	}
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNotes(@PathVariable("classificationId") String classificationId,
			@PathVariable("itemId") String itemId, @PathVariable("conceptVersion") int conceptVersion) throws RmesException {
		String notes = classificationItemService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		return ResponseEntity.status(HttpStatus.OK).body(notes);
	}
	
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/narrowers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNarrowers(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId) throws RmesException {
		String narrowers = classificationItemService.getClassificationItemNarrowers(classificationId, itemId);
		return ResponseEntity.status(HttpStatus.OK).body(narrowers);
	}
	
	@GetMapping(value="/correspondences", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondences() throws RmesException {
		String correspondences = classificationsService.getCorrespondences();
		return ResponseEntity.status(HttpStatus.OK).body(correspondences);
	}
	
	@GetMapping(value="/correspondence/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondence(@PathVariable(Constants.ID) String id) throws RmesException {
		String correspondence = classificationsService.getCorrespondence(id);
		return ResponseEntity.status(HttpStatus.OK).body(correspondence);
	}
	
	@GetMapping(value="/correspondence/{id}/associations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceAssociations(@PathVariable(Constants.ID) String id) throws RmesException {
		String associations = classificationsService.getCorrespondenceAssociations(id);
		return ResponseEntity.status(HttpStatus.OK).body(associations);
	}
	
	@GetMapping(value="/correspondence/{correspondenceId}/association/{associationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceItem(@PathVariable("correspondenceId") String correspondenceId,
			@PathVariable("associationId") String associationId) throws RmesException {
		String association = classificationsService.getCorrespondenceAssociation(correspondenceId, associationId);
		return ResponseEntity.status(HttpStatus.OK).body(association);
	}

}
