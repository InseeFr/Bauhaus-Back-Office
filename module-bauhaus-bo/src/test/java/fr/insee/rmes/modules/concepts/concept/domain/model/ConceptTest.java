package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.MalformedConceptException;
import fr.insee.rmes.modules.concepts.concept.domain.model.commands.CreateConceptCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConceptTest {

    private static final String DISSEMINATION_STATUS = "http://id.insee.fr/codes/base/statutDiffusion/Prive";

    @Test
    void factory_create_initialises_a_new_concept_as_not_validated_with_an_initial_version() throws InvalidCreateConceptCommandException, InvalidConceptIdException {
        var command = new CreateConceptCommand(
                List.of(LocalisedLabel.ofDefaultLanguage("Mon concept")),
                "HIE000000",
                null,
                DISSEMINATION_STATUS,
                List.of("Collection-001")
        );

        Concept concept = Concept.create(command, new ConceptId("c00001"));

        assertThat(concept.id()).isEqualTo(new ConceptId("c00001"));
        assertThat(concept.prefLabel().value()).isEqualTo("Mon concept");
        assertThat(concept.creator()).isEqualTo("HIE000000");
        assertThat(concept.contributor()).isEmpty();
        assertThat(concept.disseminationStatus()).isEqualTo(DISSEMINATION_STATUS);
        assertThat(concept.isValidated()).isFalse();
        assertThat(concept.version()).isEqualTo(ConceptVersion.initial());
        assertThat(concept.modified()).isEmpty();
        assertThat(concept.collectionIds()).containsExactly("Collection-001");
    }

    @Test
    void factory_separates_default_label_from_alternative_labels() throws InvalidCreateConceptCommandException, InvalidConceptIdException {
        var command = new CreateConceptCommand(
                List.of(
                        LocalisedLabel.ofDefaultLanguage("Concept FR"),
                        LocalisedLabel.ofAlternativeLanguage("Concept EN")
                ),
                "HIE000000",
                null,
                DISSEMINATION_STATUS,
                Collections.emptyList()
        );

        Concept concept = Concept.create(command, new ConceptId("c00001"));

        assertThat(concept.prefLabel()).isEqualTo(new LocalisedLabel("Concept FR", Lang.FR));
        assertThat(concept.alternativeLabels()).containsExactly(new LocalisedLabel("Concept EN", Lang.EN));
    }

    @Test
    void direct_constructor_rejects_labels_without_default_language() {
        assertThatThrownBy(() -> new Concept(
                new ConceptId("c00001"),
                List.of(LocalisedLabel.ofAlternativeLanguage("Only EN")),
                "HIE000000",
                null,
                DISSEMINATION_STATUS,
                LocalDateTime.now(),
                null,
                false,
                ConceptVersion.initial(),
                Collections.emptyList()
        )).isInstanceOf(MalformedConceptException.class)
          .hasMessageContaining("default language");
    }
}
