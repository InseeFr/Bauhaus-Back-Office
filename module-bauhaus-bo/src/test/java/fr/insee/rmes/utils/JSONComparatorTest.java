package fr.insee.rmes.utils;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONComparatorTest {

    @Test
    void shouldCompareTwoJsonObjectsAccordingToAField() {

        JSONComparator jsonComparator = new JSONComparator("id");
        JSONObject json1 = new JSONObject().put("id","2025").put("creator","Unknown");
        JSONObject json2 = new JSONObject().put("id","2026");
        JSONObject json3 = new JSONObject().put("creator","Not found");
        JSONObject json4 = new JSONObject().put("id","2027").put("creator","Unknown");
        JSONObject json5 = new JSONObject().put("id","2028").put("creator","Not found").put("publisher","Not found");

        int actualCompareJson1AndJson2 = jsonComparator.compare(json1,json2);
        int actualCompareJson2AndJson1 = jsonComparator.compare(json2,json1);
        int actualCompareJson1AndJson1 = jsonComparator.compare(json1,json1);
        int actualCompareJson1AndJson3 = jsonComparator.compare(json1,json3);
        int actualCompareJson1AndJson4 = jsonComparator.compare(json1,json4);
        int actualCompareJson1AndJson5 = jsonComparator.compare(json1,json5);

        List<Integer> actualResults = List.of(actualCompareJson1AndJson2,actualCompareJson2AndJson1,actualCompareJson1AndJson1,actualCompareJson1AndJson3,actualCompareJson1AndJson4,actualCompareJson1AndJson5);
        List<Integer> expectedResults = List.of(-1,1,0,4,-2,-3);
        assertEquals(expectedResults,actualResults);
        }










    }

