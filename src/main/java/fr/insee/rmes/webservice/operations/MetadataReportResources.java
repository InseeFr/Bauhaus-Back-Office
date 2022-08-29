package fr.insee.rmes.webservice.operations;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.operations.documentation.Attribute;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@Qualifier("Report")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
public class MetadataReportResources extends OperationsCommonResources {

	
	/***************************************************************************************************
	 * DOCUMENTATION
	 ******************************************************************************************************/

	@GetMapping(value = "/metadataStructureDefinition", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMsd", summary = "Metadata structure definition", 
	responses = { @ApiResponse(content = @Content(/*mediaType = "application/json",*/ schema = @Schema(implementation = MAS.class)))})
	public ResponseEntity<Object> getMSD(
			@Parameter(hidden = true) @RequestHeader(required=false) String accept
			) {
		MSD msd ;
		String jsonResultat = null ;

		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				msd = documentationsService.getMSD();
			} catch (RmesException e) {
				return returnRmesException(e);
			}
			return ResponseEntity.ok(XMLUtils.produceResponse(msd, accept));
		}

		else {
			try {
				jsonResultat = documentationsService.getMSDJson();
			} catch (RmesException e) {
				return returnRmesException(e);
			}
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		}
	}

	@GetMapping(value = "/metadataAttribute/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMA", summary = "Metadata attribute specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Attribute.class)))})
	public ResponseEntity<Object> getMetadataAttribute(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = documentationsService.getMetadataAttribute(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/metadataAttributes", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMAs", summary = "Metadata attributes specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type="array",implementation = Attribute.class)))})
	public ResponseEntity<Object> getMetadataAttributes() {
		String jsonResultat;
		try {
			jsonResultat = documentationsService.getMetadataAttributes();
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}


	@GetMapping(value = "/metadataReport/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Metadata report for an id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getMetadataReport(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = documentationsService.getMetadataReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/metadataReport/default", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReportDefaultValue", summary = "Get default value for metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getMetadataReportDefaultValue() throws IOException {
		return ResponseEntity.status(HttpStatus.OK).body(documentationsService.getMetadataReportDefaultValue());
	}

	@GetMapping(value = "/metadataReport/fullSims/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFullSims", summary = "Full sims for an id", 
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getFullSims(
			@Parameter(
					description = "Identifiant de la documentation (format : [0-9]{4})",
					required = true,
					schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept
			) {
		Documentation fullsims;
		String jsonResultat;
		
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				fullsims = documentationsService.getFullSimsForXml(id);
			} catch (RmesException e) {
				return returnRmesException(e);
			}

			return ResponseEntity.ok(XMLUtils.produceResponse(fullsims, accept));
		}

		else {
			try {
				jsonResultat = documentationsService.getFullSimsForJson(id);
			} catch (RmesException e) {
				return returnRmesException(e);
			}
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		}
		
		
		
	
	}

	/**
	 * GET
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/metadataReport/Owner/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Owner stamp for a Metadata report's id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public ResponseEntity<Object> getMetadataReportOwner(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = documentationsService.getMetadataReportOwner(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() || @AuthorizeMethodDecider.isIndicatorContributor() || @AuthorizeMethodDecider.isSeriesContributor()")
	@PostMapping(value = "/metadataReport", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReport", summary = "Create metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))})
	public ResponseEntity<Object> setMetadataReport(
			@Parameter(description = "Metadata report to create", required = true,
	content = @Content(schema = @Schema(implementation = Documentation.class))) @RequestBody String body) {
		logger.info("POST Metadata report");
		String id = null;
		try {
			id = documentationsService.createMetadataReport(body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		if (id == null) {return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(id);}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() "
			+ "|| @AuthorizeMethodDecider.isCnis()")
	@PutMapping(value = "/metadataReport/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReportById", summary = "Update metadata report")
	public ResponseEntity<Object> setMetadataReportById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Report to update", required = true,
			content = @Content(schema = @Schema(implementation = Documentation.class))) @RequestBody String body) {
		try {
			documentationsService.setMetadataReport(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * DELETE
	 * @param id
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")
	@DeleteMapping(value = "/metadataReport/delete/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "deleteMetadataReportById", summary = "Delete metadata report")
	public ResponseEntity<Object> deleteMetadataReportById(
			@PathVariable(Constants.ID) String id) {
		HttpStatus result=HttpStatus.NO_CONTENT;
		try {
			result = documentationsService.deleteMetadataReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(result.value()).build();
	}



	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */	
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PutMapping(value = "/metadataReport/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReportValidation", summary = "Sims validation")
	public ResponseEntity<Object> setSimsValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			documentationsService.publishMetadataReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	/**
	 * EXPORT
	 * @param id
	 * @param lg1
	 * @param lg2
	 * @param includeEmptyMas
	 * @return response
	 */	
	@GetMapping(value = "/metadataReport/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExport", summary = "Produce a document with a metadata report")
	public ResponseEntity<?> getSimsExport(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id
			,
			@Parameter(
					description = "Inclure les champs vides",
					required = false)  @RequestParam("emptyMas") Boolean includeEmptyMas
			,
			@Parameter(
					description = "Version française",
					required = false) @RequestParam("lg1")  Boolean lg1
			,
			@Parameter(
					description = "Version anglaise",
					required = false) @RequestParam("lg2")  Boolean lg2,

			@Parameter(
					description = "With documents",
					required = false) @RequestParam("document")  Boolean document
			) throws RmesException {
		if (includeEmptyMas==null) {includeEmptyMas=true;}
		if (lg1==null) {lg1=true;}
		if (lg2==null) {lg2=true;}
		if (document==null) {document=true;}
		return documentationsService.exportMetadataReport(id,includeEmptyMas,lg1,lg2, document);
	}

	/**
	 * EXPORT FOR LABEL
	 * @param id
	 * @return response
	 */	
	@GetMapping(value = "/metadataReport/export/label/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExportLabel", summary = "Produce a document with a metadata report")
	public ResponseEntity<?> getSimsExportForLabel(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id
			) throws RmesException {

		return documentationsService.exportMetadataReportForLabel(id);	
	}
	
	/**
	 * EXPORT xml files used to produce the final odt file
	 * @param id
	 * @param lg1
	 * @param lg2
	 * @param includeEmptyMas
	 * @return response
	 */	
	@GetMapping(value = "/metadataReport/export/{id}/tempFiles", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExportFiles", summary = "Get xml files used to produce a document with a metadata report")
	public ResponseEntity<Object> getSimsExportFiles(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathVariable(Constants.ID) String id
			,
			@Parameter(
					description = "Inclure les champs vides",
					required = false)  @RequestParam("emptyMas") Boolean includeEmptyMas
			,
			@Parameter(
					description = "Version française",
					required = false) @RequestParam("lg1")  Boolean lg1
			,
			@Parameter(
					description = "Version anglaise",
					required = false) @RequestParam("lg2")  Boolean lg2
			) throws RmesException {
		if (includeEmptyMas==null) {includeEmptyMas=true;}
		if (lg1==null) {lg1=true;}
		if (lg2==null) {lg2=true;}
		try {
			return documentationsService.exportMetadataReportTempFiles(id,includeEmptyMas,lg1,lg2);
		} catch (RmesException e) {
			return ResponseEntity.badRequest().build();
					}

	}

}
