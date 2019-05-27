package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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


	private String getDocumentNameFromUrl (String docUrl) {
		return StringUtils.substringAfterLast(docUrl, "/");
	}

	private String createUrl(String name) {
		return env.getProperty("fr.insee.rmes.bauhaus.storage.document")+"/"+name;
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
			throw new RmesException(HttpStatus.SC_NOT_FOUND,"No document with with URL","");
		}
		return SesameUtils.toURI(uri.getString("document"));
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
		logger.info("Generate document id");
		JSONObject json = RepositoryGestion.getResponseAsObject(DocumentsQueries.lastDocumentID());
		if (json.length()==0) {return "1000";}
		String id = json.getString(ID);
		if (id.equals("undefined")) {return "1000";}
		int newId = Integer.parseInt(id)+1;
		return String.valueOf(newId);
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
		SesameUtils.addTripleUri(docUri, SCHEMA.URL, document.getUrl(), model, graph);
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
		Document document = new Document(id);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		String url = createUrl(documentName);

		document.setUrl(url);
		Path path = Paths.get(url);

		// This check might be useless: Files.copy already throws an Exception if we try to overwrite an existing file
		if (Files.exists(path)) throw new RmesUnauthorizedException("There already exists a document under this name", documentName);

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
	 * Update a document
	 * @throws RmesException
	 */
	public void setDocument(String id, String body) throws RmesException {

		Resource graph = SesameUtils.documentsGraph();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		URI url = SesameUtils.toURI(document.getUrl());
		URI docUri = getDocumentUriIfExists(url, graph);

		logger.info("Update document : " + document.getUri() + " - " + document.getLabelLg1());

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

	private String getDocumentUrlFromId(String id) throws RmesException {
		JSONObject jsonDoc = getDocument(id);
		String url = jsonDoc.getString(URL);
		return url;
	}

	public Status deleteDocument(String docId) throws RmesException {

		Resource graph = SesameUtils.documentsGraph();
		JSONObject jsonDoc = getDocument(docId);
		String uri = jsonDoc.getString(URI);
		String url = jsonDoc.getString(URL);
		URI docUri = new URIImpl(uri);

		deleteFile(url);
		return RepositoryGestion.executeUpdate(DocumentsQueries.deleteDocumentQuery(docUri,(URI) graph));
	}

	public String changeDocument(String docId, InputStream documentFile, String documentName) throws RmesException {

		String docUrl=getDocumentUrlFromId(docId);

		// Warning if different file extension 
		String oldExt=StringUtils.substringAfterLast(docUrl, ".");
		String newExt=StringUtils.substringAfterLast(documentName, ".");
		if (!oldExt.equals(newExt)) {
			logger.info("The new file has extension: ."+newExt+" while the old file had extension: ."+oldExt);
		}

		String oldName=getDocumentNameFromUrl(docUrl);
		String newUrl=null;

		// Same documentName -> keep the same URL
		if (oldName.equals(documentName)) {
			logger.info("Replace file "+documentName+" at the same Url");
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
			newUrl=createUrl(documentName);
			Path path = Paths.get(newUrl);
			try {
				Files.copy(documentFile, path); // throws an error if a file already exists under this name
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

			// Delete the old file
			deleteFile(docUrl);

			// Update doc's url
			changeDocumentsURL(docId,docUrl,newUrl);
		}

		return newUrl;
	}


	private Response.Status changeDocumentsURL(String docId, String docUrl, String newUrl) throws RmesException {
		Resource graph = SesameUtils.documentsGraph();
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

}
