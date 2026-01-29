package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.code_list.export.CodesListExport;
import fr.insee.rmes.bauhaus_services.code_list.export.ExportedCodesList;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
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

import static fr.insee.rmes.Constants.GOAL_COMITE_LABEL;
import static fr.insee.rmes.Constants.GOAL_RMES;

@Component
public class DocumentationExport {
	static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);

	public static final String DOCUMENTATION = "documentation";
	final ExportUtils exportUtils;
	
	final SeriesUtils seriesUtils;
	
	final OperationsUtils operationsUtils;
	
	final IndicatorsUtils indicatorsUtils;
	
	final ParentUtils parentUtils;
	
	final CodesListExport codeListServiceImpl;
	
	final OrganizationsService organizationsServiceImpl;

	final OrganisationService organisationService;

	final DocumentationsUtils documentationsUtils;
	final DocumentsUtils documentsUtils;
	static final String xslFile = "/xslTransformerFiles/sims2fodt.xsl";
	static final String xmlPatternRmes = "/xslTransformerFiles/simsRmes/rmesPatternContent.xml";
	static final String zipRmes = "/xslTransformerFiles/simsRmes/toZipForRmes.zip";
	
	static final String xmlPatternLabel = "/xslTransformerFiles/simsLabel/labelPatternContent.xml";
	static final String zipLabel = "/xslTransformerFiles/simsLabel/toZipForLabel.zip";
	private final int maxLength;

	public DocumentationExport(
			@Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength,
			DocumentsUtils documentsUtils,
			ExportUtils exportUtils,
			SeriesUtils seriesUtils,
			OperationsUtils operationsUtils,
			IndicatorsUtils indicatorsUtils, ParentUtils parentUtils, CodesListExport codeListServiceImpl, OrganizationsService organizationsServiceImpl, OrganisationService organisationService, DocumentationsUtils documentationsUtils) {
		this.exportUtils = exportUtils;
		this.seriesUtils = seriesUtils;
		this.operationsUtils = operationsUtils;
		this.indicatorsUtils = indicatorsUtils;
		this.parentUtils = parentUtils;
		this.codeListServiceImpl = codeListServiceImpl;
		this.organizationsServiceImpl = organizationsServiceImpl;
		this.organisationService = organisationService;
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
			boolean lg2, boolean documents, String goal, int maxLength) throws RmesException {

		PatternAndZip patternAndZip = PatternAndZip.of(goal);
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, targetType);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);

		Exporter exporter;
		JSONObject sims = this.documentationsUtils.getDocumentationByIdSims(id);

		if (documents) {
			exporter = (xml, xsl, xmlPattern, zip, documentation) -> exportAsZip(sims, xml, xsl, xmlPattern, zip, documentation, maxLength);
		} else{
			String fileName = FilesUtils.generateFinalFileNameWithoutExtension(sims.getString(Constants.LABEL_LG1), maxLength);
			exporter = (xml, xsl, xmlPattern, zip, documentation) -> exportUtils.exportAsODT(fileName, xml, xsl, xmlPattern, zip, documentation );
		}
		return export(exporter, xmlContent, patternAndZip);
	}

	public ResponseEntity<Resource> exportAsZip(JSONObject sims, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType, int maxLength) throws RmesException {
		String simsId = sims.getString("id");
		logger.debug("Begin to download the SIMS {} with its documents", simsId);
		String fileName = FilesUtils.generateFinalFileNameWithoutExtension(sims.getString(Constants.LABEL_LG1), maxLength);

		try {

			Path directory = Files.createTempDirectory("sims");
			logger.debug("Creating tempory directory {}", directory);
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
			HttpHeaders responseHeaders = HttpUtils.generateHttpHeaders(fileName, FilesUtils.ZIP_EXTENSION);
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
			String url = DocumentsUtils.getDocumentUrlFromDocument(document);
			if(!history.contains(url)){
				history.add(url);
				logger.debug("Extracting document {}", url);


				String documentFilename = DocumentsUtils.getDocumentNameFromUrl(url);

				if(!documentsUtils.existsInStorage(documentFilename)){
					missingDocuments.add(document.getString("id"));
				} else {
					String documentFileName = FilesUtils.generateFinalFileNameWithExtension(UriUtils.getLastPartFromUri(url), maxLength);
					try (InputStream inputStream = documentsUtils.retrieveDocumentFromStorage(documentFilename)){
						Path documentDirectory = Path.of(directory.toString(), "documents");
						if (!Files.exists(documentDirectory)) {
							logger.debug("Creating the documents folder");
							Files.createDirectory(documentDirectory);
						}

						logger.debug("Writing the document {} with the name {} into the folder {}", url, documentFileName, directory);
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
	

	public ResponseEntity<Resource> exportMetadataReport(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2, Boolean document, String goal, int maxLength) throws RmesException {
		Map<String,String> xmlContent = new HashMap<>();
		String targetType = getXmlContent(id, xmlContent);
		xmlContent.put("msdFile", buildShellSims());
		return exportAsResponse(id, xmlContent,targetType,includeEmptyMas,lg1,lg2, document, goal, maxLength);
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
			transformCreatorsStampsToLabels(series);
			seriesXML = XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		} else {operationXML = emptyXML;}


		if (targetType.equals(Constants.INDICATOR_UP)) {
			Indicator indicator = indicatorsUtils.getIndicatorById(idDatabase,true);
			transformIndicatorCreatorsStampsToLabels(indicator);
			indicatorXML=XMLUtils.produceXMLResponse(indicator);
			neededCodeLists.addAll(XMLUtils.getTagValues(indicatorXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(indicatorXML,Constants.ACCRUAL_PERIODICITY_LIST));
			String idSeries = XMLUtils.getTagValues(
					XMLUtils.getTagValues(
							indicatorXML,
							Constants.WASGENERATEDBY).getFirst(),
					Constants.ID).getFirst();
			series=seriesUtils.getSeriesById(idSeries,EncodingType.XML);
			transformCreatorsStampsToLabels(series);
			seriesXML = XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		} else {indicatorXML = emptyXML;}


		if (targetType.equals(Constants.SERIES_UP)) {
			series = seriesUtils.getSeriesById(idDatabase,EncodingType.XML);
			transformCreatorsStampsToLabels(series);
			seriesXML=XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		}

		String organizationsXML = XMLUtils.produceXMLResponse(organizationsServiceImpl.getOrganizations());

		String simsXML=XMLUtils.produceResponse(documentationsUtils.getFullSimsForXml(id), "application/xml");
		neededCodeLists.addAll(XMLUtils.getTagValues(simsXML,Constants.CODELIST));

		neededCodeLists = neededCodeLists.stream().distinct().toList();

		String codeListsXML="";
		codeListsXML = codeListsXML.concat(Constants.XML_OPEN_CODELIST_TAG);

		for(String code : neededCodeLists) {
			ExportedCodesList codeList = codeListServiceImpl.exportCodesList(code);
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

	/**
	 * Transforms organization stamps to organization labels using batch retrieval for better performance
	 * @param stamps List of organization stamps to transform
	 * @return List of organization labels (never null, returns empty list if input is null or empty)
	 * @throws RmesException if organization retrieval fails
	 */
	private List<String> transformStampsToLabels(List<String> stamps) throws RmesException {
		// Null safety - return empty list instead of null
		if (stamps == null || stamps.isEmpty()) {
			return stamps != null ? stamps : Collections.emptyList();
		}

		// Validation - filter out null or blank stamps
		List<String> validStamps = stamps.stream()
				.filter(stamp -> stamp != null && !stamp.isBlank())
				.distinct()
				.toList();

		if (validStamps.isEmpty()) {
			logger.debug("No valid stamps to transform");
			return Collections.emptyList();
		}

		logger.debug("Transforming {} organization stamps to labels (batch mode)", validStamps.size());

		// Batch retrieval for better performance
		Map<String, OrganisationOption> organisationsMap;
		try {
			organisationsMap = organisationService.getOrganisationsMap(validStamps);
		} catch (Exception e) {
			logger.error("Failed to batch retrieve organizations, falling back to original stamps: {}", e.getMessage());
			return stamps;
		}

		int successCount = 0;
		int notFoundCount = 0;

		List<String> transformedCreators = new ArrayList<>();
		for (String stamp : stamps) {
			// Handle null or blank stamps
			if (stamp == null || stamp.isBlank()) {
				logger.debug("Skipping null or blank stamp");
				continue;
			}

			OrganisationOption organisation = organisationsMap.get(stamp);
			if (organisation != null && organisation.label() != null && !organisation.label().isEmpty()) {
				transformedCreators.add(organisation.label());
				successCount++;
			} else {
				// If organization not found or has no value, keep the stamp
				transformedCreators.add(stamp);
				notFoundCount++;
				if (organisation == null) {
					logger.debug("Organization not found for stamp: {}", stamp);
				} else {
					logger.debug("Organization found but has no value for stamp: {}", stamp);
				}
			}
		}

		logger.debug("Transformation complete: {} succeeded, {} not found (kept original stamps)",
				successCount, notFoundCount);

		return transformedCreators;
	}

	/**
	 * Transforms organization stamps in series creators to organization labels
	 * @param series The series object to transform
	 * @throws RmesException if organization retrieval fails
	 */
	private void transformCreatorsStampsToLabels(Series series) throws RmesException {
		series.setCreators(transformStampsToLabels(series.getCreators()));
	}

	/**
	 * Transforms organization stamps in indicator creators to organization labels
	 * @param indicator The indicator object to transform
	 * @throws RmesException if organization retrieval fails
	 */
	private void transformIndicatorCreatorsStampsToLabels(Indicator indicator) throws RmesException {
		indicator.setCreators(transformStampsToLabels(indicator.getCreators()));
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
