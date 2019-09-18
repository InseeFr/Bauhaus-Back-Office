package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.ontologies.PAV;
import fr.insee.rmes.persistance.service.sesame.ontologies.SCHEMA;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationRubric;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentsUtils {

	@Autowired
	Environment env;

	private static final String ID = "id";
	private static final String URL = "url";
	private static final String URI = "uri";
	final static Logger logger = LogManager.getLogger(DocumentsUtils.class);


	public void addDocumentsToRubric(Model model, Resource graph, DocumentationRubric rubric, URI textUri) throws RmesException {
		if (rubric.getDocuments() != null && !rubric.getDocuments().isEmpty()) {
			for (Document doc : rubric.getDocuments()) {
				URI url = SesameUtils.toURI(doc.getUrl());
				URI docUri = getDocumentUri(url);
				// TODO: Attention si plusieurs doc avec la même url
				SesameUtils.addTripleUri(textUri,INSEE.ADDITIONALMATERIAL , docUri, model, graph);
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
	public JSONArray getListDocumentLink(String idSims, String idRubric) throws RmesException {
		return RepositoryGestion.getResponseAsArray(DocumentsQueries.getDocumentsQuery(idSims,idRubric));
	}


	/**
	 * Get all documents
	 * @return allDocs
	 * @throws RmesException
	 */
	public JSONArray getAllDocuments() throws RmesException {
		JSONArray allDocs = new JSONArray();
		try {
			allDocs = RepositoryGestion.getResponseAsArray(DocumentsQueries.getAllDocumentsQuery());
		} catch (RmesException e) {
			logger.error(e.getMessage());
		}
		return allDocs;
	}


	/**
	 * Generate a new ID for document
	 * @return
	 * @throws RmesException
	 */
	protected String createDocumentID() throws RmesException {
		Boolean noDocInBase = false;
		Boolean noLinkInBase = false;
		int maxDocId = 0;
		int maxLinkId = 0;
		String id = null;

		logger.info("Generate document id");

		JSONObject json = RepositoryGestion.getResponseAsObject(DocumentsQueries.lastDocumentID());
		if (json.length()==0) {noDocInBase= true;}
		else { id = json.getString(ID);
		if (id.equals("undefined") || StringUtils.isEmpty(id)) {noDocInBase= true;}
		else maxDocId = Integer.parseInt(id);}

		json = RepositoryGestion.getResponseAsObject(DocumentsQueries.lastLinkID());
		if (json.length()==0)  {noLinkInBase= true;}
		else {id = json.getString(ID);
		if (id.equals("undefined") || StringUtils.isEmpty(id)) {noLinkInBase= true;}
		else maxLinkId = Integer.parseInt(id);}

		if (noDocInBase & noLinkInBase) {return "1000";}

		return String.valueOf(java.lang.Math.max(maxDocId,maxLinkId)+1);
	}


	/**
	 * Create document
	 * @param id
	 * @param body
	 * @param documentFile
	 * @throws RmesException
	 */
	public void createDocument(String id, String body, InputStream documentFile, String documentName) throws RmesException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id, false);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String url = createFileUrl(documentName);

		document.setUrl(url);
		Path path = Paths.get(url);

		// This check might be useless: Files.copy already throws an Exception if we try to overwrite an existing file
		if (Files.exists(path)) throw new RmesUnauthorizedException("DOCUMENT_EXISTING_FILE", documentName);

		// upload file in storage folder
		try {
			Files.copy(documentFile, path);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		URI docUri = new URIImpl(document.getUri());

		writeRdfDocument(document, docUri);

	}


	/**
	 * Update a document or link
	 * @throws RmesException
	 */
	public void setDocument(String id, String body) throws RmesException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id,isLink(id));

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		//TODO: for a document, check that the url isn't changed ?

		URI docUri =SesameUtils.toURI(document.getUri());

		logger.info("Update document : " + document.getUri() + " - " + document.getLabelLg1() + " / " + document.getLabelLg2());

		writeRdfDocument(document, docUri);

	}


	public JSONObject getDocument(String id) throws RmesException {
		JSONObject jsonDocs = new JSONObject();
		try {
			jsonDocs = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentQuery(id));
		} catch (RmesException e) {
			logger.error(e.getMessage());
		}		
		if (jsonDocs.isNull(URI)) { throw new RmesNotFoundException("Cannot find Document with id: ",id); };
		return jsonDocs;
	}

	public Status deleteDocument(String docId) throws RmesException {

		Resource graph = SesameUtils.documentsGraph();
		JSONObject jsonDoc = getDocument(docId);
		String uri = jsonDoc.getString(URI);
		String url = jsonDoc.getString(URL);
		URI docUri = new URIImpl(uri);

		// Check that the document is not referred to by any sims
		JSONArray jsonResultat = getLinksToDocument(docId);
		if (jsonResultat.length()>0) { 
			throw new RmesUnauthorizedException(
					"The document "+uri+ "cannot be deleted because it is referred to by "+jsonResultat.length()+" sims, including: "+ 
							((JSONObject) jsonResultat.get(0)).get("text").toString(),jsonResultat);
		}
		// remove the physical file
		if(!isLink(docId)) {deleteFile(url);}
		// delete the Document in the rdf base
		return RepositoryGestion.executeUpdate(DocumentsQueries.deleteDocumentQuery(docUri,(URI) graph));
	}

	public JSONArray getLinksToDocument(String docId) throws RmesException {
		return RepositoryGestion.getResponseAsArray(DocumentsQueries.getLinksToDocumentQuery(docId));
	}


	public String changeDocument(String docId, InputStream documentFile, String documentName) throws RmesException {

		String docUrl=getDocumentUrlFromId(docId);

		// clean url with prefix "file:/"
		if (docUrl.startsWith("file:/")) {
			docUrl=docUrl.substring(6);
		}
		// Cannot upload file for a Link
		else {
			if (isLink(docId)) {
				throw new RmesException(406, "Links have no attached file. Cannot upload file "+documentName+" for this document: ",docId);
			}
		}
		// Warning if different file extension 
		String oldExt=StringUtils.substringAfterLast(docUrl, ".");
		String newExt=StringUtils.substringAfterLast(documentName, ".");
		if (!oldExt.equals(newExt)) {
			logger.info("Warning: The new file has extension: ."+newExt+" while the old file had extension: ."+oldExt);
		}

		String oldName=getDocumentNameFromUrl(docUrl);
		String newUrl=null;

		// Same documentName -> keep the same URL
		if (oldName.equals(documentName)) {
			logger.info("Replacing file "+documentName+" at the same Url");
			newUrl=docUrl;
			// upload file in storage folder
			Path path = Paths.get(docUrl);
			try {
				Files.copy(documentFile, path, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		// Different documentName -> create a new URL
		else {
			// Upload the new file
			newUrl=createFileUrl(documentName);
			Path path = Paths.get(newUrl);
			try {
				Files.copy(documentFile, path); // throws an error if a file already exists under this name
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

			// Delete the old file
			deleteFile(docUrl);

			// Update document's url
			if (newUrl.indexOf(':') < 0)  newUrl="file:/"+newUrl;
			changeDocumentsURL(docId,docUrl,newUrl);
		}

		return newUrl;
	}

	/*
	 * LINKS
	 */
	public void createLink(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document link = new Document(id, true);

		try {
			link = mapper.readerForUpdating(link).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String url = link.getUrl();
		if (StringUtils.isEmpty(url)) { throw new RmesNotAcceptableException("A link must have a non-empty url. ",id);}

		//Check if the url is already used by a link
		URI uriUrl= SesameUtils.toURI(url);
		JSONObject uri = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(uriUrl, SesameUtils.documentsGraph()));
		if (uri.length()>0 ) {
			throw new RmesNotAcceptableException("LINK_EXISTING_URL",
					uri.getString("document"));
		}


		URI docUri = new URIImpl(link.getUri());

		writeRdfDocument(link, docUri);

	}


	/**
	 * Write a document in rdf database
	 * @param document
	 * @param docUri
	 * @throws RmesException
	 */

	private void writeRdfDocument(Document document, URI docUri) throws RmesException {

		Resource graph = SesameUtils.documentsGraph();
		Model model = new LinkedHashModel();

		SesameUtils.addTripleUri(docUri,RDF.TYPE , FOAF.DOCUMENT, model, graph);

		String uriString= document.getUrl();
		if (uriString.indexOf(':') < 0)  uriString="file:/"+uriString;
		SesameUtils.addTripleUri(docUri, SCHEMA.URL, uriString, model, graph);
		if (StringUtils.isNotEmpty(document.getLabelLg1())) {
			SesameUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg1(),Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLabelLg2())) {
			SesameUtils.addTripleString(docUri,RDFS.LABEL, document.getLabelLg2(),Config.LG2, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg1())) {
			SesameUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg1(),Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg2())) {
			SesameUtils.addTripleString(docUri,RDFS.COMMENT, document.getDescriptionLg2(),Config.LG2, model, graph);
		}

		if (StringUtils.isNotEmpty(document.getLangue())) {
			SesameUtils.addTripleString(docUri,DC.LANGUAGE, document.getLangue(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDateMiseAJour())) {
			SesameUtils.addTripleDate(docUri,PAV.LASTREFRESHEDON, document.getDateMiseAJour(), model, graph);
		}
		RepositoryGestion.loadSimpleObject(docUri, model, null);
	}

	private Response.Status changeDocumentsURL(String docId, String docUrl, String newUrl) throws RmesException {
		Resource graph = SesameUtils.documentsGraph();
		if (docUrl.indexOf(':') < 0)  docUrl="file:/"+docUrl;
		return RepositoryGestion.executeUpdate(DocumentsQueries.changeDocumentUrlQuery(docId,docUrl,newUrl,graph));	
	}

	private void deleteFile(String docUrl) {
		Path path = Paths.get(docUrl);
		try {
			Files.delete(path);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private String getDocumentNameFromUrl (String docUrl) {
		return StringUtils.substringAfterLast(docUrl, "/");
	}

	private String createFileUrl(String name) {
		return Config.DOCUMENTS_STORAGE+"/"+name;
	}

	/**
	 * return new uri if url doesn't exist
	 * @param url
	 * @return
	 * @throws RmesException
	 */
	private URI getDocumentUri(URI url) throws RmesException {
		JSONObject uri = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(url, SesameUtils.documentsGraph()));
		if (uri.length()==0 || !uri.has("document")) {
			String id = createDocumentID();
			return SesameUtils.objectIRI(ObjectType.DOCUMENT,id);
		}
		return SesameUtils.toURI(uri.getString("document"));
	}

	/**
	 * throw exception if url doesn't exist
	 * @param url
	 * @param graph
	 * @return
	 * @throws RmesException
	 */
	private URI getDocumentUriIfExists(URI url, Resource graph) throws RmesException {
		JSONObject uri = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(url, graph));
		if (uri.length()==0 || !uri.has("document")) {
			throw new RmesException(HttpStatus.SC_NOT_FOUND,"No document with URL","");
		}
		return SesameUtils.toURI(uri.getString("document"));
	}

	private String getDocumentUrlFromId(String id) throws RmesException {
		JSONObject jsonDoc = getDocument(id);
		String url = jsonDoc.getString(URL);
		return url;
	}

	private boolean isLink(Document document) {
		String url = document.getUrl();
		if (StringUtils.startsWith(url, Config.DOCUMENTS_GRAPH)) {return false;}
		return true;
	}

	private boolean isLink(String id ) throws RmesException {
		String url = getDocumentUrlFromId(id);
		if (StringUtils.startsWith(url, Config.DOCUMENTS_GRAPH)) {return false;}
		return true;
	}


	public void checkFileNameValidity(String fileName) throws RmesNotAcceptableException {
		if (fileName == null || fileName.trim().isEmpty()) {
       	 throw new RmesNotAcceptableException("Empty fileName", fileName);
        }
		//if (!(Pattern.matches("[a-zA-Z._-]+", fileName))) throw;
		 Pattern p = Pattern.compile("[^A-Za-z0-9._-]");
	     Matcher m = p.matcher(fileName);
	     if (m.find()) {
		        logger.info("There is a forbidden character in the FileName ");
		        throw new RmesNotAcceptableException("FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens", fileName);
	     }
	     
	}


}
