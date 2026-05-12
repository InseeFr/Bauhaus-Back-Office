package fr.insee.rmes.modules.concepts.concept.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptNotFoundException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.model.CompactConcept;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.UpdateConceptCommand;

import java.util.List;
import java.util.Optional;

@ClientSidePort
public interface ConceptsService {
    List<String> getCollectionIdsByConceptId(String conceptId) throws ConceptsFetchException;

    Optional<Concept> getConcept(ConceptId id) throws ConceptsFetchException;

    List<CompactConcept> getAllConcepts() throws ConceptsFetchException;

    List<ConceptToValidate> getConceptsToValidate() throws ConceptsFetchException;

    List<ConceptDashboardItem> getConceptsDashboard() throws ConceptsFetchException;

    ConceptId createConcept(CreateConceptCommand command) throws ConceptsFetchException, ConceptsSaveException;

    void updateConcept(UpdateConceptCommand command) throws ConceptsFetchException, ConceptsSaveException, ConceptNotFoundException;

    void validateConcepts(List<ConceptId> ids) throws ConceptsFetchException, ConceptsSaveException;

    void deleteConcept(ConceptId id) throws ConceptsFetchException, ConceptsSaveException, ConceptNotFoundException;
}
