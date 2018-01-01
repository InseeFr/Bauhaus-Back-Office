package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

import fr.insee.rmes.persistance.service.sesame.SesameConceptsImpl;

/**
 * Questionnaire Service to assume the persistance of App in JSON
 * 
 * @author N. Laval
 * 
 *
 */
public class ConceptsService {

	private ConceptsContract conceptContract;

	public ConceptsService() {
		// TODO externalisation of the parameter
		conceptContract = new SesameConceptsImpl();
	}
	
	public String getConcepts() {
		return conceptContract.getConcepts();
	}
	
	public String getConceptsSearch() {
		return conceptContract.getConceptsSearch();
	}
	
	public String getConceptsToValidate() {
		return conceptContract.getConceptsToValidate();
	}
	
	public String getConceptByID(String id) {
		return conceptContract.getConceptByID(id);
	}
	
	public String getConceptLinksByID(String id) {
		return conceptContract.getConceptLinksByID(id);
	}
	
	public String getConceptNotesByID(String id, int conceptVersion) {
		return conceptContract.getConceptNotesByID(id, conceptVersion);
	}
	
	public String getCollections() {
		return conceptContract.getCollections();
	}
	
	public String getCollectionsDashboard() {
		return conceptContract.getCollectionsDashboard();
	}
	
	public String getCollectionsToValidate() {
		return conceptContract.getCollectionsToValidate();
	}
	
	public String getCollectionByID(String id) {
		return conceptContract.getCollectionByID(id);
	}
	
	public String getCollectionMembersByID(String id) {
		return conceptContract.getCollectionMembersByID(id);
	}

	public String setConcept(String body) {
		return conceptContract.setConcept(body);
	}
	
	public void setConcept(String id, String body) {
		conceptContract.setConcept(id, body);
	}
	
	public void setConceptsValidation(String body) {
		conceptContract.setConceptsValidation(body);
	}
	
	public Response getConceptExport(String id, String acceptHeader) {
		return conceptContract.getConceptExport(id, acceptHeader);
	}
	
	public boolean setConceptSend(String id, String body) {
		return conceptContract.setConceptSend(id, body);
	}
	
	public void setCollection(String body) {
		conceptContract.setCollection(body);
	}
	
	public void setCollection(String id, String body) {
		conceptContract.setCollection(id, body);
	}
	
	public void setCollectionsValidation(String body) {
		conceptContract.setCollectionsValidation(body);
	}
	
	public Response getCollectionExport(String id, String acceptHeader) {
		return conceptContract.getCollectionExport(id, acceptHeader);
	}
	
	public boolean setCollectionSend(String id, String body) {
		return conceptContract.setCollectionSend(id, body);
	}
}
