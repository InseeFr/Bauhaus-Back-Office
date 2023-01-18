package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.ConceptsCollectionsResources;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

public interface ConceptsCollectionService {
    String getCollections() throws RmesException;

    String getCollectionsDashboard() throws RmesException;

    String getCollectionByID(String id) throws RmesException;

    String getCollectionMembersByID(String id) throws RmesException;

    ResponseEntity<?> getCollectionExportODT(String id, String accept, ConceptsCollectionsResources.Language lg, boolean withConcepts, HttpServletResponse response) throws RmesException;

    ResponseEntity<?>  getCollectionExportODS(String id, String accept, boolean withConcepts, HttpServletResponse response) throws RmesException;

    void exportZipCollection(String id, String accept, HttpServletResponse response, ConceptsCollectionsResources.Language lg, String type, boolean withConcepts) throws RmesException;
}
