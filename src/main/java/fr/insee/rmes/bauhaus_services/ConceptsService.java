package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.ConceptForAdvancedSearch;
import fr.insee.rmes.model.concepts.MembersLg;
import fr.insee.rmes.model.concepts.PartialConcept;
import fr.insee.rmes.webservice.concepts.ConceptsCollectionsResources;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Concept Service Query interface to assume the persistance of App in JSON
 * 
 * @author I6VWID
 * 
 */

public interface ConceptsService {

	List<PartialConcept> getConcepts() throws RmesException ;
	
	List<ConceptForAdvancedSearch> getConceptsSearch() throws RmesException;
	
	String getConceptsToValidate() throws RmesException;
	
	String getConceptByID(String id) throws RmesException;
	
	String getConceptLinksByID(String id) throws RmesException;
	
	String getConceptNotesByID(String id, int conceptVersion) throws RmesException;
	
	String getCollectionsToValidate() throws RmesException;
	
	String setConcept(String body) throws RmesException;

	void setConcept(String id, String body) throws RmesException;
	
	void setConceptsValidation(String body) throws  RmesException ;

	ResponseEntity<?> exportConcept(String id, String acceptHeader) throws RmesException;

	void exportZipConcept(String id, String acceptHeader, HttpServletResponse response, ConceptsCollectionsResources.Language lg, String type, boolean withConcepts) throws RmesException;

	void setCollection(String body) throws RmesException;
	
	void setCollection(String id, String body) throws  RmesException;
	
	void setCollectionsValidation(String body) throws  RmesException ;
	
	ResponseEntity<?> getCollectionExport(String id, String acceptHeader) throws RmesException ;
	
	String getRelatedConcepts(String id) throws RmesException;

	void deleteConcept(String id) throws RmesException;

	Map<String, InputStream> getConceptsExportIS(List<String> ids, List<MembersLg> members) throws RmesException;
}
