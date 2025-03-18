package fr.insee.rmes.bauhaus_services.structures.utils;

import com.sun.jna.Structure;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.persistance.ontologies.INSEE;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class StructureComponentUtilsTest {

    @Mock
    Structure structures;

    @Test
    void shouldReturnFormatComponent() {

        JSONObject response = new JSONObject();
        response.put("structures", structures);
        response.put("codeList","myCodeList");
        if (response.has(Constants.CODELIST)) {
            response.put("range", RdfUtils.toString(INSEE.CODELIST));
        }
        String expected = "{\"range\":\"http://rdf.insee.fr/def/base#codeList\",\"codeList\":\"myCodeList\"}";

        assertEquals(expected,response.toString());
    }

}