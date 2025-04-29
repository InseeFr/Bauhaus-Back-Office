package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XsltUtilsTest {

    @Test
    void shouldBuildParamsWithCorrectDisplay() {
        String target= "An example of target";
        String trueTrueTrue=  XsltUtils.buildParams(true,true,true,target);
        String trueTrueFalse=  XsltUtils.buildParams(true,true,false,target);
        String trueFalseTrue=  XsltUtils.buildParams(true,false,true,target);
        String trueFalseFalse=  XsltUtils.buildParams(true,false,false,target);
        String falseTrueTrue=  XsltUtils.buildParams(false,true,true,target);
        String falseTrueFalse=  XsltUtils.buildParams(false,true,false,target);
        String falseFalseTrue=  XsltUtils.buildParams(false,false,true,target);
        String falseFalseFalse=  XsltUtils.buildParams(false,false,false,target);

        List<String> results = List.of(trueTrueTrue,trueTrueFalse,trueFalseTrue,trueFalseFalse,falseTrueTrue,falseTrueFalse,falseFalseTrue,falseFalseFalse);
        List<Boolean> actualIncludeEmptyFields = new ArrayList<>();
        List<Boolean> actualLanguage1 = new ArrayList<>();
        List<Boolean> actualLanguage2 = new ArrayList<>();

        for (String element : results){
            actualIncludeEmptyFields.add(element.contains("</includeEmptyFields>"));
            actualLanguage1.add(element.contains("<language id=\"Fr\">1</language>"));
            actualLanguage2.add(element.contains("<language id=\"En\">2</language>"));
        }

        boolean actualCorrespondToExpectedIncludeEmptyFields = List.of(true,true,true,true,true,true,true,true).equals(actualIncludeEmptyFields);
        boolean actualCorrespondToExpectedLanguage1= List.of(true,true,true,true,false,false,false,false).equals(actualLanguage1);
        boolean actualCorrespondToExpectedLanguage2= List.of(true,true,false,false,true,true,false,false).equals(actualLanguage2);

        assertTrue(actualCorrespondToExpectedIncludeEmptyFields && actualCorrespondToExpectedLanguage1 && actualCorrespondToExpectedLanguage2);

    }
}