package fr.insee.rmes.modules.concepts.concept.domain;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;

import java.util.List;

public class DomainConceptsService implements ConceptsService {

    private final ConceptsRepository repository;

    public DomainConceptsService(ConceptsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptFetchException {
        return this.repository.getCollectionIdsByConceptId(conceptId);
    }
}
