package fr.insee.rmes.modules.classifications.nomenclatures.webservice;

import fr.insee.rmes.bauhaus_services.classifications.ClassificationsService;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.config.swagger.model.Id;
import fr.insee.rmes.config.swagger.model.classifications.FamilyClass;
import fr.insee.rmes.config.swagger.model.classifications.Members;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.classifications.families.model.PartialClassificationFamily;
import fr.insee.rmes.modules.classifications.nomenclatures.model.Classification;
import fr.insee.rmes.modules.classifications.nomenclatures.model.ClassificationItem;
import fr.insee.rmes.modules.classifications.nomenclatures.model.PartialClassification;
import fr.insee.rmes.modules.classifications.series.model.PartialClassificationSeries;
import fr.insee.rmes.modules.classifications.nomenclatures.webservice.response.PartialClassificationFamilyResponse;
import fr.insee.rmes.modules.classifications.nomenclatures.webservice.response.PartialClassificationSeriesResponse;
import fr.insee.rmes.modules.classifications.nomenclatures.webservice.response.PartialClassificationResponse;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


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


	final
	ClassificationsService classificationsService;

	final
	ClassificationItemService classificationItemService;

	public ClassificationsResources(ClassificationsService classificationsService, ClassificationItemService classificationItemService) {
		this.classificationsService = classificationsService;
		this.classificationItemService = classificationItemService;
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_FAMILY, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/families", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	@Operation(summary = "List of classification families",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=PartialClassificationFamilyResponse.class))))})
	public ResponseEntity<List<PartialClassificationFamilyResponse>> getFamilies() throws RmesException {
		List<PartialClassificationFamily> families = classificationsService.getFamilies();

		List<PartialClassificationFamilyResponse> responses = families.stream()
			.map(family -> {
				var response = PartialClassificationFamilyResponse.fromDomain(family);
				response.add(linkTo(ClassificationsResources.class).slash("family").slash(family.id()).withSelfRel());
				return response;
			})
			.toList();

		return ResponseEntity.ok()
			.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
			.body(responses);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_FAMILY, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/family/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Classification family",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = FamilyClass.class)))})
	public ResponseEntity<Object> getFamily(@PathVariable(Constants.ID) String id) throws RmesException {
		String family = classificationsService.getFamily(id);
		return ResponseEntity.status(HttpStatus.OK).body(family);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_FAMILY, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/family/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Members of family",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public ResponseEntity<Object> getFamilyMembers(@PathVariable(Constants.ID) String id) throws RmesException {
		String familyMembers = classificationsService.getFamilyMembers(id);
		return ResponseEntity.status(HttpStatus.OK).body(familyMembers);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_SERIES, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/series", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	@Operation(summary = "List of classification series",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=PartialClassificationSeriesResponse.class))))})
	public ResponseEntity<List<PartialClassificationSeriesResponse>> getSeries() throws RmesException {
		List<PartialClassificationSeries> series = classificationsService.getSeries();

		List<PartialClassificationSeriesResponse> responses = series.stream()
			.map(s -> {
				var response = PartialClassificationSeriesResponse.fromDomain(s);
				response.add(linkTo(ClassificationsResources.class).slash("series").slash(s.id()).withSelfRel());
				return response;
			})
			.toList();

		return ResponseEntity.ok()
			.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
			.body(responses);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_SERIES, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/series/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getOneSeries(@PathVariable(Constants.ID) String id) throws RmesException {
		String serie = classificationsService.getOneSeries(id);
		return ResponseEntity.status(HttpStatus.OK).body(serie);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_SERIES, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/series/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Members of series",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Members.class))))})
	public ResponseEntity<Object> getSeriesMembers(@PathVariable(Constants.ID) String id) throws RmesException {
		String seriesMembers = classificationsService.getSeriesMembers(id);
		return ResponseEntity.status(HttpStatus.OK).body(seriesMembers);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="",produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	@Operation(summary = "List of classifications",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=PartialClassificationResponse.class))))})
	public ResponseEntity<List<PartialClassificationResponse>> getClassifications() throws RmesException {
		List<PartialClassification> classifications = classificationsService.getClassifications();

		List<PartialClassificationResponse> responses = classifications.stream()
			.map(classification -> {
				var response = PartialClassificationResponse.fromDomain(classification);
				response.add(linkTo(ClassificationsResources.class).slash("classification").slash(classification.id()).withSelfRel());
				return response;
			})
			.toList();

		return ResponseEntity.ok()
			.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
			.body(responses);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)

	@GetMapping(value="/classification/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassification(@PathVariable(Constants.ID) String id) throws RmesException {
		String classification = classificationsService.getClassification(id);
		return ResponseEntity.status(HttpStatus.OK).body(classification);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value="/classification/{id}")
	@io.swagger.v3.oas.annotations.Operation(summary = "Update an existing classification" )
	public ResponseEntity<Id> updateClassification(
			@PathVariable(Constants.ID) Id id,
			@Parameter(description = "Classification to update", required = true, content = @Content(schema = @Schema(implementation = Classification.class))) @org.springframework.web.bind.annotation.RequestBody String body) throws RmesException {
		classificationsService.updateClassification(id.identifier(), body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.PUBLISH)
	@PutMapping(value="/classification/{id}/validate")
	@io.swagger.v3.oas.annotations.Operation(summary = "Publish a classification")
	public ResponseEntity<Id> publishClassification(
			@PathVariable(Constants.ID) Id id) throws RmesException {
		classificationsService.setClassificationValidation(id.identifier());
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/classification/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItems(@PathVariable(Constants.ID) String id) throws RmesException {
		String items = classificationItemService.getClassificationItems(id);
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
		@GetMapping(value="/classification/{id}/levels", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevels(@PathVariable(Constants.ID) String id) throws RmesException {
		String classificationLevels = classificationsService.getClassificationLevels(id);
		return ResponseEntity.status(HttpStatus.OK).body(classificationLevels);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/classification/{id}/level/{levelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevel(@PathVariable(Constants.ID) String id, @PathVariable("levelId") String levelId) throws RmesException {
		String classificationLevel =  classificationsService.getClassificationLevel(id, levelId);
		return ResponseEntity.status(HttpStatus.OK).body(classificationLevel);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/classification/{classificationId}/level/{levelId}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationLevelMembers(@PathVariable("classificationId") String classificationId, @PathVariable("levelId") String levelId) throws RmesException {
		String levelMembers = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		return ResponseEntity.status(HttpStatus.OK).body(levelMembers);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItem(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId) throws RmesException {
		String classificationItem = classificationItemService.getClassificationItem(classificationId, itemId);
		return ResponseEntity.status(HttpStatus.OK).body(classificationItem);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value="/classification/{classificationId}/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateClassificationItem(
			@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId,
			@Parameter(description = "Classification to update", required = true, content = @Content(schema = @Schema(implementation = ClassificationItem.class))) @org.springframework.web.bind.annotation.RequestBody String body) throws RmesException {
		classificationItemService.updateClassificationItem(classificationId, itemId, body);
		return ResponseEntity.status(HttpStatus.OK).body(itemId);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNotes(@PathVariable("classificationId") String classificationId,
			@PathVariable("itemId") String itemId, @PathVariable("conceptVersion") int conceptVersion) throws RmesException {
		String notes = classificationItemService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		return ResponseEntity.status(HttpStatus.OK).body(notes);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/classification/{classificationId}/item/{itemId}/narrowers", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getClassificationItemNarrowers(@PathVariable("classificationId") String classificationId, @PathVariable("itemId") String itemId) throws RmesException {
		String narrowers = classificationItemService.getClassificationItemNarrowers(classificationId, itemId);
		return ResponseEntity.status(HttpStatus.OK).body(narrowers);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/correspondences", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondences() throws RmesException {
		String correspondences = classificationsService.getCorrespondences();
		return ResponseEntity.status(HttpStatus.OK).body(correspondences);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/correspondence/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondence(@PathVariable(Constants.ID) String id) throws RmesException {
		String correspondence = classificationsService.getCorrespondence(id);
		return ResponseEntity.status(HttpStatus.OK).body(correspondence);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/correspondence/{id}/associations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceAssociations(@PathVariable(Constants.ID) String id) throws RmesException {
		String associations = classificationsService.getCorrespondenceAssociations(id);
		return ResponseEntity.status(HttpStatus.OK).body(associations);
	}

	@HasAccess(module = RBAC.Module.CLASSIFICATION_CLASSIFICATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/correspondence/{correspondenceId}/association/{associationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getCorrespondenceItem(@PathVariable("correspondenceId") String correspondenceId,
			@PathVariable("associationId") String associationId) throws RmesException {
		String association = classificationsService.getCorrespondenceAssociation(correspondenceId, associationId);
		return ResponseEntity.status(HttpStatus.OK).body(association);
	}

}
