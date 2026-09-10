package fr.insee.rmes.modules.concepts.concept.domain.model.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidCreateConceptCommandException;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateConceptCommandTest {

    private static final String DISSEMINATION_STATUS = "http://id.insee.fr/codes/base/statutDiffusion/Prive";
    private static final List<LocalisedLabel> ONE_FR_LABEL = List.of(LocalisedLabel.ofDefaultLanguage("Concept FR"));

    @Test
    void should_expose_concept_id() throws InvalidCreateConceptCommandException, InvalidConceptIdException {
        var command = new UpdateConceptCommand(
                "c00001", ONE_FR_LABEL, "HIE000000", null, DISSEMINATION_STATUS, Collections.emptyList());

        assertThat(command.conceptId()).isEqualTo(new ConceptId("c00001"));
    }

    @Test
    void should_reject_invalid_id() {
        assertThrows(
                InvalidConceptIdException.class,
                () -> new UpdateConceptCommand(
                        "", ONE_FR_LABEL, "HIE000000", null, DISSEMINATION_STATUS, Collections.emptyList()));
    }

    @Test
    void should_propagate_invariant_violations_from_parent() {
        assertThrows(
                InvalidCreateConceptCommandException.class,
                () -> new UpdateConceptCommand(
                        "c00001",
                        Collections.emptyList(),
                        "HIE000000",
                        null,
                        DISSEMINATION_STATUS,
                        Collections.emptyList()));
    }
}
