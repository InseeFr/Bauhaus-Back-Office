package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Test
    void shouldCheckEnumValuesAreUnique() {
        List<String> enums = List.of(XhtmlTags.OPENLIST,
                XhtmlTags.CLOSELIST,
                XhtmlTags.OPENLISTITEM,
                XhtmlTags.CLOSELISTITEM,
                XhtmlTags.PARAGRAPH,
                XhtmlTags.OPENUPPERCASE,
                XhtmlTags.CLOSEUPPERCASE
                );

        Set<String> set = new HashSet<>();
        set.addAll(enums);
        assertEquals(set.size(),enums.size());

    }

}



