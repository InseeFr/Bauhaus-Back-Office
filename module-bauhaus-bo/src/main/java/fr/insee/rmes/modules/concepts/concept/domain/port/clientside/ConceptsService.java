package fr.insee.rmes.modules.concepts.concept.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptFetchException;

import java.util.List;

@ClientSidePort
public interface ConceptsService {
    List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptFetchException;
}
