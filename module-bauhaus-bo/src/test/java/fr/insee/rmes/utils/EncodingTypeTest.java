package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncodingTypeTest {

    @Test
    void shouldReturnValueOfEncodingTypeEnum(){
        String actualMarkdown =EncodingType.MARKDOWN.toString();
        String actualXml =EncodingType.XML.toString();
        assertTrue("MARKDOWN".equals(actualMarkdown) && "XML".equals(actualXml));
    }

}