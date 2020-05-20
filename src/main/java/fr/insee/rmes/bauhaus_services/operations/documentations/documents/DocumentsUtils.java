package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.PAV;
import fr.insee.rmes.persistance.ontologies.SCHEMA;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentsQueries;
import fr.insee.rmes.utils.DateParser;

@Component
public class DocumentsUtils  extends RdfService  {

	private static final String DOCUMENT = "document";

	private static final String UPDATED_DATE = "updatedDate";

	private static final String SCHEME_FILE = "file://";
	static final Logger logger = LogManager.getLogger(DocumentsUtils.class);

	public void addDocumentsToRubric(Model model, Resource graph, DocumentationRubric rubric, URI textUri)
			throws RmesException {
		if (rubric.getDocuments() != null && !rubric.getDocuments().isEmpty()) {
			for (Document doc : rubric.getDocuments()) {
				URI url = RdfUtils.toURI(doc.getUrl());
				URI docUri = getDocumentUri(url);
				RdfUtils.addTripleUri(textUri, INSEE.ADDITIONALMATERIAL, docUri, model, graph);
			}
		}
	}

	public Path getStorageFolderPath() throws RmesException {
		Path path = null;
		File dir = new File(Config.DOCUMENTS_STORAGE);
		if (dir.exists()) {
			path = Paths.get(Config.DOCUMENTS_STORAGE);
		} else {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Storage folder not found",
					"Config.DOCUMENTS_STORAGE");
		}
		return path;
	}

	/**
	 * Get documents link to one rubric of a metadata report
	 * @param idSims
	 * @param idRubric
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getListDocumentLink(String idSims, String idRubric) throws RmesException {
		JSONArray allDocs = repoGestion.getResponseAsArray(DocumentsQueries.getDocumentsQuery(idSims, idRubric));
		formatDateInJsonArray(allDocs);
		return allDocs;
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
		}
		return allDocs;
	}

	private void formatDateInJsonArray(JSONArray allDocs) {
		if (allDocs.length() != 0) {
			for (int i = 0; i < allDocs.length(); i++) {
				JSONObject doc = allDocs.getJSONObject(i);
				if (doc.has(UPDATED_DATE)) {
					String formatedDate = DateParser.getDate(doc.getString(UPDATED_DATE));
					doc.remove(UPDATED_DATE);
					doc.put(UPDATED_DATE, formatedDate);
				}
			}
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

	private Integer getIdFromJson(JSONObject json) {
		if (json.length() == 0) {
			return null;
		} else {
			String id = json.getString(Constants.ID);
			if (id.equals("undefined") || StringUtils.isEmpty(id)) {
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
	public void createDocument(String id, String body, InputStream documentFile, String documentName)
			throws RmesException {

		/* Check rights */
		if (!stampsRestrictionsService.canManageDocumentsAndLinks()) {
			throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_CREATION_RIGHTS_DENIED,
					"Only an admin or a manager can create a new document.");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id, false);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String url = createFileUrl(documentName);
		logger.info("URL CREATED : {}", url);
		document.setUrl(url);

		// upload file in storage folder
		uploadFile(documentFile, documentName, url, false);
		try {
			URI docUri = RdfUtils.toURI(document.getUri());
			writeRdfDocument(document, docUri);
		} catch (RmesException e) {
			deleteDocument(id);
			throw e;
		}
	}

	/**
	 * Update a document or link
	 * @throws RmesException
	 */
	public void setDocument(String id, String body) throws RmesException {
		/* Check rights */
		if (isLink(id)) {
			if (!stampsRestrictionsService.canManageDocumentsAndLinks()) {
				throw new RmesUnauthorizedException(ErrorCodes.LINK_MODIFICATION_RIGHTS_DENIED,
						"Only an admin or a manager can modify a link.", id);
			}
		} else {
			if (!stampsRestrictionsService.canManageDocumentsAndLinks()) {
				throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_MODIFICATION_RIGHTS_DENIED,
						"Only an admin or a manager can modify a document.", id);
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id, isLink(id));

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		URI docUri = RdfUtils.toURI(document.getUri());
		logger.info("Update document : {} - {} / {}", document.getUri(), document.getLabelLg1(), document.getLabelLg2());
		writeRdfDocument(document, docUri);
	}

	/**
	 * Get RDF for a document by ID (one request)
	 * @param id
	 * @return
	 * @throws RmesException
	 */
	public JSONObject getDocument(String id) throws RmesException {
		JSONObject jsonDocs = new JSONObject();
		try {
			jsonDocs = repoGestion.getResponseAsObject(DocumentsQueries.getDocumentQuery(id));
		} catch (RmesException e) {
			logger.error(e.getMessage());
		}

		if (jsonDocs.isNull(Constants.URI)) {
			throw new RmesNotFoundException(ErrorCodes.DOCUMENT_UNKNOWN_ID, "Cannot find Document with id: ", id);
		}
		if (jsonDocs.has(UPDATED_DATE)) {
			jsonDocs.put(UPDATED_DATE, DateParser.getDate(jsonDocs.getString(UPDATED_DATE)));
		}

		return jsonDocs;
	}

	public Status deleteDocument(String docId) throws RmesException {
		Resource graph = RdfUtils.documentsGraph();
		JSONObject jsonDoc = getDocument(docId);
		String uri = jsonDoc.getString(Constants.URI);
		String url = getDocumentUrlFromDocument(jsonDoc);
		URI docUri = RdfUtils.toURI(uri);

		// Check that the document is not referred to by any sims
		checkDocumentReference(docId, uri);
		// remove the physical file
		if (!isLink(jsonDoc)) {
			deleteFile(url);
		}
		// delete the Document in the rdf base
		return repoGestion.executeUpdate(DocumentsQueries.deleteDocumentQuery(docUri, (URI) graph));
	}

	private void checkDocumentReference(String docId, String uri) throws RmesException {
		JSONArray jsonResultat = getLinksToDocument(docId);
		if (jsonResultat.length() > 0) {
			throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_DELETION_LINKED,
					"The document " + uri + "cannot be deleted because it is referred to by " + jsonResultat.length()
							+ " sims, including: " + ((JSONObject) jsonResultat.get(0)).get("text").toString(),
					jsonResultat);
		}
	}

	private JSONArray getLinksToDocument(String docId) throws RmesException {
		return repoGestion.getResponseAsArray(DocumentsQueries.getLinksToDocumentQuery(docId));
	}

	public String changeDocument(String docId, InputStream documentFile, String documentName) throws RmesException {

		JSONObject jsonDoc = getDocument(docId);
		String docUrl = getDocumentUrlFromDocument(jsonDoc);

		// Cannot upload file for a Link
		if (isLink(jsonDoc)) {
			throw new RmesException(HttpStatus.SC_NOT_ACCEPTABLE,
					"Links have no attached file. Cannot upload file " + documentName + " for this document: ", docId);
		}

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
			// Upload the new file
			newUrl = createFileUrl(documentName);
			logger.info("Try to replace file {}, new URL is {}", documentName, newUrl);
			uploadFile(documentFile, documentName, newUrl, false);

			// Delete the old file
			logger.info("Delete old file {}, with URL {}", documentName, docUrl);
			checkDocumentReference(docId, jsonDoc.getString(Constants.URI));
			deleteFile(docUrl);

			// Update document's url
			changeDocumentsURL(docId, docUrl, newUrl);
		}

		return newUrl;
	}

	private void uploadFile(InputStream documentFile, String documentName, String url, Boolean sameName)
			throws RmesUnauthorizedException {
		// upload file in storage folder
		logger.debug("URL : {}" , url);
		Path path = Paths.get(url.replace(SCHEME_FILE, ""));
		logger.debug("PATH : {}" , path);
		if (!Boolean.TRUE.equals(sameName) && Files.exists(path)) {
			throw new RmesUnauthorizedException(ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE,
					"There is already a document with that name.", documentName);
		}
		try {
			Files.copy(documentFile, path, StandardCopyOption.REPLACE_EXISTING); 
			// throws an error if a file already exists under this name
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	/*
	 * LINKS
	 */
	public void createLink(String id, String body) throws RmesException {
		/* Check rights */
		if (!stampsRestrictionsService.canManageDocumentsAndLinks()) {
			throw new RmesUnauthorizedException(ErrorCodes.LINK_CREATION_RIGHTS_DENIED,
					"Only an admin or a manager can create a new link.");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document link = new Document(id, true);

		try {
			link = mapper.readerForUpdating(link).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String url = link.getUrl();
		if (StringUtils.isEmpty(url)) {
			throw new RmesNotAcceptableException(ErrorCodes.LINK_EMPTY_URL, "A link must have a non-empty url. ", id);
		}

		// Check if the url is already used by a link
		URI uriUrl = RdfUtils.toURI(url);
		JSONObject uri = repoGestion
				.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(uriUrl, RdfUtils.documentsGraph()));
		if (uri.length() > 0) {
			throw new RmesNotAcceptableException(ErrorCodes.LINK_EXISTING_URL,
					"This url is already referenced by another link.", uri.getString(DOCUMENT));
		}

		URI docUri = RdfUtils.toURI(link.getUri());

		writeRdfDocument(link, docUri);
	}

	/**
	 * Write a document in rdf database
	 * @param document
	 * @param docUri
	 * @throws RmesException
	 */

	private void writeRdfDocument(Document document, URI docUri) throws RmesException {

		Resource graph = RdfUtils.documentsGraph();
		Model model = new LinkedHashModel();

		RdfUtils.addTripleUri(docUri, RDF.TYPE, FOAF.DOCUMENT, model, graph);

		String uriString = document.getUrl();
		RdfUtils.addTripleUri(docUri, SCHEMA.URL, uriString, model, graph);
		if (StringUtils.isNotEmpty(document.getLabelLg1())) {
			RdfUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg1(), Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLabelLg2())) {
			RdfUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg2(), Config.LG2, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg1())) {
			RdfUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg1(), Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg2())) {
			RdfUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg2(), Config.LG2, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLangue())) {
			RdfUtils.addTripleString(docUri, DC.LANGUAGE, document.getLangue(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDateMiseAJour())) {
			RdfUtils.addTripleDateTime(docUri, PAV.LASTREFRESHEDON, document.getDateMiseAJour(), model, graph);
		}
		repoGestion.loadSimpleObject(docUri, model, null);
	}

	private Response.Status changeDocumentsURL(String docId, String docUrl, String newUrl) throws RmesException {
		Resource graph = RdfUtils.documentsGraph();
		return repoGestion.executeUpdate(DocumentsQueries.changeDocumentUrlQuery(docId, docUrl, newUrl, graph));
	}

	private void deleteFile(String docUrl) {
		Path path = Paths.get(docUrl);
		try {
			Files.delete(path);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private String getDocumentNameFromUrl(String docUrl) {
		return StringUtils.substringAfterLast(docUrl, "/");
	}

	private String createFileUrl(String name) throws RmesException {
		String url = getStorageFolderPath().resolve(name).toString();
		Pattern p = Pattern.compile("^(?:[a-zA-Z]+:/)");
		Matcher m = p.matcher(url);
		if (m.find()) {// absolute URL
			return url;
		}
		return SCHEME_FILE + url;
	}

	/**
	 * return new uri if url doesn't exist
	 * @param url
	 * @return
	 * @throws RmesException
	 */
	private URI getDocumentUri(URI url) throws RmesException {
		JSONObject uri = repoGestion
				.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(url, RdfUtils.documentsGraph()));
		if (uri.length() == 0 || !uri.has(DOCUMENT)) {
			String id = createDocumentID();
			return RdfUtils.objectIRI(ObjectType.DOCUMENT, id);
		}
		return RdfUtils.toURI(uri.getString(DOCUMENT));
	}

	private String getDocumentUrlFromDocument(JSONObject jsonDoc) {
		return jsonDoc.getString(Constants.URL).replace(SCHEME_FILE, "");
	}

	private boolean isLink(String id) throws RmesException {
		JSONObject jsonDoc = getDocument(id);
		return isLink(jsonDoc);
	}

	private boolean isLink(JSONObject jsonDoc)  {
		String uri = jsonDoc.getString(Constants.URI);
		return StringUtils.contains(uri, Config.LINKS_BASE_URI);
	}

	public void checkFileNameValidity(String fileName) throws RmesNotAcceptableException {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_EMPTY_NAME, "Empty fileName", fileName);
		}
		Pattern p = Pattern.compile("[^A-Za-z0-9._-]");
		Matcher m = p.matcher(fileName);
		if (m.find()) {
			logger.info("There is a forbidden character in the FileName ");
			throw new RmesNotAcceptableException(ErrorCodes.DOCUMENT_FORBIDDEN_CHARATER_NAME,
					"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens",
					fileName);
		}

	}

	public Response downloadDocument(String id) throws RmesException {
		JSONObject jsonDoc = getDocument(id);
		String url = getDocumentUrlFromDocument(jsonDoc);

		Path path = Paths.get(url.replace(SCHEME_FILE, ""));
		ContentDisposition content = null;
		try (InputStream is = new FileInputStream(path.toFile()))
		{
			String fileName = getDocumentNameFromUrl(url);
			content = ContentDisposition.type("attachment").fileName(fileName).build();
			return Response.ok(is).header("Content-Disposition", content).build();

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Error downloading file");
		}
	}

}
