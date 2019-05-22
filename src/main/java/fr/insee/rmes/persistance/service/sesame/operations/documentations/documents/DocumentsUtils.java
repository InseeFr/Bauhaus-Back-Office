package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import java.io.IOException;
import java.io.InputStream;

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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.ontologies.PAV;
import fr.insee.rmes.persistance.service.sesame.ontologies.SCHEMA;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationRubric;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentsUtils {

	private static final String ID = "id";
	final static Logger logger = LogManager.getLogger(DocumentsUtils.class);


	public void addDocumentsToRubric(Model model, Resource graph, DocumentationRubric rubric, URI textUri) throws RmesException {
		if (rubric.getDocuments() != null && !rubric.getDocuments().isEmpty()) {
			for (Document doc : rubric.getDocuments()) {
				URI url = SesameUtils.toURI(doc.getUrl());
				URI docUri = getDocumentUri(url);
				SesameUtils.addTripleUri(textUri,INSEE.ADDITIONALMATERIAL , docUri, model, graph);
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
	public void createDocument(String id, String body, InputStream documentFile) throws RmesException {

		Resource graph = SesameUtils.documentsGraph();
		Model model = new LinkedHashModel();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id);

		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		// TODO : upload file at URL.

		URI url = SesameUtils.toURI(document.getUrl());//TODO : check if exists => update, not create
		
		URI docUri = getDocumentUri(url);

		SesameUtils.addTripleUri(docUri,RDF.TYPE , FOAF.DOCUMENT, model, graph);
		SesameUtils.addTripleUri(docUri, SCHEMA.URL, url, model, graph);
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
	 * @return
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
	 * Update a document
	 * @throws RmesException
	 */
	public void setDocument(String id, String body) throws RmesException {

		Model model = new LinkedHashModel();
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

		SesameUtils.addTripleUri(docUri,RDF.TYPE , FOAF.DOCUMENT, model, graph);
		SesameUtils.addTripleUri(docUri, SCHEMA.URL, url, model, graph);
		
		if (StringUtils.isNotEmpty(document.getLabelLg1())) {		
			SesameUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg1(),Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLabelLg2())) {		
			SesameUtils.addTripleString(docUri, RDFS.LABEL, document.getLabelLg2(),Config.LG2, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg1())) {		
			SesameUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg1(),Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDescriptionLg2())) {		
			SesameUtils.addTripleString(docUri, RDFS.COMMENT, document.getDescriptionLg2(),Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(document.getLangue())) {		
			SesameUtils.addTripleString(docUri,DC.LANGUAGE, document.getLangue(), model, graph);
		}
		if (StringUtils.isNotEmpty(document.getDateMiseAJour())) {		
			SesameUtils.addTripleDate(docUri,PAV.LASTREFRESHEDON, document.getDateMiseAJour(), model, graph);
		}

		logger.info("Update document : " + document.getUri() + " - " + document.getLabelLg1());
		RepositoryGestion.loadSimpleObject(docUri, model, null);
	}


	public JSONObject getDocument(String id) throws RmesException {
		JSONObject jsonDocs = new JSONObject();
		try {
			jsonDocs = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentQuery(id));
		} catch (RmesException e) {
			logger.error(e.getMessage());
		}		
		return jsonDocs;
	}

}
