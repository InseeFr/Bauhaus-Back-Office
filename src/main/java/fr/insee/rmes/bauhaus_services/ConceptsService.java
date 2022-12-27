package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.ConceptsResources;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Map;

/**
 * Concept Service Query interface to assume the persistance of App in JSON
 * 
 * @author I6VWID
 * 
 */

public interface ConceptsService {
		
	String getConcepts() throws RmesException ;
	
	String getConceptsSearch() throws RmesException;
	
	String getConceptsToValidate() throws RmesException;
	
	String getConceptByID(String id) throws RmesException;
	
	String getConceptLinksByID(String id) throws RmesException;
	
	String getConceptNotesByID(String id, int conceptVersion) throws RmesException;
	
	String getCollections() throws RmesException;
	
	String getCollectionsDashboard() throws RmesException;
	
	String getCollectionsToValidate() throws RmesException;
	
	String getCollectionByID(String id) throws RmesException;
	
	String getCollectionMembersByID(String id) throws RmesException;
	
	String setConcept(String body) throws RmesException;

	void setConcept(String id, String body) throws RmesException;
	
	void setConceptsValidation(String body) throws  RmesException ;

	ResponseEntity<?> exportConcept(String id, String acceptHeader) throws RmesException;

	void exportZipConcept(String id, String acceptHeader, HttpServletResponse response) throws RmesException;
	
	void setCollection(String body) throws RmesException;
	
	void setCollection(String id, String body) throws  RmesException;
	
	void setCollectionsValidation(String body) throws  RmesException ;
	
	ResponseEntity<?> getCollectionExport(String id, String acceptHeader) throws RmesException ;
	
	String getRelatedConcepts(String id) throws RmesException;

	String deleteConcept(String id) throws RmesException;

	Map<String, InputStream> getConceptExportIS(String id) throws RmesException;

	Map<String, InputStream> getCollectionExportIS(String id) throws RmesException;
}
