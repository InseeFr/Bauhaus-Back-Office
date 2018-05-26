package fr.insee.rmes.persistance.service.sesame.concepts;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.persistance.export.Jasper;
import fr.insee.rmes.persistance.mailSender.MailSenderContract;
import fr.insee.rmes.persistance.mailSender.RmesMailSenderImpl;
import fr.insee.rmes.persistance.service.ConceptsService;
import fr.insee.rmes.persistance.service.sesame.concepts.collections.CollectionsQueries;
import fr.insee.rmes.persistance.service.sesame.concepts.collections.CollectionsUtils;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.persistance.service.sesame.export.ConceptsExport;
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
	ConceptsExport conceptsExport;
	
	@Autowired
	Jasper jasper;
	
	@Override
	public String getConcepts() {
		logger.info("Starting to get concepts list");
		String resQuery = RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}
	
	@Override
	public String getConceptsSearch() {
		logger.info("Starting to get concepts list for advanced search");
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsSearchQuery()).toString();
	}
	
	@Override
	public String getConceptsToValidate() {
		logger.info("Starting to get provisionals concepts list");
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsToValidateQuery()).toString();
	}
	@Override
	public String getConceptByID(String id) {
		return RepositoryGestion.getResponseAsObject(ConceptsQueries.conceptQuery(id)).toString();
	}
	
	@Override
	public String getConceptLinksByID(String id) {
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id)).toString();
	}
	
	@Override
	public String getConceptNotesByID(String id, int conceptVersion) {
		return RepositoryGestion.getResponseAsObject(ConceptsQueries.conceptNotesQuery(id, conceptVersion)).toString();
	}
		
	@Override
	public String getCollections() {
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionsQuery()).toString();
	}
	
	@Override
	public String getCollectionsDashboard() {
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionsDashboardQuery()).toString();
	}
	
	@Override
	public String getCollectionsToValidate() {
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionsToValidateQuery()).toString();
	}
	
	@Override
	public String getCollectionByID(String id) {
		return RepositoryGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id)).toString();
	}
	
	@Override
	public String getCollectionMembersByID(String id) {
		return RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id)).toString();
	}
	


	/**
	 * Create new concept
	 */
	@Override
	public String setConcept(String body) {
		return conceptsUtils.setConcept(body);
	}
	
	
	/**
	 * Modify concept
	 */
	@Override
	public void setConcept(String id, String body) throws Exception {
		conceptsUtils.setConcept(id, body);
	}
		

	/**
	 * Create new collection
	 */
	public void setCollection(String body) {
		collectionsUtils.setCollection(body);
	}
//	
	/**
	 * Modify collection
	 */
	public void setCollection(String id, String body) {
		collectionsUtils.setCollection(id, body);
	}
	
	/**
	 * Validate concept(s)
	 */
	public void setConceptsValidation(String body) throws Exception {
		conceptsUtils.conceptsValidation(body);
	}
	
	/**
	 * Export concept(s)
	 */
	public Response getConceptExport(String id, String acceptHeader) {
		JSONObject concept = conceptsExport.getConceptData(id);
		InputStream is = jasper.exportConcept(concept, acceptHeader);
		String fileName = concept.getString("prefLabelLg1") + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader)
				.header("Content-Disposition", content)
				.build();
	}
	
	/**
	 * Validate collection(s)
	 */
	public void setCollectionsValidation(String body) throws Exception {
		collectionsUtils.collectionsValidation(body);
	}
	
	/**
	 * Export collection(s)
	 */
	public Response getCollectionExport(String id, String acceptHeader) {
		JSONObject collection = conceptsExport.getCollectionData(id);
		InputStream is = jasper.exportCollection(collection, acceptHeader);
		String fileName = collection.getString("prefLabelLg1") + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader)
				.header("Content-Disposition", content)
				.build();
	}
	
	/**
	 * Send concept
	 */
	public boolean setConceptSend(String id, String body) {
		// TODO Externalize Impl choice
		MailSenderContract mailSender = new RmesMailSenderImpl();
		return mailSender.sendMailConcept(id, body);
	}
	
	/**
	 * Send collection
	 */
	public boolean setCollectionSend(String id, String body) {
		// TODO Externalize Impl choice
		MailSenderContract mailSender = new RmesMailSenderImpl();
		return mailSender.sendMailCollection(id, body);
	}

}
