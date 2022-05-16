package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.code_list.LangService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.PAV;
import fr.insee.rmes.persistance.ontologies.SCHEMA;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentsQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.UriUtils;

@Component
public class DocumentsUtils  extends RdfService  {

	private static final String SCHEME_FILE = "file://";
	static final Logger logger = LogManager.getLogger(DocumentsUtils.class);

	@Autowired
	private LangService langService;

	@Autowired
	ParentUtils ownersUtils;
	
	/*
	 * METHODS LINKS TO THE SIMS - RUBRICS
	 */
	public void addDocumentsAndLinksToRubric(Model model, Resource graph, List<Document> documents, IRI textUri)
			throws RmesException {
		if (documents != null && !documents.isEmpty()) {
			for (Document doc : documents) {
				IRI url = RdfUtils.toURI(doc.getUrl());
				IRI docUri ;
				if (StringUtils.isNotEmpty(doc.getUri())){
					docUri = RdfUtils.toURI(doc.getUri());
				}
				else{
					docUri = getDocumentUri(url);
				}
				RdfUtils.addTripleUri(textUri, INSEE.ADDITIONALMATERIAL, docUri, model, graph);
			}
		}
	}


	/**
	 * Get documents link to one rubric of a metadata report
	 * @param idSims
	 * @param idRubric
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getListDocumentLink(String idSims, String idRubric, String lang) throws RmesException {
		JSONArray allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getDocumentsForSimsRubricQuery(idSims, idRubric, langService.getLanguageByConfigLg(lang)));
		formatDateInJsonArray(allDocs);
		return allDocs;
	}
	
		/**
	 * Get documents link to a metadata report (no links)
	 * @param idSims
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getListDocumentSims(String idSims) throws RmesException {
		JSONArray allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getDocumentsForSimsQuery(idSims));
		formatDateInJsonArray(allDocs);
		return allDocs;
	}

	/**
	 * Get links link to a metadata report (no document)
	 * @param idSims
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getListLinksSims(String idSims) throws RmesException {
		JSONArray allLinks = repoGestion.getResponseAsArray(DocumentsQueries.getLinksForSimsQuery(idSims));
		formatDateInJsonArray(allLinks);
		return allLinks;
	}

	/**
	 * Get all documents
	 * @return allDocs
	 * @throws RmesException
	 */
	public JSONArray getAllDocuments() throws RmesException {
		JSONArray allDocs = new JSONArray();
		try {
			allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getAllDocumentsQuery());
			formatDateInJsonArray(allDocs);
		} catch (RmesException e) {
			logger.error(e.getMessage());
			throw e;
		}
		return allDocs;
	}

	private void formatDateInJsonArray(JSONArray allDocs) {
		if (allDocs.length() != 0) {
			for (int i = 0; i < allDocs.length(); i++) {
				JSONObject doc = allDocs.getJSONObject(i);
				formatDateInJsonObject(doc);
			}
		}
	}

	private void formatDateInJsonObject(JSONObject doc) {
		if (doc.has(Constants.UPDATED_DATE)) {
			String formatedDate = DateUtils.getDate(doc.getString(Constants.UPDATED_DATE));
			doc.remove(Constants.UPDATED_DATE);
			doc.put(Constants.UPDATED_DATE, formatedDate);
		}
	}
	
	/**
	 * Generate a new ID for document or link
	 * @return
	 * @throws RmesException
	 */
	protected String createDocumentID() throws RmesException {
		logger.info("Generate document id");

		JSONObject json = repoGestion.getResponseAsObject(DocumentsQueries.lastDocumentID());
		Integer id = getIdFromJson(json) == null ? 999 : getIdFromJson(json);

		json = repoGestion.getResponseAsObject(DocumentsQueries.lastLinkID());
		id = (getIdFromJson(json) == null ? id : Math.max(getIdFromJson(json), id)) + 1;
		return id.toString();
	}

	public Integer getIdFromJson(JSONObject json) {
		if (json.length() == 0) {
			return null;
		} else {
			String id = json.getString(Constants.ID);
			if (id.equals(Constants.UNDEFINED) || StringUtils.isEmpty(id)) {
				return null;
			} else {
				return Integer.parseInt(id);
			}
		}
	}

	/**
	 * Create document
	 * @param id
	 * @param body
	 * @param documentFile
	 * @throws RmesException
	 */
	public void createDocument(String id, String body, boolean isLink, InputStream documentFile, String documentName)
			throws RmesException {

		/* Check rights */
		if (!stampsRestrictionsService.canManageDocumentsAndLinks()) {
			throw new RmesUnauthorizedException(isLink ?ErrorCodes.LINK_CREATION_RIGHTS_DENIED: ErrorCodes.DOCUMENT_CREATION_RIGHTS_DENIED,
					"Only an admin or a manager can create a new  "+ (isLink ? "link." : "document."));
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id, isLink);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}


		if (isLink) {
			checkLinkDoesNotExist(id, document.getUrl());
		}else {
			String url = createFileUrl(documentName);
			checkDocumentDoesNotExist(id, url);
			logger.info("URL CREATED : {}", url);
			document.setUrl(url);

			// upload file in storage folder
			uploadFile(documentFile, documentName, url, false);
		}

		//Write RDF graph in database
		try {
			IRI docUri = RdfUtils.toURI(document.getUri());
			writeRdfDocument(document, docUri);
		} catch (RmesException e) {
			deleteDocument(id, isLink);
			throw e;
		}
	}


	private void checkLinkDoesNotExist(String id, String url) throws RmesException {
		if (StringUtils.isEmpty(url)) {
			throw new RmesNotAcceptableException(ErrorCodes.LINK_EMPTY_URL, "A link must have a non-empty url. ", id);
		}
		// Check if the url is already used by a link
		checkUrlDoesNotExist(id, url, ErrorCodes.LINK_EXISTING_URL,"This url is already referenced by another link.");
	}
	
	private void checkDocumentDoesNotExist(String id, String url) throws RmesException {
		if (StringUtils.isEmpty(url)) {
			throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_EMPTY_NAME, "A document must have a non-empty url. ", id);
		}
		if (url.contains(SCHEME_FILE)) {
			url = url.replace(SCHEME_FILE, "");
		}

		// Check if the url is already used by another document
		checkUrlDoesNotExist(id, getDocumentNameFromUrl(url), ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE, "This url is already referenced by another document.");
	}


	private void checkUrlDoesNotExist(String id, String url, int errorCode, String errorMessage) throws RmesException {
		JSONObject existingUriJson = repoGestion.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(url));
		if (existingUriJson.length() > 0) {
			String uri = existingUriJson.getString(Constants.DOCUMENT);
			String existingId = getIdFromUri(uri);
			if (!existingId.equals(id)) {
				throw new RmesNotAcceptableException(errorCode,errorMessage, uri);
			}
		}
	}
	


	/**
	 * Update a document or link
	 * @throws RmesException
	 */
	public void setDocument(String id, String body, boolean isLink) throws RmesException {
		/* Check rights */
		if (!stampsRestrictionsService.canManageDocumentsAndLinks()) {
			throw new RmesUnauthorizedException(ErrorCodes.LINK_MODIFICATION_RIGHTS_DENIED,
					"Only an admin or a manager can modify a "+ (isLink ? "link." : "document."), id);
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id, isLink);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		IRI docUri = RdfUtils.toURI(document.getUri());
		logger.info("Update document : {} - {} / {}", document.getUri(), document.getLabelLg1(), document.getLabelLg2());
		writeRdfDocument(document, docUri);
	}

	/**
	 * Get RDF for a document or a link by ID
	 * with associated sims (and their creators)
	 * @param id
	 * @param isLink
	 * @return
	 * @throws RmesException
	 */
	public JSONObject getDocument(String id, boolean isLink) throws RmesException {
		JSONObject jsonDocs = new JSONObject();
		try {
			jsonDocs = repoGestion.getResponseAsObject(DocumentsQueries.getDocumentQuery(id, isLink));
		} catch (RmesException e) {
			logger.error(e.getMessage());
		}

		if (jsonDocs.isNull(Constants.URI)) {
			throw new RmesNotFoundException(ErrorCodes.DOCUMENT_UNKNOWN_ID, "Cannot find "+ (isLink ? "Link" : "Document")+" with id: ", id);
		}
		formatDateInJsonObject(jsonDocs);
		JSONArray sims = repoGestion.getResponseAsArray(DocumentsQueries.getSimsByDocument(id, isLink));

		for (int i = 0; i < sims.length(); i++) {
			JSONObject sim = sims.getJSONObject(i);
			sim.put(Constants.CREATORS, new JSONArray(ownersUtils.getDocumentationOwnersByIdSims(sim.getString(Constants.ID))));
		}

		jsonDocs.put("sims", sims);
		return jsonDocs;
	}

	/**
	 * Delete a document or a link
	 * @param docId
	 * @return
	 * @throws RmesException
	 */
	public HttpStatus deleteDocument(String docId, boolean isLink) throws RmesException {
		JSONObject jsonDoc = getDocument(docId, isLink);
		String uri = jsonDoc.getString(Constants.URI);
		String url = getDocumentUrlFromDocument(jsonDoc);
		IRI docUri = RdfUtils.toURI(uri);

		// Check that the document is not referred to by any sims
		checkDocumentReference(docId, uri);
		// remove the physical file
		if (!isLink) {
			deleteFile(url);
		}
		// delete the Document in the rdf base
		return repoGestion.executeUpdate(DocumentsQueries.deleteDocumentQuery(docUri));
	}

	// Check that the document is not referred to by any sims
	private void checkDocumentReference(String docId, String uri) throws RmesException {
		JSONArray jsonResultat = repoGestion.getResponseAsArray(DocumentsQueries.getLinksToDocumentQuery(docId));
		if (jsonResultat.length() > 0) {
			throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_DELETION_LINKED,
					"The document " + uri + "cannot be deleted because it is referred to by " + jsonResultat.length()
					+ " sims, including: " + ((JSONObject) jsonResultat.get(0)).get(Constants.TEXT).toString(),
					jsonResultat);
		}
	}
	/**
	 * Check that if the file is referenced to by some sims, user has rights on these sims
	 * @param docId
	 * @throws RmesException
	 */
	private void checkRightsToModifyFile(String docId) throws RmesException {
		JSONArray sims = repoGestion.getResponseAsArray(DocumentsQueries.getLinksToDocumentQuery(docId));
		if (sims.length() == 0) return; //document's file isn't linked to a sims
		for (int i = 0; i < sims.length(); i++) {
			String simsUri = ((JSONObject) sims.get(i)).get(Constants.TEXT).toString();
			
			Pattern p = Pattern.compile("(.*)attribut/([0-9]{4})/(.*)");
			Matcher m = p.matcher(simsUri);
			String simsId = null ;
			if(m.matches()) {
				simsId = m.group(2);
			}
			String[] target = ownersUtils.getDocumentationTargetTypeAndId(simsId);
			//target[0] =  targetType / target[1] idTarget
			IRI targetIri = null ;
			
			switch(target[0]) {
				case Constants.OPERATION_UP : targetIri = RdfUtils.objectIRI(ObjectType.OPERATION, target[1]); break;
				case Constants.SERIES_UP : targetIri = RdfUtils.objectIRI(ObjectType.SERIES, target[1]); break;
				case Constants.INDICATOR_UP : targetIri = RdfUtils.objectIRI(ObjectType.INDICATOR, target[1]); break;
				default : break;
			} 
					
			if (!stampsRestrictionsService.canModifySims(targetIri)) {
				throw new RmesUnauthorizedException(ErrorCodes.SIMS_MODIFICATION_RIGHTS_DENIED,
						"Only an admin, CNIS, or a manager can modify this sims.", simsUri);
			}		
		}
		
	}
	


	public String changeFile(String docId, InputStream documentFile, String documentName) throws RmesException {

		JSONObject jsonDoc = getDocument(docId, false);
		// Cannot upload file for a Link = if not found it's probably a link
		if (!jsonDoc.has(Constants.URL)) {
			throw new RmesException(HttpStatus.NOT_ACCEPTABLE.value(),
					"Document not found. Warning : Links have no attached file. Cannot upload file " + documentName + " for this document: ", docId);
		}

		String docUrl = getDocumentUrlFromDocument(jsonDoc);

		// check rights
		checkRightsToModifyFile(docId);		
		
		// Warning if different file extension
		String oldExt = StringUtils.substringAfterLast(docUrl, ".");
		String newExt = StringUtils.substringAfterLast(documentName, ".");
		if (!oldExt.equals(newExt)) {
			logger.info("Warning: The new file has extension: .{} while the old file had extension: .{}", newExt,oldExt);
		}

		String oldName = getDocumentNameFromUrl(docUrl);
		String newUrl = null;

		// Same documentName -> keep the same URL
		if (oldName.equals(documentName)) {		
			logger.info("Replacing file {} at the same Url", documentName);
			uploadFile(documentFile, documentName, docUrl, true);
		}
		// Different documentName -> create a new URL
		else {
			// Delete the old file
			logger.info("Delete old file {}, with URL {}", documentName, docUrl);
			deleteFile(docUrl);

			// Upload the new file
			newUrl = createFileUrl(documentName);
			logger.info("Try to replace file {}, new URL is {}", documentName, newUrl);
			uploadFile(documentFile, documentName, newUrl, false);

			// Update document's url
			changeDocumentsURL(docId, addSchemeFile(docUrl), addSchemeFile(newUrl));
		}

		return newUrl;
	}
	
	private String addSchemeFile(String url) {
		if (url.startsWith(SCHEME_FILE))return url;
		return SCHEME_FILE+url;
	}

	private void uploadFile(InputStream documentFile, String documentName, String url, Boolean sameName)
			throws RmesUnauthorizedException {
		// upload file in storage folder
		logger.debug("URL : {}" , url);
		Path path = Paths.get(url.replace(SCHEME_FILE, ""));
		logger.debug("PATH : {}" , path);
		if (!Boolean.TRUE.equals(sameName) && path.toFile().exists()) {
			throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE,
					"There is already a document with that name.", documentName);
		}
		try {
			Files.copy(documentFile, path, StandardCopyOption.REPLACE_EXISTING); 
			// don't throw an error if a file already exists under this name
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}


	/**
	 * Write a document in rdf database
	 * @param document
	 * @param docUri
	 * @throws RmesException
	 */

	private void writeRdfDocument(Document document, IRI docUri) throws RmesException {

		Resource graph = RdfUtils.documentsGraph();
		Model model = new LinkedHashModel();

		RdfUtils.addTripleUri(docUri, RDF.TYPE, FOAF.DOCUMENT, model, graph);

		String uriString = document.getUrl();
		RdfUtils.addTripleUri(docUri, SCHEMA.URL, uriString, model, graph);
		if (StringUtils.isNotEmpty(document.getLabelLg1())) {
			RdfUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg1(), config.getLg1(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLabelLg2())) {
			RdfUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg2(), config.getLg2(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg1())) {
			RdfUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg1(), config.getLg1(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg2())) {
			RdfUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg2(), config.getLg2(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLangue())) {
			RdfUtils.addTripleString(docUri, DC.LANGUAGE, document.getLangue(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDateMiseAJour())) {
			RdfUtils.addTripleDateTime(docUri, PAV.LASTREFRESHEDON, document.getDateMiseAJour(), model, graph);
		}
		repoGestion.loadSimpleObject(docUri, model);
	}

	private HttpStatus changeDocumentsURL(String docId, String docUrl, String newUrl) throws RmesException {
		return repoGestion.executeUpdate(DocumentsQueries.changeDocumentUrlQuery(docId, docUrl, newUrl));
	}

	private void deleteFile(String docUrl) {
		Path path = Paths.get(docUrl);
		try {
			Files.delete(path);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public String getDocumentNameFromUrl(String docUrl) {
		return UriUtils.getLastPartFromUri(docUrl);
	}
	
	private String getIdFromUri(String uri) {
		return UriUtils.getLastPartFromUri(uri);
	}

	private String createFileUrl(String name) throws RmesException {
		String url = getGestionStorageFolderPath().resolve(name).toString();
		Pattern p = Pattern.compile("^(?:[a-zA-Z]+:/)");
		Matcher m = p.matcher(url);
		if (m.find()) {// absolute URL
			return url;
		}
		return addSchemeFile(url);
	}

	/**
	 * return new uri if url doesn't exist
	 * @param url
	 * @return
	 * @throws RmesException
	 */
	private IRI getDocumentUri(IRI url) throws RmesException {
		JSONObject uri = repoGestion
				.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(getDocumentNameFromUrl(url.stringValue())));
		if (uri.length() == 0 || !uri.has(Constants.DOCUMENT)) {
			String id = createDocumentID();
			return RdfUtils.objectIRI(ObjectType.DOCUMENT, id);
		}
		return RdfUtils.toURI(uri.getString(Constants.DOCUMENT));
	}

	public String getDocumentUrlFromDocument(JSONObject jsonDoc) {
		return jsonDoc.getString(Constants.URL).replace(SCHEME_FILE, "");
	}

	public void checkFileNameValidity(String fileName) throws RmesNotAcceptableException {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_EMPTY_NAME, "Empty fileName", fileName);
		}
		Pattern p = Pattern.compile("[^A-Za-z0-9._-]");
		Matcher m = p.matcher(fileName);
		if (m.find()) {
			logger.info("There is a forbidden character in the FileName ");
			throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_FORBIDDEN_CHARACTER_NAME,
					"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens",
					fileName);
		}

	}

	public Document buildDocumentFromJson(JSONObject jsonDoc) {
		Document doc = new Document();
		if (jsonDoc.has(Constants.LABEL_LG1)) {
			doc.setLabelLg1(jsonDoc.getString(Constants.LABEL_LG1));
		}
		if (jsonDoc.has(Constants.LABEL_LG2)) {
			doc.setLabelLg2(jsonDoc.getString(Constants.LABEL_LG2));
		}
		if (jsonDoc.has(Constants.DESCRIPTION_LG1)) {
			doc.setDescriptionLg1(jsonDoc.getString(Constants.DESCRIPTION_LG1));
		}
		if (jsonDoc.has(Constants.DESCRIPTION_LG2)) {
			doc.setDescriptionLg2(jsonDoc.getString(Constants.DESCRIPTION_LG2));
		}
		if (jsonDoc.has(Constants.UPDATED_DATE)) {
			doc.setDateMiseAJour(jsonDoc.getString(Constants.UPDATED_DATE));
		}
		if (jsonDoc.has(Constants.LANG)) {
			doc.setLangue(jsonDoc.getString(Constants.LANG));
		}
		if (jsonDoc.has(Constants.URL)) {
			doc.setUrl(jsonDoc.getString(Constants.URL));
		}
		if (jsonDoc.has(Constants.URI)) {
			doc.setUri(jsonDoc.getString(Constants.URI));
		}

		return doc ;
	}

	/**
	 * Download a document by id
	 * @param id
	 * @return Response containing the file (inputStream)
	 * @throws RmesException
	 */
	public ResponseEntity<Object> downloadDocumentFile(String id) throws RmesException {
		JSONObject jsonDoc = getDocument(id, false);

		String url = getDocumentUrlFromDocument(jsonDoc);
		String fileName = getDocumentNameFromUrl(url);
		Path path = Paths.get(url);
		ContentDisposition content = ContentDisposition.builder("attachement").filename(fileName).build();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentDisposition(content);
		try {
			return ResponseEntity.ok()
						.headers(responseHeaders)
						.body((StreamingOutput) output -> {
	                InputStream input = Files.newInputStream(path);
	                IOUtils.copy(input, output);
	                input.close();
	                output.flush();   
	                output.close();
	        });			
		 } catch ( Exception e ) { 
         	logger.error(e.getMessage());
         	throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Error downloading file"); 
         }
	}


	private Path getGestionStorageFolderPath() throws RmesException {
		Path path = null;
		File dir = new File(config.getDocumentsStorageGestion());
		if (dir.exists()) {
			path = Paths.get(config.getDocumentsStorageGestion());
		} else {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Storage folder not found",
					"config.DOCUMENTS_STORAGE");
		}
		return path;
	}



}
