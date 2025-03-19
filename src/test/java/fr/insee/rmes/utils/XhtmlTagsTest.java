package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XhtmlTagsTest {

    @Test
    void verifyInListItemAndInUpperCaseFunctionalities() {

        String s = "Bauhaus-back-Office";
        boolean responseOne = ("<li>Bauhaus-back-Office</li>").equals(XhtmlTags.inListItem(s));
        boolean responseTwo =("<U>Bauhaus-back-Office</U>").equals(XhtmlTags.inUpperCase(s));
        assertTrue(responseOne && responseTwo);

    }
}