package fr.insee.rmes.model;

import fr.insee.rmes.model.concepts.PartialConcept;
import fr.insee.rmes.infrastructure.utils.DiacriticSorter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppendableLabelTest {

    @Test
    void appendLabel_shouldAppendRight() {
        String id = "1";
        String label = "label";
        String altLabel1 = "alt 1";
        String altLabel2 = "alt 2";
        PartialConcept partialConcept = new PartialConcept(id, label, altLabel1);
        PartialConcept partialConcept2 = new PartialConcept(id, label, altLabel2);
        var partialConceptAppended = partialConcept.appendObject(partialConcept2);

        assertThat(partialConceptAppended.getClass()).isEqualTo(PartialConcept.class);
        assertThat(partialConceptAppended.altLabel()).hasToString(altLabel1 + " || " + altLabel2);
    }


    @Test
    void appendLabelWithBadRecord_shouldRaiseException() {
        String id = "1";
        String label = "label";

        BadRecord badRecord = new BadRecord(id, label);
        BadRecord other = new BadRecord(id, label);
        assertThatThrownBy(() -> badRecord.appendObject(other))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Method 'public class fr.insee.rmes.model.AppendableLabelTest$BadRecord withAltLabels(String)' not found for 'class fr.insee.rmes.model.AppendableLabelTest$BadRecord'");
    }

    @Test
    void appendLabelWithBadRecord_shouldRaiseExceptionForBadTypeBecauseNull() {
        String id = "1";
        String label = "label";
        String expectedMessage = "Method 'public class fr.insee.rmes.model.AppendableLabelTest$AgainAnOtherBadRecord withAltLabels(String)' for '" + AgainAnOtherBadRecord.class + "' should return a type of " + AgainAnOtherBadRecord.class + " instead of null";

        AgainAnOtherBadRecord badRecord = new AgainAnOtherBadRecord(id, label);
        AgainAnOtherBadRecord other = new AgainAnOtherBadRecord(id, label);
        assertThatThrownBy(() -> badRecord.appendObject(other))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedMessage);
    }

    record BadRecord(String id, String altLabels) implements DiacriticSorter.AppendableLabels<BadRecord> {
    }

    public record AgainAnOtherBadRecord(String id,
                                        String altLabels) implements DiacriticSorter.AppendableLabels<AgainAnOtherBadRecord> {

        public AgainAnOtherBadRecord withAltLabels(@SuppressWarnings("java:S1172") String altLabels) {
            return null;
        }
    }

}