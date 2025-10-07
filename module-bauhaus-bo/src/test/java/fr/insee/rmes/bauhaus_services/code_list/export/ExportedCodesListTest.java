package fr.insee.rmes.bauhaus_services.code_list.export;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExportedCodesListTest {

    List<ExportedCode> list = List.of(new ExportedCode("firstMockedIri","firstMockedCode","firstMockedLabelLg1", "firstMockedLabelLg2"),
    new ExportedCode("secondMockedIri","secondMockedCode","secondMockedLabelLg1", "secondMockedLabelLg2"),
    new ExportedCode("thirdMockedIri","thirdMockedCode","thirdMockedLabelLg1", "thirdMockedLabelLg2"));

    ExportedCodesList exportedCodeList = new ExportedCodesList("mockedNotation","mockedLabelLg1","mockedLabelLg2", list);


    @Test
    void shouldGetterIriValueCorrespondsToTheExpectedValue() {
        assertEquals("mockedNotation", exportedCodeList.notation());
    }

    @Test
    void shouldGetterCodesValueCorrespondsToTheExpectedValue() {
        assertEquals(list, exportedCodeList.codes());
    }

    @Test
    void shouldGetterLabelLg1ValueCorrespondsToTheExpectedValue() {
        assertEquals("mockedLabelLg1", exportedCodeList.labelLg1());
    }

    @Test
    void shouldGetterLabelLg2ValueCorrespondsToTheExpectedValue() {
        assertEquals("mockedLabelLg2", exportedCodeList.labelLg2());
    }

    @Test
    void shouldCheckTheTwoInstancesAreDifferent() {
        ExportedCodesList exportedCodeListOther = new ExportedCodesList("mockedNotation","mockedLabelLg1","mockedLabelLg2", list);
        boolean areDifferent = exportedCodeList!=exportedCodeListOther;
        assertTrue(areDifferent);
    }

    @Test
    void shouldCheckEncapsulatedValuesAreTheSame() {
        ExportedCodesList exportedCodeListOther = new ExportedCodesList("mockedNotation","mockedLabelLg1","mockedLabelLg2", list);
        assertEquals(exportedCodeListOther,exportedCodeList);
    }

}