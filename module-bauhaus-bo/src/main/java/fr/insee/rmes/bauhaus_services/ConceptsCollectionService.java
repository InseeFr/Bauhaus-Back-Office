package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Language;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface ConceptsCollectionService {
    String getCollectionsDashboard() throws RmesException;

    String getCollectionMembersByID(String id) throws RmesException;

    ResponseEntity<?> getCollectionExportODT(String id, String accept, Language lg, boolean withConcepts, HttpServletResponse response) throws RmesException;

    ResponseEntity<?>  getCollectionExportODS(String id, String accept, boolean withConcepts, HttpServletResponse response) throws RmesException;

    void exportZipCollection(String id, String accept, HttpServletResponse response, Language lg, String type, boolean withConcepts) throws RmesException;
}
