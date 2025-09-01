package fr.insee.rmes.onion.infrastructure.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.swagger.model.Accept;
import fr.insee.rmes.config.swagger.model.operations.documentation.Attribute;
<<<<<<<< HEAD:module-bauhaus-bo/src/main/java/fr/insee/rmes/onion/infrastructure/webservice/operations/MetadataReportResources.java
import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.onion.domain.exceptions.GenericInternalServerException;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.exceptions.operations.NotFoundAttributeException;
import fr.insee.rmes.onion.domain.exceptions.operations.OperationDocumentationRubricWithoutRangeException;
========
import fr.insee.rmes.onion.domain.exceptions.GenericInternalServerException;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.exceptions.operations.NotFoundAttributeException;
import fr.insee.rmes.onion.domain.exceptions.operations.OperationDocumentationRubricWithoutRangeException;
import fr.insee.rmes.onion.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MAS;
>>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/main/java/fr/insee/rmes/onion/infrastructure/webservice/operations/MetadataReportResources.java
import fr.insee.rmes.onion.domain.port.clientside.DocumentationService;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.utils.XMLUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@Qualifier("Report")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
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
	@Operation(
			summary = "Metadata structure definition",
			responses = {
					@ApiResponse(content = @Content(schema = @Schema(implementation = MAS.class)))
			}
	)
	public ResponseEntity<Object> getMSD(
			@Parameter(hidden = true) @RequestHeader(required=false) String accept
			) throws RmesException {
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
	@Operation(summary = "Metadata attribute specification and property",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Attribute.class)))})
	public DocumentationAttribute getMetadataAttribute(@PathVariable(Constants.ID) String id) throws RmesException, NotFoundAttributeException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
		return documentationService.getMetadataAttribute(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Metadata attributes specification and property",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type="array",implementation = Attribute.class)))})
	public List<DocumentationAttribute> getMetadataAttributes() throws RmesException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
		return documentationService.getMetadataAttributes();
	}


	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Metadata report for an id",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getMetadataReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String metadataReport = documentationsService.getMetadataReport(id);
		return ResponseEntity.ok(metadataReport);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/default", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get default value for metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getMetadataReportDefaultValue() throws IOException {
		return ResponseEntity.ok(documentationsService.getMetadataReportDefaultValue());
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/fullSims/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@Operation(summary = "Full sims for an id",
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getFullSims(
			@Parameter(
					description = "Identifiant de la documentation (format : [0-9]{4})",
					required = true,
					schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept
			) throws RmesException {
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
	@Operation(summary = "Owner stamp for a Metadata report's id",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getMetadataReportOwner(@PathVariable(Constants.ID) String id) throws RmesException {
		String metadataReportOwner = documentationsService.getMetadataReportOwner(id);
		return ResponseEntity.status(HttpStatus.OK).body(metadataReportOwner);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value = "/metadataReport", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))})
	public ResponseEntity<Object> setMetadataReport(
			@Parameter(description = "Metadata report to create", required = true,
	content = @Content(schema = @Schema(implementation = Documentation.class))) @RequestBody String body) throws RmesException {
		String id = documentationsService.createMetadataReport(body);
		if (id == null) {return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(id);}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value = "/metadataReport/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update metadata report")
	public ResponseEntity<Object> setMetadataReportById(
			@PathVariable(Constants.ID) String id,
			@Parameter(description = "Report to update", required = true,
			content = @Content(schema = @Schema(implementation = Documentation.class))) @RequestBody String body) throws RmesException {
		documentationsService.setMetadataReport(id, body);
		return ResponseEntity.noContent().build();
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.DELETE)
	@DeleteMapping(value = "/metadataReport/delete/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Delete metadata report")
	public ResponseEntity<Object> deleteMetadataReportById(
			@PathVariable(Constants.ID) String id) throws RmesException {
		HttpStatus result = documentationsService.deleteMetadataReport(id);
		return ResponseEntity.status(result.value()).build();
	}



	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.PUBLISH)
	@PutMapping(value = "/metadataReport/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Sims validation")
	public ResponseEntity<String> setSimsValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		documentationsService.publishMetadataReport(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);

	}


	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(summary = "Produce a document with a metadata report")
	public ResponseEntity<?> getSimsExport(
			@Parameter(
				description = "Identifiant de la documentation (format : [0-9]{4})",
				required = true,
				schema = @Schema(pattern = "[0-9]{4}", type = "string")
			)
			@PathVariable(Constants.ID) String id,

			@Parameter(description = "Inclure les champs vides")
			@RequestParam(name = "emptyMas", defaultValue = "true") boolean includeEmptyMas,

			@Parameter(description = "Version française")
			@RequestParam(name = "lg1", defaultValue = "true")  boolean lg1,

			@Parameter(description = "Version anglaise")
			@RequestParam(name = "lg2", defaultValue = "true")  boolean lg2,

			@Parameter(description = "With documents")
			@RequestParam(name = "document", defaultValue = "true")  boolean document
			) throws RmesException {


		return documentationsService.exportMetadataReport(id,includeEmptyMas,lg1,lg2, document);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/export/label/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(summary = "Produce a document with a metadata report")
	public ResponseEntity<?> getSimsExportForLabel(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id
			) throws RmesException {

		return documentationsService.exportMetadataReportForLabel(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_SIMS, privilege = RBAC.Privilege.READ)
	@GetMapping(value = "/metadataReport/export/{id}/tempFiles", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(summary = "Get xml files used to produce a document with a metadata report")
	public ResponseEntity<Object> getSimsExportFiles(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id
			,
			@Parameter(
					description = "Inclure les champs vides"
            )
			@RequestParam(name = "emptyMas", defaultValue = "true") boolean includeEmptyMas,
			@Parameter(
					description = "Version française"
            )
			@RequestParam(name = "lg1", defaultValue = "true")  boolean lg1,
			@Parameter(
					description = "Version anglaise"
            )
			@RequestParam(name = "lg2", defaultValue = "true")  boolean lg2
		) throws RmesException {
		return documentationsService.exportMetadataReportTempFiles(id,includeEmptyMas,lg1,lg2);
	}
}
