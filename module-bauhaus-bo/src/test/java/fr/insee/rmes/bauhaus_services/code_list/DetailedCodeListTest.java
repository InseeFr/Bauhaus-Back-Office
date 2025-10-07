package fr.insee.rmes.bauhaus_services.code_list;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class DetailedCodeListTest {

    @Test
    void shouldVerifyClassFunctionality() {
        DetailedCodeList detailedCodeList = new DetailedCodeList();

        detailedCodeList.setNotation("mockedNotation");
        detailedCodeList.setCodeListLabelLg1("mockedCodeListLabelLg1");
        detailedCodeList.setCodeListLabelLg2("mockedCodeListLabelLg2");

        CodeListItem firstCodeListItem = new CodeListItem();
        firstCodeListItem.setCode("mockedFirstCode");
        CodeListItem secondCodeListItem = new CodeListItem();
        secondCodeListItem.setCode("mockedFirstCode");
        List<CodeListItem> codes = List.of(firstCodeListItem,secondCodeListItem);
        detailedCodeList.setCodes(codes);

        boolean checkCodeListLabelLg1 = Objects.equals(detailedCodeList.getCodeListLabelLg1(), "mockedCodeListLabelLg1");
        boolean checkCodeListLabelLg2 = Objects.equals(detailedCodeList.getCodeListLabelLg2(), "mockedCodeListLabelLg2");
        boolean checkCodes = detailedCodeList.getCodes()==codes;

        assertTrue(checkCodeListLabelLg1 && checkCodeListLabelLg2 && checkCodes);

    }
}