package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.FileSystemOperation;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentationsRubricsUtilsTest {

    @Test
    void shouldBuildRubricFromJsonReturnNotNullObject() {

        ParentUtils parentUtils = new ParentUtils();
        FileSystemOperation fileSystemOperation = new FileSystemOperation(new ConfigStub());

        DocumentationsRubricsUtils documentationsRubricsUtils = new DocumentationsRubricsUtils();
        DocumentsUtils docUtils = new DocumentsUtils(parentUtils,fileSystemOperation);
        documentationsRubricsUtils.setDocUtils(docUtils);

        boolean forXml= true;

        JSONObject jsonRubric = new JSONObject();
        jsonRubric.put("idAttribute","idAttributeExample");
        jsonRubric.put("value","valueExample");
        jsonRubric.put("labelLg1","labelLg1Example");
        jsonRubric.put("labelLg2","labelLg2Example");
        jsonRubric.put("codeList","codeListExample");
        jsonRubric.put("rangeType","rangeTypeExample");

        JSONObject jsonObjectFirst = new JSONObject();
        jsonObjectFirst.put("element1","element1Example");
        jsonObjectFirst.put("element2","element2Example");

        JSONObject jsonObjectSecond = new JSONObject();
        jsonObjectSecond.put("element3","element3Example");
        jsonObjectSecond.put("element4","element4Example");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObjectFirst).put(jsonObjectSecond);

        jsonRubric.put(Constants.DOCUMENTS_LG1,jsonArray);

        DocumentationRubric response = documentationsRubricsUtils.buildRubricFromJson(jsonRubric,forXml);
        assertNotNull(response);

    }

}