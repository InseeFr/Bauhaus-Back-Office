package fr.insee.rmes.modules.operations.operations.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Qualifier("Operation")
@RestController
@RequestMapping("/operations")
@ConditionalOnModule("operations")
public class OperationsResources  {

	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	public OperationsResources(OperationsService operationsService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
	}


	@HasAccess(module = RBAC.Module.OPERATION_OPERATION, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/operations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PartialOperationResponse>> getOperations() throws RmesException {
		List<PartialOperationResponse> responses = this.operationsService.getOperations().stream()
				.map(operation -> {
					var response = PartialOperationResponse.fromDomain(operation);
					response.add(linkTo(OperationsResources.class).slash("operation").slash(operation.id()).withSelfRel());
					return response;
				})
				.toList();

		return ResponseEntity.ok()
				.contentType(MediaTypes.HAL_JSON)
				.body(responses);
	}

	@HasAccess(module = RBAC.Module.OPERATION_OPERATION, privilege = RBAC.Privilege.READ)
	@GetMapping(
			value = "/operation/{id}",
			produces = {
				MediaType.APPLICATION_JSON_VALUE,
				MediaType.APPLICATION_XML_VALUE
			}
	)
	public ResponseEntity<Operation> getOperationByID(@PathVariable(Constants.ID) String id) throws RmesException {
		return ResponseEntity.status(HttpStatus.OK).body(operationsService.getOperationById(id));
	}

	@HasAccess(module = RBAC.Module.OPERATION_OPERATION, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value = "/operation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> setOperationById(@PathVariable(Constants.ID) String id, @RequestBody String body) throws RmesException {

		operationsService.setOperation(id, body);
		return ResponseEntity.noContent().build();
	}

	@HasAccess(module = RBAC.Module.OPERATION_OPERATION, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value = "/operation", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createOperation(@RequestBody String body) throws RmesException {
		String id = operationsService.createOperation(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_OPERATION, privilege = RBAC.Privilege.PUBLISH)
	@PutMapping(value = "/operation/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> setOperationValidation(@PathVariable(Constants.ID) String id) throws RmesException {
		operationsService.setOperationValidation(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

}
