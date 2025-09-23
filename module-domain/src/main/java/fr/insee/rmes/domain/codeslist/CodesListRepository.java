package fr.insee.rmes.domain.codeslist;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;

import java.util.List;

/**
 * Repository port for codes list data access.
 * This is a port (interface) in hexagonal architecture - no framework dependencies.
 */
public interface CodesListRepository {
    
    /**
     * Retrieves all codes lists with optional filtering by partial/complete types.
     * 
     * @param partial if true, retrieves partial codes lists (skos:Collection),
     *                if false, retrieves complete codes lists (skos:ConceptScheme)
     * @return List of CodesListDomain objects (pure domain model)
     */
    List<CodesListDomain> findAllCodesLists(boolean partial);
}