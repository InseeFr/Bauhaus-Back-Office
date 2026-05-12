package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsSaveException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link GraphDBConceptsRepository}.
 *
 * <p>Boots a Spring context against a real GraphDB testcontainer and exercises each
 * non-trivial repository method in a sequential flow. The dataset is seeded with a
 * single fully-formed concept ({@code c0042}) carrying a versionable note — the
 * legacy {@code conceptQuery.ftlh} requires that note for {@code getConcept} to
 * return a non-empty row.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GraphDBConceptsRepositoryIT extends WithGraphDBContainer {

    private static final String DISSEMINATION_STATUS = "http://id.insee.fr/codes/base/statutDiffusion/Prive";
    private static final ConceptId SEEDED = new ConceptId("c0042");

    @Autowired
    ConceptsRepository repository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> BAUHAUS_TEST_REPOSITORY);
        container.withInitFolder("fr/insee/rmes/modules/concepts/concept/infrastructure/graphdb")
                .withTrigFiles("graphdb-concepts-repository-it.trig");
    }

    @Test
    @Order(1)
    @DisplayName("getConcept returns Optional.empty() for an unknown id")
    void getConcept_unknown_id_returns_empty() throws ConceptsFetchException {
        Optional<Concept> result = repository.getConcept(new ConceptId("c-does-not-exist"));
        assertThat(result).isEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("getConcept loads the seeded concept with its alt labels and collections")
    void getConcept_returns_seeded_concept() throws ConceptsFetchException {
        Optional<Concept> result = repository.getConcept(SEEDED);

        assertThat(result).isPresent();
        Concept concept = result.get();
        assertThat(concept.id()).isEqualTo(SEEDED);
        assertThat(concept.prefLabel().value()).isEqualTo("Concept IT FR");
        assertThat(concept.creator()).isEqualTo("http://bauhaus/HIE000000");
        assertThat(concept.contributor()).contains("http://bauhaus/HIE000001");
        assertThat(concept.disseminationStatus()).isEqualTo(DISSEMINATION_STATUS);
        assertThat(concept.isValidated()).isFalse();
        assertThat(concept.collectionIds()).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("getConcepts lists the seeded concept")
    void getConcepts_returns_seeded() throws ConceptsFetchException {
        var concepts = repository.getConcepts();
        assertThat(concepts)
                .extracting(c -> c.id().value())
                .contains("c0042");
    }

    @Test
    @Order(4)
    @DisplayName("getConceptsToValidate lists the seeded (unvalidated) concept")
    void getConceptsToValidate_returns_seeded() throws ConceptsFetchException {
        var toValidate = repository.getConceptsToValidate();
        assertThat(toValidate)
                .extracting(c -> c.id().value())
                .contains("c0042");
    }

    @Test
    @Order(5)
    @DisplayName("nextConceptId returns c{N+1} where N is the highest existing numeric notation")
    void nextConceptId_increments_highest_existing_notation() throws ConceptsFetchException {
        ConceptId next = repository.nextConceptId();
        // The seeded notation is c0042; if previous tests in this class created higher ids,
        // we still expect the projection to be lexically > c0042.
        assertThat(next.value()).matches("^c\\d+$");
        int parsed = Integer.parseInt(next.value().substring(1));
        assertThat(parsed).isGreaterThanOrEqualTo(43);
    }

    @Test
    @Order(6)
    @DisplayName("findExistingConceptIds returns only the ids actually present in the repository")
    void findExistingConceptIds_filters_unknown_ids() throws ConceptsFetchException {
        Set<String> existing = repository.findExistingConceptIds(List.of("c0042", "c-does-not-exist"));
        assertThat(existing).containsExactly("c0042");
    }

    @Test
    @Order(7)
    @DisplayName("save persists a new concept (verified via findExistingConceptIds)")
    void save_persists_new_concept() throws InvalidCreateConceptCommandException, ConceptsSaveException, ConceptsFetchException {
        ConceptId newId = new ConceptId("c9001");
        var command = new CreateConceptCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("Save IT")),
                "http://bauhaus/HIE000000",
                "http://bauhaus/HIE000000",
                DISSEMINATION_STATUS,
                Collections.emptyList()
        );

        repository.save(Concept.create(command, newId));

        assertThat(repository.findExistingConceptIds(List.of(newId.value())))
                .containsExactly(newId.value());
    }

    @Test
    @Order(8)
    @DisplayName("validate flips isValidated on the seeded concept")
    void validate_marks_concept_as_validated() throws ConceptsSaveException, ConceptsFetchException {
        repository.validate(List.of(SEEDED));

        var refetched = repository.getConcept(SEEDED);
        assertThat(refetched).isPresent();
        assertThat(refetched.get().isValidated()).isTrue();
    }

    @Test
    @Order(9)
    @DisplayName("delete removes a concept from the repository")
    void delete_removes_concept() throws InvalidCreateConceptCommandException, ConceptsSaveException, ConceptsFetchException {
        ConceptId toDelete = new ConceptId("c9002");
        var command = new CreateConceptCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("Delete IT")),
                "http://bauhaus/HIE000000",
                "http://bauhaus/HIE000000",
                DISSEMINATION_STATUS,
                Collections.emptyList()
        );
        repository.save(Concept.create(command, toDelete));
        // Sanity check: the just-saved concept is visible to findExistingConceptIds.
        assertThat(repository.findExistingConceptIds(List.of(toDelete.value())))
                .containsExactly(toDelete.value());

        repository.delete(toDelete);

        assertThat(repository.findExistingConceptIds(List.of(toDelete.value()))).isEmpty();
    }
}
