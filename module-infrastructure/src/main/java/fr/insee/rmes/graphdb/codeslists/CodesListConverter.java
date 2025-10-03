package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;

import java.util.Collections;

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
                codesList.uri(),
                codesList.id(),
                codesList.labelLg1(),
                codesList.labelLg2(),
                codesList.descriptionLg1(),
                codesList.descriptionLg2(),
                codesList.range(),
                codesList.lastCodeUriSegment(),
                codesList.created(),
                codesList.creator(),
                codesList.validationState(),
                codesList.disseminationStatus(),
                codesList.modified(),
                codesList.iriParent(),
                Collections.emptyList()
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
                codesListDomain.getUri(),
                codesListDomain.getId(),
                codesListDomain.getLabelLg1(),
                codesListDomain.getLabelLg2(),
                codesListDomain.getDescriptionLg1(),
                codesListDomain.getDescriptionLg2(),
                codesListDomain.getRange(),
                codesListDomain.getLastCodeUriSegment(),
                codesListDomain.getCreated(),
                codesListDomain.getCreator(),
                codesListDomain.getValidationState(),
                codesListDomain.getDisseminationStatus(),
                codesListDomain.getModified(),
                codesListDomain.getIriParent(),
                codesListDomain.getContributors()
        );
    }
}