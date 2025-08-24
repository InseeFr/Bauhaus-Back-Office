package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class MetadataStructureDefUtilsTest {

    @InjectMocks
    MetadataStructureDefUtils metadataStructureDefUtils = new MetadataStructureDefUtils();

    @MockitoBean
    RepositoryGestion repoGestion;

    JSONObject correctJsonObject = new JSONObject().put(Constants.ID,"Constants.ID").put(Constants.URI,"Constants.URI");
    JSONObject falseJsonObject = new JSONObject().put(Constants.ID,"Constants.ID");
    JSONArray array = new JSONArray().put(correctJsonObject).put(falseJsonObject);

    @Test
    void shouldThrowARmesExceptionWhenGetMetadataAttributeById() throws RmesException {
        String id ="2025";
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getAttributeSpecificationQuery(id))).thenReturn(new JSONObject());
        RmesException exception = assertThrows(RmesException.class, () -> metadataStructureDefUtils.getMetadataAttributeById(id));
        assertTrue(exception.getDetails().contains("Attribute not found"));
    }

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

    @Test
    void shouldGetMetadataAttributesUriWhenAttributesEmpty() throws RmesException {
        when(repoGestion.getResponseAsArray(DocumentationsQueries.getAttributesUriQuery())).thenReturn(new JSONArray());
        Map<String,String> actual = metadataStructureDefUtils.getMetadataAttributesUri();
        assertEquals(new HashMap<>(),actual);
    }

    @Test
    void shouldGetMetadataAttributesUriWhenAttributesNotEmpty() throws RmesException {
       when(repoGestion.getResponseAsArray(DocumentationsQueries.getAttributesUriQuery())).thenReturn(array);
       Map<String,String> actual = metadataStructureDefUtils.getMetadataAttributesUri();
       assertEquals("{CONSTANTS.ID=Constants.URI}",actual.toString());
    }

    @Test
    void shouldThrowARmesExceptionWhenGetMetadataAttributes() throws RmesException {
        when(repoGestion.getResponseAsArray(DocumentationsQueries.getAttributesQuery())).thenReturn(array);
        RmesException exception = assertThrows(RmesException.class, () -> metadataStructureDefUtils.getMetadataAttributes());
        assertTrue(exception.getDetails().contains("At least one attribute don't have range"));
    }

}