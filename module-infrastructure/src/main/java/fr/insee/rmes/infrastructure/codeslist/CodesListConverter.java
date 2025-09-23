package fr.insee.rmes.infrastructure.codeslist;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import fr.insee.rmes.graphdb.codeslists.CodesList;

/**
 * Converter between infrastructure models (CodesList) and domain models (CodesListDomain).
 * This class handles the mapping between different layers to maintain separation of concerns.
 */
public class CodesListConverter {
    
    /**
     * Converts from infrastructure model to domain model.
     * 
     * @param codesList Infrastructure model
     * @return Domain model
     */
    public static CodesListDomain toDomain(CodesList codesList) {
        if (codesList == null) {
            return null;
        }
        
        return new CodesListDomain(
                codesList.id(),
                codesList.uri(),
                codesList.labelLg1(),
                codesList.labelLg2(),
                codesList.range()
        );
    }
    
    /**
     * Converts from domain model to infrastructure model.
     * 
     * @param codesListDomain Domain model
     * @return Infrastructure model
     */
    public static CodesList toInfrastructure(CodesListDomain codesListDomain) {
        if (codesListDomain == null) {
            return null;
        }
        
        return new CodesList(
                codesListDomain.getId(),
                codesListDomain.getUri(),
                codesListDomain.getLabelLg1(),
                codesListDomain.getLabelLg2(),
                codesListDomain.getRange()
        );
    }
}