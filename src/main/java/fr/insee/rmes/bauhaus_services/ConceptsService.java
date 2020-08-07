package fr.insee.rmes.bauhaus_services;

import javax.ws.rs.core.Response;

import fr.insee.rmes.exceptions.RmesException;

/**
 * Concept Service Query interface to assume the persistance of App in JSON
 * 
 * @author I6VWID
 * 
 */

public interface ConceptsService {
		
	public String getConcepts() throws RmesException ;
	
	public String getConceptsSearch() throws RmesException;
	
	public String getConceptsToValidate() throws RmesException;
	
	public String getConceptByID(String id) throws RmesException;
	
	public String getConceptLinksByID(String id) throws RmesException;
	
	public String getConceptNotesByID(String id, int conceptVersion) throws RmesException;
	
	public String getCollections() throws RmesException;
	
	public String getCollectionsDashboard() throws RmesException;
	
	public String getCollectionsToValidate() throws RmesException;
	
	public String getCollectionByID(String id) throws RmesException;
	
	public String getCollectionMembersByID(String id) throws RmesException;
	
	public String setConcept(String body) throws RmesException;	

	public void setConcept(String id, String body) throws RmesException;
	
	public void setConceptsValidation(String body) throws  RmesException ;
	
	public Response getConceptExport(String id, String acceptHeader);
	
	public boolean setConceptSend(String id, String body) throws  RmesException ;
	
	public void setCollection(String body) throws RmesException;
	
	public void setCollection(String id, String body) throws  RmesException;
	
	public void setCollectionsValidation(String body) throws  RmesException ;
	
	public Response getCollectionExport(String id, String acceptHeader) ;
	
	public boolean setCollectionSend(String id, String body) throws  RmesException ;

	public String getRelatedConcepts(String id) throws RmesException;

	public String deleteConcept(String id) throws RmesException;

}
