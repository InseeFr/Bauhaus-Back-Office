package fr.insee.rmes.bauhaus_services.code_list.export;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExportedCodeTest {

    @Test
    void shouldGetterIriValueCorrespondsToTheExpectedValue() {
        ExportedCode exportedCode = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        assertEquals("mockedIri", exportedCode.iri());
    }

    @Test
    void shouldGetterCodeValueCorrespondsToTheExpectedValue() {
        ExportedCode exportedCode = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        assertEquals("mockedCode", exportedCode.code());
    }

    @Test
    void shouldGetterLabelLg1ValueCorrespondsToTheExpectedValue() {
        ExportedCode exportedCode = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        assertEquals("mockedLabelLg1", exportedCode.labelLg1());
    }

    @Test
    void shouldGetterLabelLg2ValueCorrespondsToTheExpectedValue() {
        ExportedCode exportedCode = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        assertEquals("mockedLabelLg2", exportedCode.labelLg2());
    }

    @Test
    void shouldCheckTheTwoInstancesAreDifferent() {
        ExportedCode exportedCode = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        ExportedCode exportedCodeOther = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        boolean areDifferent = exportedCode!=exportedCodeOther;
        assertTrue(areDifferent);
    }

    @Test
    void shouldCheckEncapsulatedValuesAreTheSame() {
        ExportedCode exportedCode = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        ExportedCode exportedCodeOther = new ExportedCode("mockedIri","mockedCode","mockedLabelLg1", "mockedLabelLg2");
        assertEquals(exportedCodeOther,exportedCode);
    }


}