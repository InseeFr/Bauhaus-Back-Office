package fr.insee.rmes.bauhaus_services.operations.famopeserind_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FamOpeSerIndUtilsTest {

    @Test
    void shouldBuildIdLabelTwoLangsFromJson() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.ID,"id2025");
        jsonObject.put(Constants.LABEL_LG1,"fr");
        jsonObject.put(Constants.LABEL_LG2,"en");

        JSONArray creators = new JSONArray();
        creators.put("Other").put("Unknown");

        jsonObject.put(Constants.CREATORS,creators);

        FamOpeSerIndUtils famOpeSerIndUtils = new FamOpeSerIndUtils();

        IdLabelTwoLangs labelTwoLangs  = famOpeSerIndUtils.buildIdLabelTwoLangsFromJson(jsonObject);

        boolean isIdCorrect = ("id2025").equals(labelTwoLangs.getId());
        boolean isLabelLg1Correct = Objects.equals(labelTwoLangs.getLabelLg1(), "fr");
        boolean isLabelLg2Correct = Objects.equals(labelTwoLangs.getLabelLg2(), "en");
        boolean isFirstCreatorCorrect = Objects.equals(labelTwoLangs.getCreators().getFirst(), "Other");
        boolean isSecondCreatorCorrect = Objects.equals(labelTwoLangs.getCreators().getLast(), "Unknown");

        List<Boolean> actual = List.of(isIdCorrect,isLabelLg1Correct,isLabelLg2Correct,isFirstCreatorCorrect,isSecondCreatorCorrect);
        List<Boolean> expected = List.of(true,true,true,true,true);

        assertEquals(expected,actual);

    }

    @Test
    void shouldFixOrganizationsNames() {

        JSONObject jsonObjectBefore = new JSONObject();
        jsonObjectBefore.put(Constants.PUBLISHER,"publishersExample");
        jsonObjectBefore.put(Constants.CONTRIBUTOR,"contributorsExample");
        jsonObjectBefore.put(Constants.DATA_COLLECTOR,"dataCollectorsExample");

        JSONObject jsonObjectAfter = new JSONObject();
        jsonObjectAfter.put(Constants.PUBLISHERS,"publishersExample");
        jsonObjectAfter.put(Constants.CONTRIBUTORS,"contributorsExample");
        jsonObjectAfter.put(Constants.DATA_COLLECTORS,"dataCollectorsExample");

        FamOpeSerIndUtils famOpeSerIndUtils = new FamOpeSerIndUtils();
        famOpeSerIndUtils.fixOrganizationsNames(jsonObjectBefore);

        assertEquals(jsonObjectAfter.toString(),jsonObjectBefore.toString());
    }

    @Test
    void shouldBuildObjectFromJson() {

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(Constants.ID).put(Constants.UNDEFINED).put(Constants.LABEL_LG1);
        FamOpeSerIndUtils famOpeSerIndUtils = new FamOpeSerIndUtils();

        String actual= famOpeSerIndUtils.buildStringListFromJson(jsonArray).toString();
        String expected = "[id, undefined, labelLg1]";

        assertEquals(expected,actual);

    }

}