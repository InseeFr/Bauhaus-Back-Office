package fr.insee.rmes.modules.concepts.concept.domain;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptAlreadyPublishedException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptNotFoundException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.model.PartialConcept;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.UpdateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DomainConceptsService implements ConceptsService {

    private final ConceptsRepository repository;

    public DomainConceptsService(ConceptsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptsFetchException {
        return this.repository.getCollectionIdsByConceptId(conceptId);
    }

    @Override
    public Optional<Concept> getConcept(ConceptId id) throws ConceptsFetchException {
        return this.repository.getConcept(id);
    }

    @Override
    public List<PartialConcept> getAllConcepts() throws ConceptsFetchException {
        return this.repository.getConcepts();
    }

    @Override
    public List<ConceptToValidate> getConceptsToValidate() throws ConceptsFetchException {
        return this.repository.getConceptsToValidate();
    }

    @Override
    public List<ConceptDashboardItem> getConceptsDashboard() throws ConceptsFetchException {
        return this.repository.getConceptsDashboard();
    }

    @Override
    public ConceptId createConcept(CreateConceptCommand command) throws ConceptsFetchException, ConceptsSaveException {
        ConceptId newId = this.repository.nextConceptId();
        Concept concept = Concept.create(command, newId);
        this.repository.save(concept);
        return newId;
    }

    @Override
    public void updateConcept(UpdateConceptCommand command) throws ConceptsFetchException, ConceptsSaveException, ConceptNotFoundException {
        ConceptId conceptId = command.conceptId();
        if (this.repository.getConcept(conceptId).isEmpty()) {
            throw new ConceptNotFoundException("Concept %s not found".formatted(conceptId.value()));
        }
        Concept updated = Concept.create(command, conceptId);
        this.repository.update(updated);
    }

    @Override
    public void validateConcepts(List<ConceptId> ids) throws ConceptsFetchException, ConceptsSaveException, ConceptAlreadyPublishedException {
        if (ids.isEmpty()) return;
        List<String> rawIds = ids.stream().map(ConceptId::value).toList();
        Set<String> existing = this.repository.findExistingConceptIds(rawIds);
        List<String> missing = rawIds.stream().filter(id -> !existing.contains(id)).toList();
        if (!missing.isEmpty()) {
            throw new ConceptsFetchException(
                    new ConceptNotFoundException("Concepts not found: " + String.join(", ", missing)));
        }
        Set<String> alreadyPublished = this.repository.findValidatedConceptIds(rawIds);
        List<String> published = rawIds.stream().filter(alreadyPublished::contains).toList();
        if (!published.isEmpty()) {
            throw new ConceptAlreadyPublishedException("Concepts already published: " + String.join(", ", published));
        }
        this.repository.validate(ids);
    }

    @Override
    public void deleteConcept(ConceptId id) throws ConceptsFetchException, ConceptsSaveException, ConceptNotFoundException {
        if (this.repository.getConcept(id).isEmpty()) {
            throw new ConceptNotFoundException("Concept %s not found".formatted(id.value()));
        }
        this.repository.delete(id);
    }
}
