package fr.insee.rmes.bauhaus_services;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

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

	public ResponseEntity<?> exportConcept(String id, String acceptHeader) throws RmesException;

	public void exportZipConcept(String id, String acceptHeader, HttpServletResponse response) throws RmesException;


	public boolean setConceptSend(String id, String body) throws  RmesException ;
	
	public void setCollection(String body) throws RmesException;
	
	public void setCollection(String id, String body) throws  RmesException;
	
	public void setCollectionsValidation(String body) throws  RmesException ;
	
	public ResponseEntity<?> getCollectionExport(String id, String acceptHeader) throws RmesException ;

	public ResponseEntity<?> getCollectionConceptsExport(String id, String acceptHeader) throws RmesException ;
	
	public boolean setCollectionSend(String id, String body) throws  RmesException ;

	public String getRelatedConcepts(String id) throws RmesException;

	public String deleteConcept(String id) throws RmesException;

	Map<String, InputStream> getConceptExportIS(String id) throws RmesException;

	Map<String, InputStream> getCollectionExportIS(String id) throws RmesException;

}
