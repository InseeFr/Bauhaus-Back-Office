package fr.insee.rmes.model.operations.documentations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;
import org.junit.jupiter.api.Test;

class DocumentationTest {

    @Test
    void shouldGetIdTargetWhenDocumentationIsImplemented() {
        Documentation documentationWithZeroComponents = new Documentation();
        documentationWithZeroComponents.setIdOperation("");
        documentationWithZeroComponents.setIdSeries("");
        documentationWithZeroComponents.setIdIndicator("");

        Documentation documentationWithIdOperation = new Documentation();
        documentationWithIdOperation.setIdOperation("idOperation");
        documentationWithIdOperation.setIdSeries("");
        documentationWithIdOperation.setIdIndicator("");

        Documentation documentationWithIdSeries = new Documentation();
        documentationWithIdSeries.setIdOperation("");
        documentationWithIdSeries.setIdSeries("idSeries");
        documentationWithIdSeries.setIdIndicator("");

        Documentation documentationWithIdIndicator = new Documentation();
        documentationWithIdIndicator.setIdOperation("");
        documentationWithIdIndicator.setIdSeries("");
        documentationWithIdIndicator.setIdIndicator("idIndicator");

        boolean isGetIdTargetNull = documentationWithZeroComponents.getIdTarget() == null;
        boolean isGetIdTargetIsIdOperation = Objects.equals(documentationWithIdOperation.getIdTarget(), "idOperation");
        boolean isGetIdTargetIsIdSeries = Objects.equals(documentationWithIdSeries.getIdTarget(), "idSeries");
        boolean isGetIdTargetIsIdIndicator = Objects.equals(documentationWithIdIndicator.getIdTarget(), "idIndicator");

        assertTrue(isGetIdTargetNull
                && isGetIdTargetIsIdOperation
                && isGetIdTargetIsIdSeries
                && isGetIdTargetIsIdIndicator);
    }
}
