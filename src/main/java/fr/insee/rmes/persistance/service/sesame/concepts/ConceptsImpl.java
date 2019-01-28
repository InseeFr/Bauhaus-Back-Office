package fr.insee.rmes.persistance.service.sesame.concepts;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.export.Jasper;
import fr.insee.rmes.persistance.mail_sender.MailSenderContract;
import fr.insee.rmes.persistance.service.ConceptsService;
import fr.insee.rmes.persistance.service.sesame.concepts.collections.CollectionsQueries;
import fr.insee.rmes.persistance.service.sesame.concepts.collections.CollectionsUtils;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class ConceptsImpl implements ConceptsService {

	final static Logger logger = LogManager.getLogger(ConceptsImpl.class);

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
		String resQuery = RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getConceptsSearch()  throws RmesException{
		logger.info("Starting to get concepts list for advanced search");
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsSearchQuery()).toString();
	}

	@Override
	public String getConceptsToValidate()  throws RmesException{
		logger.info("Starting to get provisionals concepts list");
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsToValidateQuery()).toString();
	}
	@Override
	public String getConceptByID(String id)  throws RmesException{
		JSONObject concept = conceptsUtils.getConceptById(id);
		return concept.toString();
	}
	@Override
	public String getRelatedConcepts(String id)  throws RmesException{
		JSONArray resQuery =getRelatedConceptsAsArray(id);
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}
	
	public JSONArray getRelatedConceptsAsArray(String id)  throws RmesException{
		String uriConcept = getConceptUriByID(id);
		return conceptsUtils.getRelatedConcepts(uriConcept);
	}
		
	@Override
	public String deleteConcept(String id) throws RmesException {

		String uriConcept = getConceptUriByID(id);
		JSONArray graphArray = conceptsUtils.getGraphsWithConcept(uriConcept);

		if (graphArray.length()>1) {
			String listGraphs="";
			for (int i=0; i<graphArray.length(); i++) {
				JSONObject currentGraph=(JSONObject) graphArray.get(i);
				listGraphs.concat(currentGraph.getString("src"));
				listGraphs.concat("-");
			}
			throw new RmesUnauthorizedException("The concept "+id+" cannot be deleted because it is used in several graphs.",listGraphs);
		}
		else {
			String listConcepts=getRelatedConcepts(id);
			if(!listConcepts.equals("[]")) {
				throw new RmesUnauthorizedException("The concept "+id+" cannot be deleted because it is linked to other concepts.",listConcepts);
			}
			else {
				JSONObject jsonGraph=(JSONObject) graphArray.get(0);
				String uriGraph = jsonGraph.getString("src");
				Response.Status result= deleteConceptFromGraph(uriConcept,uriGraph);
				String successMessage="The concept "+id+" has been deleted from graph "+uriGraph;
				if (result!= Status.OK) {
					throw new RmesException(402,"Unexpected return message: ",result.toString());
				} else { return successMessage;}
			}
		}
	}
/*
	private JSONArray getConceptVersions(String uriConcept) throws RmesException {
		JSONArray versions = conceptsUtils.getConceptVersions(uriConcept);
		return versions;
	}
*/
	@Override
	public Response.Status deleteConceptFromGraph(String uriConcept, String uriGraph) throws RmesException {
		return conceptsUtils.deleteConcept(uriConcept,uriGraph);
	}

	@Override
	public String getGraphWithConcept(String id)  throws RmesException{
		String uriConcept = getConceptUriByID(id);
		JSONArray graph = conceptsUtils.getGraphsWithConcept(uriConcept);
		return graph.toString();
	}

	@Override
	public String getConceptLinksByID(String id)  throws RmesException{
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id)).toString();
	}

	@Override
	public String getConceptUriByID(String id)  throws RmesException{
		JSONObject json = RepositoryGestion.getResponseAsObject(conceptsUtils.getConceptUriByID(id));
		return json.getString("uri");
	}

	@Override
	public String getConceptNotesByID(String id, int conceptVersion)  throws RmesException{
		return RepositoryGestion.getResponseAsObject(ConceptsQueries.conceptNotesQuery(id, conceptVersion)).toString();
	}

	@Override
	public String getCollections()  throws RmesException{
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionsQuery()).toString();
	}

	@Override
	public String getCollectionsDashboard()  throws RmesException{
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionsDashboardQuery()).toString();
	}

	@Override
	public String getCollectionsToValidate()  throws RmesException{
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionsToValidateQuery()).toString();
	}

	@Override
	public String getCollectionByID(String id)  throws RmesException{
		return RepositoryGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id)).toString();
	}

	@Override
	public String getCollectionMembersByID(String id)  throws RmesException{
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id)).toString();
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
	public void setCollection(String id, String body) throws RmesUnauthorizedException, RmesException {
		collectionsUtils.setCollection(id, body);
	}

	/**
	 * Validate concept(s)
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 */
	public void setConceptsValidation(String body) throws RmesUnauthorizedException, RmesException  {
		conceptsUtils.conceptsValidation(body);
	}

	/**
	 * Export concept(s)
	 */
	public Response getConceptExport(String id, String acceptHeader)  {
		JSONObject concept;
		try {
			concept = conceptsExport.getConceptData(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type("text/plain").build();
		}
		InputStream is = jasper.exportConcept(concept, acceptHeader);
		String fileName = concept.getString("prefLabelLg1") + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader)
				.header("Content-Disposition", content)
				.build();
	}

	/**
	 * Validate collection(s)
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 * @throws Exception 
	 */
	public void setCollectionsValidation(String body) throws RmesUnauthorizedException, RmesException   {
		collectionsUtils.collectionsValidation(body);
	}

	/**
	 * Export collection(s)
	 */
	public Response getCollectionExport(String id, String acceptHeader){
		JSONObject collection = null;
		try {
			collection = conceptsExport.getCollectionData(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type("text/plain").build();
		}
		InputStream is = jasper.exportCollection(collection, acceptHeader);
		String fileName = collection.getString("prefLabelLg1") + jasper.getExtension(acceptHeader);
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
	public boolean setConceptSend(String id, String body) throws RmesUnauthorizedException, RmesException  {
		return mailSender.sendMailConcept(id, body);
	}

	/**
	 * Send collection
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 */
	public boolean setCollectionSend(String id, String body) throws RmesUnauthorizedException, RmesException  {
		return mailSender.sendMailCollection(id, body);
	}

}
