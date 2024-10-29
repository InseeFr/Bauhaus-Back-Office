package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.code_list.DetailedCodeList;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static fr.insee.rmes.bauhaus_services.Constants.GOAL_COMITE_LABEL;
import static fr.insee.rmes.bauhaus_services.Constants.GOAL_RMES;

@Component
public class DocumentationExport {
	static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);

	public static final String DOCUMENTATION = "documentation";
	final ExportUtils exportUtils;
	
	final SeriesUtils seriesUtils;
	
	final OperationsUtils operationsUtils;
	
	final IndicatorsUtils indicatorsUtils;
	
	final ParentUtils parentUtils;
	
	final CodeListService codeListServiceImpl;
	
	final OrganizationsService organizationsServiceImpl;
	
	final DocumentationsUtils documentationsUtils;
	final DocumentsUtils documentsUtils;
	static final String xslFile = "/xslTransformerFiles/sims2fodt.xsl";
	static final String xmlPatternRmes = "/xslTransformerFiles/simsRmes/rmesPatternContent.xml";
	static final String zipRmes = "/xslTransformerFiles/simsRmes/toZipForRmes.zip";
	
	static final String xmlPatternLabel = "/xslTransformerFiles/simsLabel/labelPatternContent.xml";
	static final String zipLabel = "/xslTransformerFiles/simsLabel/toZipForLabel.zip";
	private final int maxLength;

	public DocumentationExport(@Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength, DocumentsUtils documentsUtils, ExportUtils exportUtils, SeriesUtils seriesUtils, OperationsUtils operationsUtils, IndicatorsUtils indicatorsUtils, ParentUtils parentUtils, CodeListService codeListServiceImpl, OrganizationsService organizationsServiceImpl, DocumentationsUtils documentationsUtils) {
		this.exportUtils = exportUtils;
		this.seriesUtils = seriesUtils;
		this.operationsUtils = operationsUtils;
		this.indicatorsUtils = indicatorsUtils;
		this.parentUtils = parentUtils;
		this.codeListServiceImpl = codeListServiceImpl;
		this.organizationsServiceImpl = organizationsServiceImpl;
		this.documentationsUtils = documentationsUtils;
		this.documentsUtils = documentsUtils;
		this.maxLength = maxLength;
	}

	/**
	 *
	 * @param id The identifier of the report we want to export
	 * @param documents a boolean value indicating if we want to include the related documents to the export.
	 *                  If this value is equal to true, the export will be a .ZIP archive. If equal to false,
	 *                  the export will be a .ODT file.
	 */
	public ResponseEntity<Resource> exportAsResponse(String id, Map<String, String> xmlContent, String targetType, boolean includeEmptyFields, boolean lg1,
			boolean lg2, boolean documents, String goal) throws RmesException {

		PatternAndZip patternAndZip = PatternAndZip.of(goal);
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, targetType);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);

		Exporter exporter;
		JSONObject sims = this.documentationsUtils.getDocumentationByIdSims(id);

		if (documents) {
			exporter = (xml, xsl, xmlPattern, zip, documentation) -> exportAsZip(sims, xml, xsl, xmlPattern, zip, documentation );
		} else{
			String fileName = sims.getString(Constants.LABEL_LG1);
			exporter = (xml, xsl, xmlPattern, zip, documentation) -> exportUtils.exportAsODT(fileName, xml, xsl, xmlPattern, zip, documentation );
		}
		return export(exporter, xmlContent, patternAndZip);
	}

	public ResponseEntity<Resource> exportAsZip(JSONObject sims, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
		String simsId = sims.getString("id");
		logger.debug("Begin to download the SIMS {} with its documents", simsId);
		String fileName = sims.getString(Constants.LABEL_LG1);

		try {

			Path directory = Files.createTempDirectory("sims");
			logger.debug("Creating tempory directory {}", directory.toString());
			Path simsDirectory = Files.createDirectory(Path.of(directory.toString(), fileName));
			logger.debug("Creating tempory directory {}", simsDirectory);

			logger.debug("Generating the InputStream for the SIMS {}", simsId);

			InputStream input = exportUtils.exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION);
			if (input == null){
				logger.debug("Error when creating the export of the SIMS {}", simsId);
				throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't export this object", "");
			}

			logger.debug("Creating the .odt file for the SIMS {}", simsId);
			Path tempFile = Files.createFile(Path.of(simsDirectory.toString(), fileName + FilesUtils.ODT_EXTENSION));
			Files.write(tempFile, input.readAllBytes(), StandardOpenOption.APPEND);
			logger.debug("Finishing the creation of the .odt file for the SIMS {}", simsId);


			logger.debug("Starting downloading documents for the SIMS {}", simsId);
			Set<String> missingDocuments = this.exportRubricsDocuments(sims, simsDirectory);
			logger.debug("Ending downloading documents for the SIMS {}", simsId);

			logger.debug("Zipping the folder for the SIMS {}", simsId);
			FilesUtils.zipDirectory(simsDirectory.toFile());

			logger.debug("Zip created for the SIMS {}", simsId);
			HttpHeaders responseHeaders = HttpUtils.generateHttpHeaders(sims.getString(Constants.LABEL_LG1), FilesUtils.ZIP_EXTENSION, this.maxLength);
			responseHeaders.set("X-Missing-Documents", String.join(",", missingDocuments));
			Resource resource = new UrlResource(Paths.get(simsDirectory.toString(), simsDirectory.getFileName() + FilesUtils.ZIP_EXTENSION).toUri());
			return ResponseEntity.ok()
					.headers(responseHeaders)
					.body(resource);
		}
		catch (Exception exception) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception.getClass().getSimpleName());
		}
	}

	private Set<String> exportRubricsDocuments(JSONObject sims, Path directory) throws IOException, RmesException {
		Set<String> history = new HashSet<>();
		JSONArray documents = documentsUtils.getDocumentsUriAndUrlForSims(sims.getString("id"));
		Set<String> missingDocuments = new HashSet<>();

		for (int i = 0; i < documents.length(); i++) {
			JSONObject document = documents.getJSONObject(i);
			String url = document.getString("url").replace("file://", "");
			if(!history.contains(url)){
				history.add(url);
				logger.debug("Extracting document {}", url);


				Path documentPath = Path.of(url);

				if(!Files.exists(documentPath)){
					missingDocuments.add(document.getString("id"));
				} else {
					String documentFileName = FilesUtils.reduceFileNameSize(UriUtils.getLastPartFromUri(url), maxLength);
					try (InputStream inputStream = Files.newInputStream(documentPath)){
						Path documentDirectory = Path.of(directory.toString(), "documents");
						if (!Files.exists(documentDirectory)) {
							logger.debug("Creating the documents folder");
							Files.createDirectory(documentDirectory);
						}

						logger.debug("Writing the document {} with the name {} into the folder {}", url, documentFileName, directory.toString());
						Path documentTempFile = Files.createFile(Path.of(documentDirectory.toString(), documentFileName));
						Files.write(documentTempFile, inputStream.readAllBytes(), StandardOpenOption.APPEND);
					}
				}

			}
		}

		return missingDocuments;
	}

	private ResponseEntity<Resource> export(Exporter exporter, Map<String, String> xmlContent, PatternAndZip patternAndZip) throws RmesException {
		return exporter.export(xmlContent, xslFile, patternAndZip.xmlPattern(), patternAndZip.zip(), DOCUMENTATION);
	}

	public ResponseEntity<Object> exportXmlFiles(Map<String, String> xmlContent, String targetType, boolean includeEmptyFields, boolean lg1,
			boolean lg2) throws RmesException {
		//Add params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, targetType);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);

		return exportUtils.exportFilesAsResponse(xmlContent);
	}
	

	public ResponseEntity<Resource> exportMetadataReport(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2, Boolean document, String goal) throws RmesException {
		Map<String,String> xmlContent = new HashMap<>();
		String targetType = getXmlContent(id, xmlContent);
		String msdXML = buildShellSims();
		xmlContent.put("msdFile", msdXML);
		return exportAsResponse(id, xmlContent,targetType,includeEmptyMas,lg1,lg2, document, goal);
	}
	

	public ResponseEntity<Object> exportMetadataReportFiles(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2) throws RmesException {
		Map<String,String> xmlContent = new HashMap<>();
		String targetType = getXmlContent(id, xmlContent);
		String msdXML = buildShellSims();
		xmlContent.put("msdFile", msdXML);
		return exportXmlFiles(xmlContent,targetType,includeEmptyMas,lg1,lg2);
	}

	public String getXmlContent(String id, Map<String, String> xmlContent) throws RmesException {
		String emptyXML=XMLUtils.produceEmptyXML();
		Operation operation;
		Series series;
		String operationXML;
		String seriesXML = emptyXML;
		String indicatorXML;

		String[] target = parentUtils.getDocumentationTargetTypeAndId(id);
		String targetType = target[0];
		String idDatabase = target[1];

		List<String> neededCodeLists=new ArrayList<>();

		if (targetType.equals(Constants.OPERATION_UP)) {
			operation=operationsUtils.getOperationById(idDatabase);
			operationXML = XMLUtils.produceXMLResponse(operation);
			neededCodeLists.addAll(XMLUtils.getTagValues(operationXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(operationXML,Constants.ACCRUAL_PERIODICITY_LIST));
			String idSeries=operation.getSeries().getId();
			series=seriesUtils.getSeriesById(idSeries,EncodingType.XML);
			seriesXML = XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		} else {operationXML = emptyXML;}


		if (targetType.equals(Constants.INDICATOR_UP)) {
			indicatorXML=XMLUtils.produceXMLResponse(
					indicatorsUtils.getIndicatorById(idDatabase,true));
			neededCodeLists.addAll(XMLUtils.getTagValues(indicatorXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(indicatorXML,Constants.ACCRUAL_PERIODICITY_LIST));
			String idSeries = XMLUtils.getTagValues(
					XMLUtils.getTagValues(
							indicatorXML,
							Constants.WASGENERATEDBY).getFirst(),
					Constants.ID).getFirst();
			series=seriesUtils.getSeriesById(idSeries,EncodingType.XML);
			seriesXML = XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		} else {indicatorXML = emptyXML;}


		if (targetType.equals(Constants.SERIES_UP)) {
			seriesXML=XMLUtils.produceXMLResponse(
					seriesUtils.getSeriesById(idDatabase,EncodingType.XML));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		}

		String organizationsXML = XMLUtils.produceXMLResponse(organizationsServiceImpl.getOrganizations());

		String simsXML=XMLUtils.produceResponse(documentationsUtils.getFullSimsForXml(id), "application/xml");
		neededCodeLists.addAll(XMLUtils.getTagValues(simsXML,Constants.CODELIST));

		neededCodeLists = neededCodeLists.stream().distinct().collect(Collectors.toList());

		String codeListsXML="";
		codeListsXML = codeListsXML.concat(Constants.XML_OPEN_CODELIST_TAG);

		for(String code : neededCodeLists) {
			DetailedCodeList codeList = codeListServiceImpl.getCodeListAndCodesForExport(code);
			codeListsXML = codeListsXML.concat(XMLUtils.produceXMLResponse(codeList));
		}
		codeListsXML=codeListsXML.concat(Constants.XML_END_CODELIST_TAG);


		xmlContent.put("simsFile",  simsXML);
		xmlContent.put("seriesFile",  seriesXML);
		xmlContent.put("operationFile",  operationXML);
		xmlContent.put("indicatorFile",  indicatorXML);
		xmlContent.put("codeListsFile",  codeListsXML);
		xmlContent.put("organizationsFile",  organizationsXML);
		return targetType;
	}
	

	private String buildShellSims() throws RmesException {
		MSD msd= documentationsUtils.getMSD();
		return XMLUtils.produceXMLResponse(msd);
	}

	private interface Exporter{
		ResponseEntity<Resource> export(Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException;
	}

	private record PatternAndZip(String xmlPattern, String zip) {
		public static PatternAndZip of(String goal) throws RmesBadRequestException {
			return switch (goal){
				case GOAL_RMES -> new PatternAndZip(xmlPatternRmes, zipRmes);
				case GOAL_COMITE_LABEL -> new PatternAndZip(xmlPatternLabel,zipLabel);
				default -> throw new RmesBadRequestException("The goal is unknown");
			};
		}
	}
}
