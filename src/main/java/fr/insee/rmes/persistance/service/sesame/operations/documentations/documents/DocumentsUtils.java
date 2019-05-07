package fr.insee.rmes.persistance.service.sesame.operations.documentations.documents;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

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
	 * Get documents in a Sims
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getAllDocumentsInSims(String idSims) throws RmesException {
		return RepositoryGestion.getResponseAsArray(DocumentsQueries.getAllDocumentsQuery(idSims));
	}
	
	/**
	 * Get all documents
	 * @return
	 * @throws RmesException
	 */
	public JSONArray getAllDocuments() throws RmesException {
		JSONArray allSims = getAllGraphsWithSims();
		JSONArray allDocs = new JSONArray();
		
		allSims.forEach(sims -> {
			String idSims = ((JSONObject) sims).getString("sims");
			JSONArray newDocs = new JSONArray();
			try {
				newDocs = RepositoryGestion.getResponseAsArray(DocumentsQueries.getAllDocumentsQuery(idSims));
			} catch (RmesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			allDocs.put(newDocs);
		});
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

}
