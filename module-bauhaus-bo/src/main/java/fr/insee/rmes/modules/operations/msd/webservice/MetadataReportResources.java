package fr.insee.rmes.modules.operations.msd.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.commons.configuration.swagger.model.Accept;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.NotFoundAttributeException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import fr.insee.rmes.modules.operations.msd.webservice.response.DocumentationAttributeResponse;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.utils.XMLUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Qualifier("Report")
@RestController
@RequestMapping("/operations")
@ConditionalOnModule("operations")
public class MetadataReportResources {

	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	protected final DocumentationService documentationService;

	public MetadataReportResources(OperationsService operationsService, OperationsDocumentationsService documentationsService, DocumentationService documentationService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
        this.documentationService = documentationService;
    }


	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(
			value = "/metadataStructureDefinition",
			produces = {
					MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE
			}
	)
	public ResponseEntity<Object> getMSD(@RequestHeader(required=false) String accept) throws RmesException {
		Accept acceptHeader = Accept.fromMediaType(accept);
		return switch (acceptHeader) {
			case XML -> {
				var msd = documentationsService.getMSD();
				yield ResponseEntity.ok(XMLUtils.produceResponse(msd, String.valueOf(accept)));
			}
			case JSON -> {
				var jsonResultat = documentationsService.getMSDJson();
				yield ResponseEntity.ok(jsonResultat);
			}
			default -> ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
		};
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataAttribute/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public DocumentationAttribute getMetadataAttribute(@PathVariable(Constants.ID) String id) throws RmesException, NotFoundAttributeException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
		return documentationService.getMetadataAttribute(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataAttributes", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
	public ResponseEntity<List<DocumentationAttributeResponse>> getMetadataAttributes() throws RmesException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
		List<DocumentationAttribute> attributes = documentationService.getMetadataAttributes();

		List<DocumentationAttributeResponse> responses = attributes.stream()
			.map(attribute -> {
				var response = DocumentationAttributeResponse.fromDomain(attribute);
				response.add(linkTo(MetadataReportResources.class).slash("metadataAttribute").slash(attribute.id()).withSelfRel());
				return response;
			})
			.toList();

		return ResponseEntity.ok()
			.contentType(org.springframework.hateoas.MediaTypes.HAL_JSON)
			.body(responses);
	}


	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getMetadataReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String metadataReport = documentationsService.getMetadataReport(id);
		return ResponseEntity.ok(metadataReport);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/default", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getMetadataReportDefaultValue() throws IOException {
		return ResponseEntity.ok(documentationsService.getMetadataReportDefaultValue());
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/fullSims/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Object> getFullSims(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept) throws RmesException {
		Accept acceptHeader = Accept.fromMediaType(accept);
		return switch (acceptHeader) {
			case XML -> {
				var documentation = documentationsService.getFullSimsForXml(id);
				yield ResponseEntity.ok(XMLUtils.produceResponse(documentation, accept));
			}
			case JSON -> {
				var jsonResultat = documentationsService.getFullSimsForJson(id);
				yield ResponseEntity.ok(jsonResultat);
			}
			default -> ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
		};
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/Owner/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getMetadataReportOwner(@PathVariable(Constants.ID) String id) throws RmesException {
		String metadataReportOwner = documentationsService.getMetadataReportOwner(id);
		return ResponseEntity.status(HttpStatus.OK).body(metadataReportOwner);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value = "/metadataReport", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> setMetadataReport(@RequestBody String body) throws RmesException {
		String id = documentationsService.createMetadataReport(body);
		if (id == null) {return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(id);}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value = "/metadataReport/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> setMetadataReportById(
			@PathVariable(Constants.ID) String id,
			@RequestBody String body) throws RmesException {
		documentationsService.setMetadataReport(id, body);
		return ResponseEntity.noContent().build();
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.DELETE)
	@DeleteMapping(value = "/metadataReport/delete/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> deleteMetadataReportById(
			@PathVariable(Constants.ID) String id) throws RmesException {
		HttpStatus result = documentationsService.deleteMetadataReport(id);
		return ResponseEntity.status(result.value()).build();
	}



	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.PUBLISH)
	@PutMapping(value = "/metadataReport/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> setSimsValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		documentationsService.publishMetadataReport(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);

	}


	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	public ResponseEntity<?> getSimsExport(
			@PathVariable(Constants.ID) String id,
			@RequestParam(name = "emptyMas", defaultValue = "true") boolean includeEmptyMas,
			@RequestParam(name = "lg1", defaultValue = "true")  boolean lg1,
			@RequestParam(name = "lg2", defaultValue = "true")  boolean lg2,
			@RequestParam(name = "document", defaultValue = "true")  boolean document
			) throws RmesException {


		return documentationsService.exportMetadataReport(id,includeEmptyMas,lg1,lg2, document);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/export/label/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	public ResponseEntity<?> getSimsExportForLabel(@PathVariable(Constants.ID) String id) throws RmesException {

		return documentationsService.exportMetadataReportForLabel(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/export/{id}/tempFiles", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	public ResponseEntity<Object> getSimsExportFiles(
            @PathVariable(Constants.ID) String id,
			@RequestParam(name = "emptyMas", defaultValue = "true") boolean includeEmptyMas,
			@RequestParam(name = "lg1", defaultValue = "true")  boolean lg1,
			@RequestParam(name = "lg2", defaultValue = "true")  boolean lg2
		) throws RmesException {
		return documentationsService.exportMetadataReportTempFiles(id,includeEmptyMas,lg1,lg2);
	}
}
