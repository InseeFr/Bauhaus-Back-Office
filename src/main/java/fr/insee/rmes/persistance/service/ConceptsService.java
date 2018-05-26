package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

/**
 * Concept Service Query interface to assume the persistance of App in JSON
 * 
 * @author I6VWID
 * 
 */

public interface ConceptsService {
		
	public String getConcepts();
	
	public String getConceptsSearch();
	
	public String getConceptsToValidate();
	
	public String getConceptByID(String id);
	
	public String getConceptLinksByID(String id);
	
	public String getConceptNotesByID(String id, int conceptVersion);
	
	public String getCollections();
	
	public String getCollectionsDashboard();
	
	public String getCollectionsToValidate();
	
	public String getCollectionByID(String id);
	
	public String getCollectionMembersByID(String id);
	
	public String setConcept(String body);	

	public void setConcept(String id, String body);
	
	public void setConceptsValidation(String body) throws Exception;
	
	public Response getConceptExport(String id, String acceptHeader);
	
	public boolean setConceptSend(String id, String body);
	
	public void setCollection(String body);
	
	public void setCollection(String id, String body);
	
	public void setCollectionsValidation(String body);
	
	public Response getCollectionExport(String id, String acceptHeader);
	
	public boolean setCollectionSend(String id, String body);
}
