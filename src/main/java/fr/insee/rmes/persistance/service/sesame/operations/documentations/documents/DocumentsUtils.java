package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import java.io.IOException;
import java.util.List;

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
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.ontologies.PAV;
import fr.insee.rmes.persistance.service.sesame.ontologies.SCHEMA;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationRubric;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.operations.Operation;
import fr.insee.rmes.persistance.service.sesame.operations.series.Series;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentsUtils {

	private static final String ID = "id";
	final static Logger logger = LogManager.getLogger(DocumentsUtils.class);


// A adapter pour createDocument
	public void addDocumentsToRubric(Model model, Resource graph, DocumentationRubric rubric, URI textUri) throws RmesException {
		if (rubric.getDocuments() != null && !rubric.getDocuments().isEmpty()) {
			for (DocumentLink doc : rubric.getDocuments()) {
				URI url = SesameUtils.toURI(doc.getUrl());
				URI docUri = getDocumentUri(url, graph);
				SesameUtils.addTripleUri(textUri,INSEE.ADDITIONALMATERIAL , docUri, model, graph);
				SesameUtils.addTripleUri(docUri,RDF.TYPE , FOAF.DOCUMENT, model, graph);
				SesameUtils.addTripleUri(docUri, SCHEMA.URL, url, model, graph);
				if (StringUtils.isNotEmpty(doc.getLabelLg1())) {
					SesameUtils.addTripleString(docUri, RDFS.LABEL, doc.getLabelLg1(),Config.LG1, model, graph);
				}
				if (StringUtils.isNotEmpty(doc.getLabelLg2())) {
					SesameUtils.addTripleString(docUri,RDFS.LABEL, doc.getLabelLg2(),Config.LG2, model, graph);
				}
				if (StringUtils.isNotEmpty(doc.getLang())) {
					SesameUtils.addTripleString(docUri,DC.LANGUAGE, doc.getLang(), model, graph);
				}
				if (StringUtils.isNotEmpty(doc.getLastRefresh())) {
					SesameUtils.addTripleDate(docUri,PAV.LASTREFRESHEDON, doc.getLastRefresh(), model, graph);
				}
			}					
		}
	}

/*
 
	/**
	 * Check the existing id is the same that the id to set
	 * Update only
	 * @param idRequest
	 * @param idSims
	 * @param idOperation
	 * @throws RmesException
	 
	private void checkIdsBeforeUpdate(String idRequest, String idSims, String idOperation) throws RmesException {
		//Check idSims
		if (idRequest==null || idSims == null || !idRequest.equals(idSims)) {
			logger.error("Can't update a documentation if idSims or id don't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idSims can't be null, and must be the same in request", "idSims in param : "+idRequest+" /id in body : "+idSims)	;	
		}
		//Check idOperation
		if (idOperation==null) {
			logger.error("Can't update a documentation if idOperation don't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idOperation can't be null", "idOperation or id is null")	;	
		}
		JSONObject existingIdOperation =  RepositoryGestion.getResponseAsObject(DocumentationsQueries.getDocumentationOperationQuery(idSims));
		if (existingIdOperation == null || existingIdOperation.get("idOperation")==null) {
			logger.error("Can't find operation linked to the documentation");
			throw new RmesNotFoundException("Operation not found", "Maybe this is a creation")	;	
		}
		if (!idOperation.equals(existingIdOperation.get("idOperation"))) {
			logger.error("idOperation and idSims don't match");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idOperation and idSims don't match", "Documentation linked to operation : " + existingIdOperation)	;	
		}
	}
 * 
 * 
 */
	private URI getDocumentUri(URI url, Resource graph) throws RmesException {
		JSONObject uri = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentUriQuery(url, graph));
		if (uri.length()==0 || !uri.has("document")) {
			String id = createDocumentID();
			return SesameUtils.objectIRI(ObjectType.DOCUMENT,id);
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
	 * Get contexts that include a Sims
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getAllGraphsWithSims() throws RmesException {
		return RepositoryGestion.getResponseAsArray(DocumentsQueries.getAllGraphsWithSimsQuery());
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
				//	System.out.println("---------------------\n newDocs: " +newDocs);
			} catch (RmesException e) {
				e.printStackTrace();
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
	 * @return
	 * @throws RmesException
	 */

	public void setDocument(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Document document = new Document(id);
		try {
			document = mapper.readerForUpdating(document).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfDocument(document);
		logger.info("Update document : " + document.getUri() + " - " + document.getLabelLg1());
	}


	public JSONObject getDocument(String id) throws RmesException {
		JSONObject jsonDocs = new JSONObject();
		try {
			jsonDocs = RepositoryGestion.getResponseAsObject(DocumentsQueries.getDocumentQuery(id));
		} catch (RmesException e) {
			e.printStackTrace();
		}		
		return jsonDocs;
	}


	/*
	 * CREATE
	 */
	private void createRdfDocument(Document document) throws RmesException {

		// Dans quel graph ??
		Model model = new LinkedHashModel();
		String uri = document.getUri();
		URI documentURI = SesameUtils.documentIRI(document.getId());
		/*Const*/
		model.add(documentURI, RDF.TYPE, INSEE.DOCUMENT, SesameUtils.operationsGraph());
		/*Required
		model.add(seriesURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(series.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
		/*Optional
		SesameUtils.addTripleString(seriesURI, SKOS.PREF_LABEL, series.getPrefLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		SesameUtils.addTripleString(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		SesameUtils.addTripleString(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		String creator=series.getCreator();
		if (!StringUtils.isEmpty(creator)) {
			SesameUtils.addTripleUri(seriesURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(creator), model, SesameUtils.operationsGraph());
		}
		String gestionnaire=series.getGestionnaire();
		if (!StringUtils.isEmpty(gestionnaire)) {
			SesameUtils.addTripleUri(seriesURI, INSEE.GESTIONNAIRE, organizationsService.getOrganizationUriById(gestionnaire), model, SesameUtils.operationsGraph());
		}

		//partenaires
		List<OperationsLink> contributors = series.getContributor();
		if (contributors != null){
			for (OperationsLink contributor : contributors) {
				if(!contributor.isEmpty()) {
					SesameUtils.addTripleUri(seriesURI, DCTERMS.CONTRIBUTOR,organizationsService.getOrganizationUriById(contributor.getId()),model, SesameUtils.operationsGraph());		
				}
			}
		}

		List<OperationsLink> dataCollectors = series.getDataCollector();
		if (dataCollectors != null) {
			for (OperationsLink dataCollector : dataCollectors) {
				if(!dataCollector.isEmpty()) {
					SesameUtils.addTripleUri(seriesURI, INSEE.DATA_COLLECTOR,organizationsService.getOrganizationUriById(dataCollector.getId()),model, SesameUtils.operationsGraph());
				}		
			}
		}



		if (familyURI != null) {
			//case CREATION : link series to family
			SesameUtils.addTripleUri(seriesURI, DCTERMS.IS_PART_OF, familyURI, model, SesameUtils.operationsGraph());
			SesameUtils.addTripleUri(familyURI, DCTERMS.HAS_PART, seriesURI, model, SesameUtils.operationsGraph());
		}*/

		RepositoryGestion.keepHierarchicalOperationLinks(documentURI,model);

		RepositoryGestion.loadObjectWithReplaceLinks(documentURI, model);
	}

}
