package fr.insee.rmes.modules.concepts.concept.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.model.PartialConcept;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ServerSidePort
public interface ConceptsRepository {
    List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptsFetchException;

    Optional<Concept> getConcept(ConceptId id) throws ConceptsFetchException;

    List<PartialConcept> getConcepts() throws ConceptsFetchException;

    List<ConceptToValidate> getConceptsToValidate() throws ConceptsFetchException;

    List<ConceptDashboardItem> getConceptsDashboard() throws ConceptsFetchException;

    ConceptId nextConceptId() throws ConceptsFetchException;

    void save(Concept concept) throws ConceptsSaveException;

    void update(Concept concept) throws ConceptsSaveException;

    Set<String> findExistingConceptIds(List<String> ids) throws ConceptsFetchException;

    Set<String> findValidatedConceptIds(List<String> ids) throws ConceptsFetchException;

    void validate(List<ConceptId> ids) throws ConceptsSaveException;

    void delete(ConceptId id) throws ConceptsSaveException;
}
