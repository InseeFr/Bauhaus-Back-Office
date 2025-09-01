package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.model.concepts.PartialCollection;
import fr.insee.rmes.onion.infrastructure.webservice.concepts.ConceptsCollectionsResources;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ConceptsCollectionService {
    List<PartialCollection> getCollections() throws RmesException;

    String getCollectionsDashboard() throws RmesException;

    String getCollectionByID(String id) throws RmesException;

    String getCollectionMembersByID(String id) throws RmesException;

    ResponseEntity<?> getCollectionExportODT(String id, String accept, ConceptsCollectionsResources.Language lg, boolean withConcepts, HttpServletResponse response) throws RmesException;

    ResponseEntity<?>  getCollectionExportODS(String id, String accept, boolean withConcepts, HttpServletResponse response) throws RmesException;

    void exportZipCollection(String id, String accept, HttpServletResponse response, ConceptsCollectionsResources.Language lg, String type, boolean withConcepts) throws RmesException;
}
