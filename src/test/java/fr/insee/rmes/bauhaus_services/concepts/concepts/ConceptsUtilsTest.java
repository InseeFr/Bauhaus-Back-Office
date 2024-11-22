package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.model.concepts.ConceptForExport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ConceptsUtilsTest {

    @Test
    void testGetConceptExportFileName() {
        String conceptId = "12345";
        String prefLabelLg1 = "Lg1ConceptLabel";
        String prefLabelLg2 = "Lg2ConceptLabel";
        int maxLength = 10;

        ConceptForExport concept = new ConceptForExport();
        concept.setId(conceptId);
        concept.setPrefLabelLg1(prefLabelLg1);
        concept.setPrefLabelLg2(prefLabelLg2);

        ConceptsUtils service = new ConceptsUtils(null, null, maxLength);

        String result = service.getConceptExportFileName(concept);

        assertEquals("12345Lg1co", result);

    }
}