package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EncodingTypeTest {

    @Test
    void shouldReturnValueOfEncodingTypeEnum() {
        String actualMarkdown = EncodingType.MARKDOWN.toString();
        String actualXml = EncodingType.XML.toString();
        assertTrue("MARKDOWN".equals(actualMarkdown) && "XML".equals(actualXml));
    }
}
