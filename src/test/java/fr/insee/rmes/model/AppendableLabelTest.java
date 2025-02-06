package fr.insee.rmes.model;

import fr.insee.rmes.model.concepts.PartialConcept;
import org.junit.jupiter.api.Test;

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
        var partialConceptAppended = partialConcept.appendLabel(partialConcept2);

        assertThat(partialConceptAppended.getClass()).isEqualTo(PartialConcept.class);
        assertThat(partialConceptAppended.altLabels()).hasToString(altLabel1 + " || " + altLabel2);
    }


    @Test
    void appendLabelWithBadRecord_shouldRaiseException() {
        String id = "1";
        String label = "label";

        BadRecord badRecord = new BadRecord(id, label);
        BadRecord other = new BadRecord(id, label);
        assertThatThrownBy(() -> badRecord.appendLabel(other))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Method 'withAltLabels' not found for class");
    }

    @Test
    void appendLabelWithBadRecord_shouldRaiseExceptionForBadType() {
        String id = "1";
        String label = "label";
        String expectedMessage = "Method 'withAltLabels' from class " + OtherBadRecord.class + " should return a type of " + OtherBadRecord.class + " instead of " + Object.class;

        OtherBadRecord badRecord = new OtherBadRecord(id, label);
        OtherBadRecord other = new OtherBadRecord(id, label);
        assertThatThrownBy(() -> badRecord.appendLabel(other))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void appendLabelWithBadRecord_shouldRaiseExceptionForBadTypeBecauseNull() {
        String id = "1";
        String label = "label";
        String expectedMessage = "Method 'withAltLabels' from class " + AgainAnOtherBadRecord.class + " should return a type of " + AgainAnOtherBadRecord.class + " instead of null";

        AgainAnOtherBadRecord badRecord = new AgainAnOtherBadRecord(id, label);
        AgainAnOtherBadRecord other = new AgainAnOtherBadRecord(id, label);
        assertThatThrownBy(() -> badRecord.appendLabel(other))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedMessage);
    }

    record BadRecord(String id, String altLabels) implements AppendableLabel<BadRecord> {
    }

    public record OtherBadRecord(String id, String altLabels) implements AppendableLabel<OtherBadRecord> {
        public Object withAltLabels(String altLabels) {
            return new Object();
        }
    }

    public record AgainAnOtherBadRecord(String id, String altLabels) implements AppendableLabel<AgainAnOtherBadRecord> {
        public AgainAnOtherBadRecord withAltLabels(String altLabels) {
            return null;
        }
    }

}