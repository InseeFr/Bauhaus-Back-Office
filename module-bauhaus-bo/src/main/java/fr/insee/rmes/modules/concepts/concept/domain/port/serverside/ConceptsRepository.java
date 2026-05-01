package fr.insee.rmes.modules.concepts.concept.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptFetchException;

import java.util.List;

@ServerSidePort
public interface ConceptsRepository {
    List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptFetchException;
}
