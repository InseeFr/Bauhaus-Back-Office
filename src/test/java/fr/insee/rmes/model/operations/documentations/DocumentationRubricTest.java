package fr.insee.rmes.model.operations.documentations;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentationRubricTest {
  
    @Mock
    List<Document> document1;
    List<Document> document2;

    @Test
    void shouldReturnBooleanWhenDocumentationRubricIsEmpty() {

        DocumentationRubric documentationRubricNotEmpty = new DocumentationRubric();
        documentationRubricNotEmpty.setValue(null);
        documentationRubricNotEmpty.setLabelLg1("labelLg1");
        documentationRubricNotEmpty.setLabelLg2("labelLg2");
        documentationRubricNotEmpty.setCodeList("codeList");
        documentationRubricNotEmpty.setDocumentsLg1(null);
        documentationRubricNotEmpty.setDocumentsLg2(null);
        documentationRubricNotEmpty.setUri(null);

        documentationRubricNotEmpty.isEmpty();

        DocumentationRubric documentationRubricEmpty = new DocumentationRubric();
        documentationRubricEmpty.setValue(null);
        documentationRubricEmpty.setLabelLg1("");
        documentationRubricEmpty.setLabelLg2("");
        documentationRubricEmpty.setCodeList("");
        documentationRubricEmpty.setDocumentsLg1(null);
        documentationRubricEmpty.setDocumentsLg2(null);
        documentationRubricEmpty.setUri(null);

        List<Boolean> actual = List.of(documentationRubricNotEmpty.isEmpty(),documentationRubricEmpty.isEmpty());

        assertEquals(List.of(false,true),actual);

    }


   @Test
   void shouldReturnBooleanWhenDocumentationRubricIsNotEmpty() {

        DocumentationRubric documentationRubric = new DocumentationRubric();

        documentationRubric.setIdAttribute("idAttribute");
        documentationRubric.setValue(List.of("value1","value2"));
        documentationRubric.setCodeList("codeList");
        documentationRubric.setLabelLg1("labelLg1");
        documentationRubric.setLabelLg2("labelLg2");
        documentationRubric.setRangeType("rangeType");
        documentationRubric.setUri("uri");
        documentationRubric.setDocumentsLg1(document1);
        documentationRubric.setDocumentsLg2(document2);

        boolean isEmpty = documentationRubric.isEmpty();
        boolean hasRichTextLg1 =documentationRubric.hasRichTextLg1();
        boolean hasRichTextLg2 =documentationRubric.hasRichTextLg2();
        boolean getValue = Objects.equals(documentationRubric.getSimpleValue(), "value1");

        List<Boolean> expected = List.of(false,true,true,true);
        List<Boolean> actual = List.of(isEmpty,hasRichTextLg1,hasRichTextLg2,getValue );

        assertEquals(expected,actual);

    }

}