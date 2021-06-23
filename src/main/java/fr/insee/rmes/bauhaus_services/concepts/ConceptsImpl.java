package fr.insee.rmes.bauhaus_services.concepts;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.text.CaseUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionsUtils;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.external_services.export.Jasper;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.model.mail_sender.MailSenderContract;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.XMLUtils;

@Service
public class ConceptsImpl  extends RdfService implements ConceptsService {

	private static final String THE_CONCEPT = "The concept ";


	static final Logger logger = LogManager.getLogger(ConceptsImpl.class);

	
	@Autowired 
	ConceptsUtils conceptsUtils;

	@Autowired 
	CollectionsUtils collectionsUtils;

	@Autowired 
	ConceptsExportBuilder conceptsExport;

	@Autowired
	Jasper jasper;

	@Autowired
	MailSenderContract mailSender;

	@Override
	public String getConcepts()  throws RmesException{
		logger.info("Starting to get concepts list");
		String resQuery = repoGestion.getResponseAsArray(ConceptsQueries.conceptsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getConceptsSearch()  throws RmesException{
		logger.info("Starting to get concepts list for advanced search");
		return repoGestion.getResponseAsArray(ConceptsQueries.conceptsSearchQuery()).toString();
	}

	@Override
	public String getConceptsToValidate()  throws RmesException{
		logger.info("Starting to get provisionals concepts list");
		return repoGestion.getResponseAsArray(ConceptsQueries.conceptsToValidateQuery()).toString();
	}
	
	@Override
	public String getConceptByID(String id)  throws RmesException{
		JSONObject concept = conceptsUtils.getConceptById(id);
		return concept.toString();
	}
	
	@Override
	public String getRelatedConcepts(String id)  throws RmesException{
		String uriConcept = ((SimpleIRI)RdfUtils.objectIRI(ObjectType.CONCEPT,id)).toString();
		JSONArray resQuery = conceptsUtils.getRelatedConcepts(uriConcept);
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}


	/**
	 * @param id
	 * @return String
	 * @throws RmesException
	 */	
	@Override
	public String deleteConcept(String id) throws RmesException {
		String uriConcept = ((SimpleIRI)RdfUtils.objectIRI(ObjectType.CONCEPT,id)).toString();
		JSONArray graphArray = conceptsUtils.getGraphsWithConcept(uriConcept);

		/* check concept isn't used in several graphs */
		if (graphArray.length()>1) {
			String listGraphs="";
			/* list the graphs involved in log */
			for (int i=0; i<graphArray.length(); i++) {
				JSONObject currentGraph=(JSONObject) graphArray.get(i);
				listGraphs = listGraphs.concat(currentGraph.getString("src"));
				listGraphs = listGraphs.concat("-");
			}
			 
			JSONObject details = new JSONObject();
			details.put("idConcept", id);
			details.put("graphs", graphArray);
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_DELETION_SEVERAL_GRAPHS,
					THE_CONCEPT+id+" cannot be deleted because it is used in several graphs.",
					details);
			
		}
		/* Check concept has no link */
		String listConcepts=getRelatedConcepts(id);
		if(!listConcepts.equals("[]")) { 
			JSONObject details = new JSONObject();
			details.put("idConcept", id);
			details.put("linkedConcepts", listConcepts);
			throw new RmesUnauthorizedException(
					ErrorCodes.CONCEPT_DELETION_LINKED,
					THE_CONCEPT+id+" cannot be deleted because it is linked to other concepts.",
					details);
		}
		/* deletion */
		Response.Status result= conceptsUtils.deleteConcept(id);
		String successMessage=THE_CONCEPT+id+" has been deleted from graph "+RdfUtils.conceptGraph();
		if (result!= Status.OK) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR,"Unexpected return message: ",result.toString());
		} else { 
			return successMessage;
		}
	}

	@Override
	public String getConceptLinksByID(String id)  throws RmesException{
		return repoGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id)).toString();
	}

	@Override
	public String getConceptNotesByID(String id, int conceptVersion)  throws RmesException{
		return repoGestion.getResponseAsObject(ConceptsQueries.conceptNotesQuery(id, conceptVersion)).toString();
	}

	@Override
	public String getCollections()  throws RmesException{
		return repoGestion.getResponseAsArray(CollectionsQueries.collectionsQuery()).toString();
	}

	@Override
	public String getCollectionsDashboard()  throws RmesException{
		return repoGestion.getResponseAsArray(CollectionsQueries.collectionsDashboardQuery()).toString();
	}

	@Override
	public String getCollectionsToValidate()  throws RmesException{
		return repoGestion.getResponseAsArray(CollectionsQueries.collectionsToValidateQuery()).toString();
	}

	@Override
	public String getCollectionByID(String id)  throws RmesException{
		return repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id)).toString();
	}

	@Override
	public String getCollectionMembersByID(String id)  throws RmesException{
		return repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id)).toString();
	}



	/**
	 * Create new concept
	 * @throws RmesException 
	 */
	@Override
	public String setConcept(String body) throws RmesException {
		return conceptsUtils.setConcept(body);
	}


	/**
	 * Modify concept
	 * @throws RmesException 
	 */
	@Override
	public void setConcept(String id, String body) throws RmesException {
		conceptsUtils.setConcept(id, body);
	}


	/**
	 * Create new collection
	 * @throws RmesException 
	 */
	@Override
	public void setCollection(String body) throws RmesException {
		collectionsUtils.setCollection(body);
	}
	//	
	/**
	 * Modify collection
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 * @throws Exception 
	 */
	@Override
	public void setCollection(String id, String body) throws RmesException {
		collectionsUtils.setCollection(id, body);
	}

	/**
	 * Validate concept(s)
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 */
	@Override
	public void setConceptsValidation(String body) throws  RmesException  {
		conceptsUtils.conceptsValidation(body);
	}

	/**
	 * Export concept(s)
	 */
	@Override
	public Response getConceptExport(String id, String acceptHeader) throws RmesException  {
		ConceptForExport concept;
		try {
			concept = conceptsExport.getConceptData(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		
		Map<String, String> xmlContent = convertConceptInXml(concept);	
		String fileName = getFileNameForExport(concept);
		return conceptsExport.exportAsResponse(fileName,xmlContent,true,true,true);
	}
	
	@Override
	public InputStream getConceptExportIS(String id) throws RmesException  {
		ConceptForExport concept = conceptsExport.getConceptData(id);
		Map<String, String> xmlContent = convertConceptInXml(concept);
		return conceptsExport.exportAsInputStream(xmlContent,true,true,true);
	}

	private Map<String, String> convertConceptInXml(ConceptForExport concept) {
		String conceptXml = XMLUtils.produceXMLResponse(concept);
		Map<String,String> xmlContent = new HashMap<>();
		xmlContent.put("conceptFile",  conceptXml.replace("ConceptForExport", "Concept"));
		return xmlContent;
	}
	
	

	public String getFileNameForExport(ConceptForExport concept) {
		return CaseUtils.toCamelCase(concept.getPrefLabelLg1(), false)+"-"+concept.getId();
	}
	
	@Override
	public Map<String,InputStream> getConceptExportIS(String id) throws RmesException  {
		ConceptForExport concept = conceptsExport.getConceptData(id);
		Map<String, String> xmlContent = convertConceptInXml(concept);
		String fileName = getFileNameForExport(concept);
		Map<String,InputStream> ret = new HashMap<String, InputStream>();
		ret.put(fileName, conceptsExport.exportAsInputStream(fileName,xmlContent,true,true,true));
		return ret;
	}

	private Map<String, String> convertConceptInXml(ConceptForExport concept) {
		String conceptXml = XMLUtils.produceXMLResponse(concept);
		Map<String,String> xmlContent = new HashMap<>();
		xmlContent.put("conceptFile",  conceptXml.replace("ConceptForExport", "Concept"));
		return xmlContent;
	}
	
	

	/**
	 * Validate collection(s)
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 * @throws Exception 
	 */
	@Override
	public void setCollectionsValidation(String body) throws  RmesException   {
		collectionsUtils.collectionsValidation(body);
	}

	/**
	 * Export collection(s)
	 */
	@Override
	public Response getCollectionExport(String id, String acceptHeader){
		JSONObject collection = null;
		try {
			collection = conceptsExport.getCollectionData(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		InputStream is = jasper.exportCollection(collection, acceptHeader);
		String fileName = collection.getString(Constants.PREF_LABEL_LG1) + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader)
				.header("Content-Disposition", content)
				.build();
	}

	/**
	 * Send concept
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 */
	@Override
	public boolean setConceptSend(String id, String body) throws  RmesException  {
		return mailSender.sendMailConcept(id, body);
	}

	/**
	 * Send collection
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 */
	@Override
	public boolean setCollectionSend(String id, String body) throws  RmesException  {
		return mailSender.sendMailCollection(id, body);
	}

}
