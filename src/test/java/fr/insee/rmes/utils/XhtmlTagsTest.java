package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class XhtmlTagsTest {

    String s = "Bauhaus-back-Office";

    @Test
    void verifyInUpperCaseFunctionalities() {
        assertEquals(("<U>Bauhaus-back-Office</U>"), XhtmlTags.inUpperCase(s));
    }

    @Test
    void verifyInListFunctionalities3() {
        String actual = XhtmlTags.inListItem(s);
        assertTrue(actual.startsWith("<li>") && actual.endsWith("</li>"));
    }
}



