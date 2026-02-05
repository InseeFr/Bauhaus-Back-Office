package fr.insee.rmes.modules.operations.indicators.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.utils.XMLUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Qualifier("Indicator")
@RestController
@RequestMapping(value="/operations")
@ConditionalOnModule("operations")
public class IndicatorsResources {
	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	public IndicatorsResources(OperationsService operationsService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicators", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PartialOperationIndicatorResponse>> getIndicators() throws RmesException {
		List<PartialOperationIndicatorResponse> responses = this.operationsService.getIndicators().stream()
				.map(indicator -> {
					var response = PartialOperationIndicatorResponse.fromDomain(indicator);
					response.add(linkTo(IndicatorsResources.class).slash("indicator").slash(indicator.id()).withSelfRel());
					return response;
				})
				.toList();

		return ResponseEntity.ok()
				.contentType(MediaTypes.HAL_JSON)
				.body(responses);
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicators/withSims",produces= MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getIndicatorsWIthSims() throws RmesException {
		String indicators = operationsService.getIndicatorsWithSims();
		return ResponseEntity.status(HttpStatus.OK).body(indicators);
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicators/advanced-search", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getIndicatorsForSearch() throws RmesException {
		String indicators = operationsService.getIndicatorsForSearch();
		return ResponseEntity.status(HttpStatus.OK).body(indicators);

	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicator/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Object> getIndicatorByID(
            @PathVariable(Constants.ID) String id,
			@RequestHeader(required=false) String accept) throws RmesException {

		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			return ResponseEntity.status(HttpStatus.OK).body(XMLUtils.produceXMLResponse(operationsService.getIndicatorById(id)));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(operationsService.getIndicatorJsonByID(id));
		}
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value="/indicator/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> setIndicatorById(
			@PathVariable(Constants.ID) String id, 
			@RequestBody String body) throws RmesException {

		operationsService.setIndicator(id, body);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.PUBLISH)
	@PutMapping(value="/indicator/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> setIndicatorValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {

		operationsService.validateIndicator(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value="/indicator", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> setIndicator(@RequestBody String body) throws RmesException {
		String id =  operationsService.setIndicator(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
}
