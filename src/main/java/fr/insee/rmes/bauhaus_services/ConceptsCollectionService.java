package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.ConceptsResources;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

public interface ConceptsCollectionService {
    ResponseEntity<?> getCollectionExportODT(String id, String accept, ConceptsResources.Language lg) throws RmesException;

    ResponseEntity<?>  getCollectionExportODS(String id, String accept) throws RmesException;

    void exportZipCollection(String id, String accept, HttpServletResponse response, ConceptsResources.Language lg, String type) throws RmesException;
}
