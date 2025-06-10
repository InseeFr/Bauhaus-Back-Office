package fr.insee.rmes.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class XhtmlToMarkdownUtilsTest {

    String sentence = "<p><span style=\"text-decoration: underline;\">This</span> is <span style=\"color: #00ff00;\">an</span> <strong>example</strong> of a<em> text</em> !</p>";
    JSONObject jsonobject = new JSONObject().put("sentence",sentence);

    @Test
    void shouldReturnADifferentTextThanTheOnePassedInParameter() {
        String actual = XhtmlToMarkdownUtils.markdownToXhtml(sentence);
        assertNotEquals(sentence, actual);
    }

    @Test
    void shouldConvertJSONObject() {
        String saveJsonBeforeConversion= jsonobject.toString();
        XhtmlToMarkdownUtils.convertJSONObject(jsonobject);
        assertNotEquals(saveJsonBeforeConversion,jsonobject.toString());
    }

    @Test
    void shouldConvertJSONArray() {
        JSONArray jsonArray = new JSONArray().put("exemple").put(jsonobject);
        String saveJsonArrayBeforeConversion= jsonArray.toString();
        XhtmlToMarkdownUtils.convertJSONArray(jsonArray);
        assertNotEquals(saveJsonArrayBeforeConversion,jsonArray.toString());
    }
    }
