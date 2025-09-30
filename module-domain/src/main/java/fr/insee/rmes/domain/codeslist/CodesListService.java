package fr.insee.rmes.domain.codeslist;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;

import java.util.List;

/**
 * Domain service for codes list operations.
 * This is a port (interface) in hexagonal architecture - no framework dependencies.
 */
public interface CodesListService {
    
    /**
     * Retrieves all codes lists with optional filtering by partial/complete types.
     * 
     * @param partial if true, retrieves partial codes lists (skos:Collection),
     *                if false, retrieves complete codes lists (skos:ConceptScheme)
     * @return List of CodesListDomain objects (pure domain model)
     */
    List<CodesListDomain> getAllCodesLists(boolean partial);
}