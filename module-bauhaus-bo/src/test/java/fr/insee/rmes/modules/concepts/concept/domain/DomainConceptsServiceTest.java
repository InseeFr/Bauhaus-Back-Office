package fr.insee.rmes.modules.concepts.concept.domain;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptNotFoundException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.model.CompactConcept;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptVersion;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.UpdateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainConceptsServiceTest {

    private static final ConceptId ID = new ConceptId("c00001");
    private static final Concept SAMPLE_CONCEPT = new Concept(
            ID,
            List.of(LocalisedLabel.ofDefaultLanguage("Mon concept")),
            "HIE000000",
            "HIE000000",
            "http://id.insee.fr/codes/base/statutDiffusion/Prive",
            LocalDateTime.of(2026, 1, 1, 10, 0),
            null,
            ValidationStatus.UNPUBLISHED,
            ConceptVersion.initial(),
            Collections.emptyList()
    );

    ConceptsRepository conceptsRepository;
    DomainConceptsService domainConceptsService;

    @BeforeEach
    void setUp() {
        conceptsRepository = Mockito.mock(ConceptsRepository.class);
        domainConceptsService = new DomainConceptsService(conceptsRepository);
    }

    @Test
    void should_return_collection_ids_for_concept() throws ConceptsFetchException {
        when(conceptsRepository.getCollectionIdsByConceptId("c00001"))
                .thenReturn(List.of("collection-id-1", "collection-id-2"));

        var result = domainConceptsService.getCollectionIdsByConceptId("c00001");

        assertThat(result).containsExactlyInAnyOrder("collection-id-1", "collection-id-2");
    }

    @Test
    void should_return_empty_list_when_concept_belongs_to_no_collection() throws ConceptsFetchException {
        when(conceptsRepository.getCollectionIdsByConceptId("c00001"))
                .thenReturn(List.of());

        var result = domainConceptsService.getCollectionIdsByConceptId("c00001");

        assertThat(result).isEmpty();
    }

    @Test
    void getConcept_returns_concept_when_present_in_repository() throws ConceptsFetchException {
        when(conceptsRepository.getConcept(ID)).thenReturn(Optional.of(SAMPLE_CONCEPT));

        Optional<Concept> result = domainConceptsService.getConcept(ID);

        assertThat(result).contains(SAMPLE_CONCEPT);
    }

    @Test
    void getConcept_returns_empty_when_id_is_unknown() throws ConceptsFetchException {
        when(conceptsRepository.getConcept(ID)).thenReturn(Optional.empty());

        Optional<Concept> result = domainConceptsService.getConcept(ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllConcepts_returns_compact_list_from_repository() throws ConceptsFetchException {
        var compact1 = new CompactConcept(new ConceptId("c00001"), LocalisedLabel.ofDefaultLanguage("A"));
        var compact2 = new CompactConcept(new ConceptId("c00002"), LocalisedLabel.ofDefaultLanguage("B"));
        when(conceptsRepository.getConcepts()).thenReturn(List.of(compact1, compact2));

        List<CompactConcept> result = domainConceptsService.getAllConcepts();

        assertThat(result).containsExactly(compact1, compact2);
    }

    @Test
    void getConceptsToValidate_delegates_to_repository() throws ConceptsFetchException {
        var item = new ConceptToValidate(new ConceptId("c00001"), "Mon concept", "HIE000000");
        when(conceptsRepository.getConceptsToValidate()).thenReturn(List.of(item));

        List<ConceptToValidate> result = domainConceptsService.getConceptsToValidate();

        assertThat(result).containsExactly(item);
    }

    @Test
    void getConceptsDashboard_delegates_to_repository() throws ConceptsFetchException {
        var item = new ConceptDashboardItem(
                new ConceptId("c00001"), "Mon concept", "2026-01-01T00:00:00", null, ValidationStatus.UNPUBLISHED, "HIE000000");
        when(conceptsRepository.getConceptsDashboard()).thenReturn(List.of(item));

        List<ConceptDashboardItem> result = domainConceptsService.getConceptsDashboard();

        assertThat(result).containsExactly(item);
    }

    @Test
    void createConcept_uses_nextConceptId_from_repository_and_returns_it()
            throws InvalidCreateConceptCommandException, InvalidConceptIdException, ConceptsFetchException, ConceptsSaveException {
        ConceptId generatedId = new ConceptId("c00007");
        when(conceptsRepository.nextConceptId()).thenReturn(generatedId);
        var command = sampleCreateCommand();

        ConceptId result = domainConceptsService.createConcept(command);

        assertThat(result).isEqualTo(generatedId);
    }

    @Test
    void createConcept_saves_concept_built_from_command_with_generated_id()
            throws InvalidCreateConceptCommandException, InvalidConceptIdException, ConceptsFetchException, ConceptsSaveException {
        ConceptId generatedId = new ConceptId("c00007");
        when(conceptsRepository.nextConceptId()).thenReturn(generatedId);
        var command = sampleCreateCommand();

        domainConceptsService.createConcept(command);

        ArgumentCaptor<Concept> captor = ArgumentCaptor.forClass(Concept.class);
        verify(conceptsRepository).save(captor.capture());
        Concept saved = captor.getValue();
        assertThat(saved.id()).isEqualTo(generatedId);
        assertThat(saved.prefLabel().value()).isEqualTo("Mon concept");
        assertThat(saved.creator()).isEqualTo("HIE000000");
        assertThat(saved.disseminationStatus())
                .isEqualTo("http://id.insee.fr/codes/base/statutDiffusion/Prive");
        assertThat(saved.validationState()).isEqualTo(ValidationStatus.UNPUBLISHED);
        assertThat(saved.version()).isEqualTo(ConceptVersion.initial());
    }

    private static CreateConceptCommand sampleCreateCommand() throws InvalidCreateConceptCommandException {
        return new CreateConceptCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("Mon concept")),
                "HIE000000",
                "HIE000000",
                "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                Collections.emptyList()
        );
    }

    private static UpdateConceptCommand sampleUpdateCommand(String id)
            throws InvalidCreateConceptCommandException, InvalidConceptIdException {
        return new UpdateConceptCommand(
                id,
                List.of(LocalisedLabel.ofDefaultLanguage("Mon concept (modifié)")),
                "HIE000000",
                "HIE000000",
                "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                Collections.emptyList()
        );
    }

    @Test
    void updateConcept_updates_repository_when_concept_exists()
            throws InvalidCreateConceptCommandException, InvalidConceptIdException,
            ConceptsFetchException, ConceptsSaveException, ConceptNotFoundException {
        when(conceptsRepository.getConcept(ID)).thenReturn(Optional.of(SAMPLE_CONCEPT));
        var command = sampleUpdateCommand(ID.value());

        domainConceptsService.updateConcept(command);

        ArgumentCaptor<Concept> captor = ArgumentCaptor.forClass(Concept.class);
        verify(conceptsRepository).update(captor.capture());
        Concept updated = captor.getValue();
        assertThat(updated.id()).isEqualTo(ID);
        assertThat(updated.prefLabel().value()).isEqualTo("Mon concept (modifié)");
    }

    @Test
    void updateConcept_throws_ConceptNotFoundException_when_id_is_unknown()
            throws InvalidCreateConceptCommandException, InvalidConceptIdException,
            ConceptsFetchException, ConceptsSaveException {
        when(conceptsRepository.getConcept(ID)).thenReturn(Optional.empty());
        var command = sampleUpdateCommand(ID.value());

        assertThatThrownBy(() -> domainConceptsService.updateConcept(command))
                .isInstanceOf(ConceptNotFoundException.class)
                .hasMessageContaining(ID.value());
        verify(conceptsRepository, never()).update(any());
    }

    @Test
    void validateConcepts_delegates_to_repository_when_all_ids_exist()
            throws ConceptsFetchException, ConceptsSaveException {
        var id1 = new ConceptId("c00001");
        var id2 = new ConceptId("c00002");
        when(conceptsRepository.findExistingConceptIds(List.of("c00001", "c00002")))
                .thenReturn(Set.of("c00001", "c00002"));

        domainConceptsService.validateConcepts(List.of(id1, id2));

        verify(conceptsRepository).validate(List.of(id1, id2));
    }

    @Test
    void validateConcepts_throws_when_at_least_one_id_is_missing()
            throws ConceptsFetchException {
        var id1 = new ConceptId("c00001");
        var id2 = new ConceptId("c00404");
        when(conceptsRepository.findExistingConceptIds(List.of("c00001", "c00404")))
                .thenReturn(Set.of("c00001"));

        assertThatThrownBy(() -> domainConceptsService.validateConcepts(List.of(id1, id2)))
                .isInstanceOf(ConceptsFetchException.class)
                .hasCauseInstanceOf(ConceptNotFoundException.class)
                .hasMessageContaining("c00404");
    }

    @Test
    void validateConcepts_is_a_noop_on_empty_input() throws ConceptsFetchException, ConceptsSaveException {
        domainConceptsService.validateConcepts(List.of());

        verify(conceptsRepository, never()).validate(any());
        verify(conceptsRepository, never()).findExistingConceptIds(any());
    }

    @Test
    void deleteConcept_calls_repository_when_id_exists()
            throws ConceptsFetchException, ConceptsSaveException, ConceptNotFoundException {
        when(conceptsRepository.getConcept(ID)).thenReturn(Optional.of(SAMPLE_CONCEPT));

        domainConceptsService.deleteConcept(ID);

        verify(conceptsRepository).delete(ID);
    }

    @Test
    void deleteConcept_throws_ConceptNotFoundException_when_id_is_unknown()
            throws ConceptsFetchException, ConceptsSaveException {
        when(conceptsRepository.getConcept(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> domainConceptsService.deleteConcept(ID))
                .isInstanceOf(ConceptNotFoundException.class)
                .hasMessageContaining(ID.value());
        verify(conceptsRepository, never()).delete(any());
    }
}
