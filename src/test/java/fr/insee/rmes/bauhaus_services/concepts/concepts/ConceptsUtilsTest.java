package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.model.concepts.ConceptForExport;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConceptsUtilsTest {

    @Test
    void shouldCreateID() {

        JSONObject json = new JSONObject();
        json.put("id","ID");
        json.put("creator","CREATOR");
        json.put(Constants.NOTATION,"5148");

        String notation = json.getString(Constants.NOTATION);
        int id = Integer.parseInt(notation.substring(1))+1;

        assertEquals("c149","c" + id);

    }

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