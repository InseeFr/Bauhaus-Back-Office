package fr.insee.rmes.persistance.service.sesame;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONObject;

import fr.insee.rmes.persistance.mailSender.MailSenderContract;
import fr.insee.rmes.persistance.mailSender.RmesMailSenderImpl;
import fr.insee.rmes.persistance.service.ConceptsContract;
import fr.insee.rmes.persistance.service.sesame.collections.CollectionsQueries;
import fr.insee.rmes.persistance.service.sesame.collections.CollectionsUtils;
import fr.insee.rmes.persistance.service.sesame.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.service.sesame.concepts.ConceptsUtils;
import fr.insee.rmes.persistance.service.sesame.export.Export;
import fr.insee.rmes.persistance.service.sesame.export.Jasper;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

public class SesameConceptsImpl implements ConceptsContract {
	
	final static Logger logger = Logger.getLogger(SesameConceptsImpl.class);
	
	@Override
	public String getConcepts() {
		logger.info("Starting to get concepts list");
		return RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptsQuery()).toString();
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
		return new ConceptsUtils().setConcept(body);
	}
	
	
	/**
	 * Modify concept
	 */
	@Override
	public void setConcept(String id, String body) {
		new ConceptsUtils().setConcept(id, body);
	}
		

	/**
	 * Create new collection
	 */
	public void setCollection(String body) {
		CollectionsUtils.setCollection(body);
	}
//	
	/**
	 * Modify collection
	 */
	public void setCollection(String id, String body) {
		CollectionsUtils.setCollection(id, body);
	}
	
	/**
	 * Validate concept(s)
	 */
	public void setConceptsValidation(String body) {
		new ConceptsUtils().conceptsValidation(body);
	}
	
	/**
	 * Export concept(s)
	 */
	public Response getConceptExport(String id, String acceptHeader) {
		JSONObject concept = new Export().getConceptData(id);
		Jasper jasper = new Jasper();
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
	public void setCollectionsValidation(String body) {
		CollectionsUtils.collectionsValidation(body);
	}
	
	/**
	 * Export collection(s)
	 */
	public Response getCollectionExport(String id, String acceptHeader) {
		JSONObject collection = new Export().getCollectionData(id);
		Jasper jasper = new Jasper();
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
