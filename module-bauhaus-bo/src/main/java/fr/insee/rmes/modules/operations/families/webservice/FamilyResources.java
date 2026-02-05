package fr.insee.rmes.modules.operations.families.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.domain.port.clientside.FamilyService;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.operations.series.webservice.SeriesResources;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.webservice.response.operations.OperationFamilyResponse;
import fr.insee.rmes.webservice.response.operations.OperationFamilySeriesResponse;
import fr.insee.rmes.webservice.response.operations.OperationFamilySubjectResponse;
import fr.insee.rmes.webservice.response.operations.PartialOperationFamilyResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Qualifier("Family")
@RestController
@RequestMapping(
		value = "/operations",
		produces = {
				"application/hal+json",
				MediaType.APPLICATION_JSON_VALUE,
				MediaType.TEXT_PLAIN_VALUE
		}
)
@ConditionalOnModule("operations")
public class FamilyResources  {

	protected final OperationsService operationsService;
	protected final FamilyService familyService;
	protected final OperationsDocumentationsService documentationsService;

	public FamilyResources(OperationsService operationsService, FamilyService familyService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
        this.familyService = familyService;
        this.documentationsService = documentationsService;
	}


	@GetMapping("/families")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	public ResponseEntity<List<PartialOperationFamilyResponse>> getFamilies() throws RmesException {
		List<PartialOperationFamily> families = familyService.getFamilies();

        List<PartialOperationFamilyResponse> responses = families.stream()
                .map(family -> {
                    var response = PartialOperationFamilyResponse.fromDomain(family);
                    response.add(linkTo(FamilyResources.class).slash("family").slash(family.id()).withSelfRel());
                    return response;
                })
                .toList();
		
		return ResponseEntity.ok()
				.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
				.body(responses);
	}

	@GetMapping("/family/{id}")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	public OperationFamilyResponse getFamilyByID(@PathVariable(Constants.ID) String id) throws RmesException {
		var family =  familyService.getFamily(id);
        return OperationFamilyResponse.fromDomain(
                family,
                family.series().stream().map(series -> {
                    var response = OperationFamilySeriesResponse.fromDomain(series);
                    response.add(linkTo(SeriesResources.class).slash("series").slash(series.id()).withSelfRel());
                    return response;
                }).toList(),
                family.subjects().stream().map(OperationFamilySubjectResponse::fromDomain).toList()

        );
	}

	@GetMapping("/families/advanced-search")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	public ResponseEntity<Object> getFamiliesForSearch() throws RmesException {
		String families = operationsService.getFamiliesForSearch();
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(families);
	}

	@GetMapping("/families/{id}/seriesWithReport")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	public ResponseEntity<Object> getSeriesWithReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String series = operationsService.getSeriesWithReport(id);
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(series);
	}


	@PutMapping("/family/{id}")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.UPDATE)
	public ResponseEntity<Object> setFamilyById(
			@PathVariable(Constants.ID) String id, 
			@RequestBody String body) throws RmesException {
		operationsService.setFamily(id, body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	@PostMapping("/family")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.CREATE)
	public ResponseEntity<Object> createFamily(@RequestBody String body) throws RmesException {
		String id = operationsService.createFamily(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.PUBLISH)
	@PutMapping("/family/{id}/validate")
	public ResponseEntity<Object> setFamilyValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		operationsService.setFamilyValidation(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
}
