package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataStructureDefUtilsTest {

    MetadataStructureDefUtils metadataStructureDefUtils = new MetadataStructureDefUtils();

    @Test
    void shouldThrowARmesExceptionWhenTransformRangeType() {
        JSONObject mas = new JSONObject().put("keyExample","valueExample");
        RmesException exception = assertThrows(RmesException.class, () -> metadataStructureDefUtils.transformRangeType(mas));
        assertTrue(exception.getDetails().contains("At least one attribute don't have range"));
    }

    @Test
    void shouldThrowAnIllegalArgumentExceptionWhenTransformRangeType(){
        JSONObject mas = new JSONObject().put("range","rangeValue");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> metadataStructureDefUtils.transformRangeType(mas));
        assertTrue(exception.getMessage().contains("Not a valid (absolute) IRI:"));
    }

    @Test
    void shouldTransformRangeType() throws RmesException {
        JSONObject jsonObjectBefore = new JSONObject().put("range","urn:example:example").put(Constants.CODELIST,"value");
        metadataStructureDefUtils.transformRangeType(jsonObjectBefore);
        boolean isRangeKeyRemoved=!jsonObjectBefore.has("range");
        boolean isConstantsCodeListKeyRemoved=!jsonObjectBefore.has(Constants.CODELIST);
        boolean isConstantsRangeTypeKeyAdded=jsonObjectBefore.has(Constants.RANGE_TYPE);
        Assertions.assertTrue(isRangeKeyRemoved && isConstantsCodeListKeyRemoved && isConstantsRangeTypeKeyAdded);
    }
}