package fr.insee.rmes.webservice.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import fr.insee.rmes.utils.ExportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.w3c.dom.Document;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import static fr.insee.rmes.utils.ExportUtils.getExtension;

@Qualifier("Operation")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
public class OperationsResources extends OperationsCommonResources {

	@Autowired
	ExportUtils exportUtils;

	/***************************************************************************************************
	 * OPERATIONS
	 ******************************************************************************************************/

	@GetMapping(value = "/operations", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperations", summary = "List of operations", responses = {
			@ApiResponse(content = @Content(schema = @Schema(type = "array", implementation = IdLabelAltLabel.class))) })
	public ResponseEntity<Object> getOperations() throws RmesException {
		String jsonResultat = operationsService.getOperations();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);

	}

	@GetMapping(value = "/operation/{id}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationByID", summary = "Operation", responses = {
			@ApiResponse(content = @Content(/* mediaType = "application/json", */ schema = @Schema(implementation = Operation.class))) })
	public ResponseEntity<Object> getOperationByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat = XMLUtils.produceXMLResponse(operationsService.getOperationById(id));
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		} else {
			try {
				resultat = operationsService.getOperationJsonByID(id);
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultat);
	}

	@PostMapping(value="/operation/codebook",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,"application/vnd.oasis.opendocument.text" },
			produces = {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_OCTET_STREAM_VALUE,"application/vnd.oasis.opendocument.text" }
	)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getCodeBook", summary = "Produce a codebook from a DDI")
	
	public  ResponseEntity<?> getCodeBook( 
			
			@Parameter(schema = @Schema(type = "string", format = "String", description = "Accept"))
			@RequestHeader(required=false) String accept, 			

			@Parameter(schema = @Schema(type = "string", format = "binary", description = "file in DDI"))
			@RequestParam(value="file") MultipartFile isDDI, // InputStream isDDI,
			
			@Parameter(schema = @Schema(type = "string", format = "binary", description = "file for structure"))
			@RequestParam(value = "dicoVar") MultipartFile isCodeBook //InputStream isCodeBook
			 
			) 
				throws RmesException {
		logger.info("Generate CodeBook from DDI {}, {}", isDDI.getOriginalFilename(), isCodeBook.getOriginalFilename());
		String ddi;
		File codeBookFile;
		try {
			ddi = new String(isDDI.getBytes(), StandardCharsets.UTF_8);
			codeBookFile = FilesUtils.streamToFile(isCodeBook.getInputStream(), "dicoVar",".odt");
		} catch (IOException e1) {
			throw new RmesException(HttpStatus.BAD_REQUEST, e1.getMessage(), "Files can't be read");
		}
		ResponseEntity<Resource> response;
		
		try {
			response=operationsService.getCodeBookExport(ddi,codeBookFile,accept);
			logger.debug("Codebook is generated");
		} 
		catch (RmesException e) {
			logger.error("Failed to generate codebook {}", e.getMessageAndDetails());
			return returnRmesException(e);
		}		
		return response;	
	}

	@PostMapping(value="/operation/codebook/V2",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
			produces = {MediaType.MULTIPART_FORM_DATA_VALUE,"application/vnd.oasis.opendocument.text" }
	)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getCodeBook", summary = "Produce a codebook from a DDI")

	public  ResponseEntity<?> getCodeBookV2(

			@Parameter(schema = @Schema(type = "string", format = "String", description = "Accept"))
			@RequestHeader(required=false) String accept,

			@Parameter(schema = @Schema(type = "string", allowableValues = { "concis" , "concis avec expression" , "scindable" , "non scindable" }))
			@RequestParam(value = "dicoVar") String isCodeBook, //InputStream isCodeBook,

			@RequestParam(value="file") MultipartFile isDDI // InputStream isDDI,

			// 1. selectionner le fichier ddi , lui appliquer une transfo pour oter le namespace et créer un nouveau fichier issu de la transfo

			// 2. prendre le fichier précedent et passer la transfo pour checker. Si le fichier contient ok on continue la suite sinon on arrete et on exporte le fichier issu de la transfo

			// 3. si ok précédemment, on applique la méthode  exportUtils.exportAsResponse("export.odt", xmlContent,xslFile,xmlPatternRmes,zipRmes, "dicoVariable");
			// avec xmlContent= fichier resultat du 1. , xslFile = dico_codes.xsl , xmlPatternRmes= un fichier issu du enum du swagger (iscodebook), zipRmes=xslTransformerFiles/dicoCodes/toZipForDicoCodes.zip


	)
			throws Exception {

		String DDI= new String(isDDI.getBytes(), StandardCharsets.UTF_8);
		File xslRemoveNameSpaces = new File("src\\main\\resources\\xslTransformerFiles\\remove-namespaces.xsl");
		File xslCheckReference = new File("src\\main\\resources\\xslTransformerFiles\\check-references.xsl");
		String dicoCode = new String("/xslTransformerFiles/dico-codes.xsl");
		String zipRmes = "/xslTransformerFiles/dicoCodes/toZipForDicoCodes.zip";

		String xslPatternFile = null;
		switch (isCodeBook) {
			case "concis":
				String xmlFileConcis = "/xslTransformerFiles/dicoCodes/dicoConcisPatternContent.xml";
				xslPatternFile = xmlFileConcis;
				break;
			case "concis avec expression":
				String xmlFileConcisAvecExpression = "/xslTransformerFiles/dicoCodes/dicoConcisDescrPatternContent.xml";
				xslPatternFile = xmlFileConcisAvecExpression;
				break;
			case "scindable":
				String xmlFileScindable = "/xslTransformerFiles/dicoCodes/dicoScindablePatternContent.xml";
				xslPatternFile = xmlFileScindable;
				break;
			case "non scindable":
				String xmlFileNonScindable = "/xslTransformerFiles/dicoCodes/dicoNonScindablePatternContent.xml";
				xslPatternFile = xmlFileNonScindable;
				break;
			default:
				System.out.println("Choix incorrect");
				break;
		}

		File test = File.createTempFile("test1", ".xml");
		test.deleteOnExit();
		transformerRemoveNameSpaces(DDI,xslRemoveNameSpaces,test);

		File control = File.createTempFile("control", ".xml");
		transformerCheckReference(test,xslCheckReference,control);

		DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
		DocumentBuilder db= dbf.newDocumentBuilder();
		Document doc= db.parse(control);
		String checkResult = doc.getDocumentElement().getNodeName();
		ResponseEntity<Resource> resource = null;
//		if (checkResult!="OK") {
//			return resource.ok(Files.readString(control.toPath()));
//		}
		HashMap<String,String> contentXML= new HashMap<>();
		contentXML.put("ddi-file",Files.readString(test.toPath()));

		return exportUtils.exportAsResponse("export.odt", contentXML,dicoCode,xslPatternFile,zipRmes, "dicoVariable");
	}

	public static void transformerRemoveNameSpaces(String ddi,File xslRemoveNameSpaces, File output) throws Exception{
		TransformerFactory factory = TransformerFactory.newInstance();
		Source stylesheetSource = new StreamSource(xslRemoveNameSpaces);
		Transformer transformer = factory.newTransformer(stylesheetSource);
		Source inputSource = new StreamSource(new StringReader(ddi));
		Result outputResult = new StreamResult(output);
		transformer.transform(inputSource, outputResult);
	}

	public static void transformerCheckReference(File input,File xslRemoveNameSpaces, File output) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Source stylesheetSource = new StreamSource(xslRemoveNameSpaces);
		Transformer transformer = factory.newTransformer(stylesheetSource);
		Source inputSource = new StreamSource(input);
		Result outputResult = new StreamResult(output);
		transformer.transform(inputSource, outputResult);
	}




	/**
	 * UPDATE
	 * 
	 * @param id
	 * @param body
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() " + "|| @AuthorizeMethodDecider.isSeriesContributor() "
			+ "|| @AuthorizeMethodDecider.isCnis()")
	@PutMapping(value = "/operation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationById", summary = "Update operation")
	public ResponseEntity<Object> setOperationById(@PathVariable(Constants.ID) String id,
			@Parameter(description = "Operation to update", required = true, content = @Content(schema = @Schema(implementation = Operation.class))) @RequestBody String body) {
		try {
			operationsService.setOperation(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * CREATE
	 * 
	 * @param body
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() " + "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PostMapping(value = "/operation", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createOperation", summary = "Create operation")
	public ResponseEntity<Object> createOperation(
			@Parameter(description = "Operation to create", required = true, content = @Content(schema = @Schema(implementation = Operation.class))) @RequestBody String body) {
		String id = null;
		try {
			id = operationsService.createOperation(body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	/**
	 * PUBLISH
	 * 
	 * @param id
	 * @return response
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() " + "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PutMapping(value = "/operation/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationValidation", summary = "Operation validation")
	public ResponseEntity<Object> setOperationValidation(@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setOperationValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

}
