package fr.insee.rmes.bauhaus_services.code_list;

import static fr.insee.rmes.bauhaus_services.code_list.CodeListItem.getClassOperationsLink;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CodeListItemTest {

    @Test
    void shouldVerifyElementsWhenCreateCodeListItemClass() {

        CodeListItem codeListItem = CodeListItem.of("mockedCode", "mockedLabelLg1", "mockedLabelLg2", "mockedIri");
        CodeListItem codeListItemWithoutParameters = new CodeListItem();

        boolean isNullLastCodeUriSegment = codeListItem.getLastCodeUriSegment() == null
                && codeListItemWithoutParameters.getLastCodeUriSegment() == null;
        boolean isNullLastCodeUri =
                codeListItem.getCodeUri() == null && codeListItemWithoutParameters.getCodeUri() == null;
        boolean isNotNullClassLink =
                "fr.insee.rmes.bauhaus_services.code_list.CodeListItem".equals(getClassOperationsLink());

        assertTrue(isNullLastCodeUriSegment && isNullLastCodeUri && isNotNullClassLink);
    }
}
