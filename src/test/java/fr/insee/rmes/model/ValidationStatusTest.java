package fr.insee.rmes.model;

import org.junit.jupiter.api.Test;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class ValidationStatusTest {

    @Test
    void  shouldReturnValidationStatusValues() {
        boolean modified = Objects.equals(ValidationStatus.MODIFIED.getValue(), ValidationStatus.MODIFIED.toString()) && Objects.equals(ValidationStatus.MODIFIED.getValue(), "Modified");
        boolean unpublished = Objects.equals(ValidationStatus.UNPUBLISHED.getValue(), ValidationStatus.UNPUBLISHED.toString()) && Objects.equals(ValidationStatus.UNPUBLISHED.getValue(), "Unpublished");
        boolean validated = Objects.equals(ValidationStatus.VALIDATED.getValue(), ValidationStatus.VALIDATED.toString()) && Objects.equals(ValidationStatus.VALIDATED.getValue(), "Validated");
        assertTrue(modified && unpublished && validated );
    }
}