package fr.insee.rmes.model.operations.documentations;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void shouldReturnTrueWhenHasRichTextLg1WithOnlyDocuments() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setLabelLg1("");
        List<Document> docs = new ArrayList<>();
        docs.add(new Document());
        rubric.setDocumentsLg1(docs);

        assertTrue(rubric.hasRichTextLg1());
    }

    @Test
    void shouldReturnFalseWhenHasRichTextLg1WithEmptyLabelAndNoDocs() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setLabelLg1("");
        rubric.setDocumentsLg1(null);

        assertFalse(rubric.hasRichTextLg1());
    }

    @Test
    void shouldReturnTrueWhenHasRichTextLg2WithOnlyDocuments() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setLabelLg2("");
        List<Document> docs = new ArrayList<>();
        docs.add(new Document());
        rubric.setDocumentsLg2(docs);

        assertTrue(rubric.hasRichTextLg2());
    }

    @Test
    void shouldReturnFalseWhenHasRichTextLg2WithEmptyLabelAndNoDocs() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setLabelLg2("");
        rubric.setDocumentsLg2(null);

        assertFalse(rubric.hasRichTextLg2());
    }

    @Test
    void shouldReturnNullWhenGetSimpleValueWithEmptyList() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setValue(new ArrayList<>());

        assertNull(rubric.getSimpleValue());
    }

    @Test
    void shouldReturnNullWhenGetSimpleValueWithNullList() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setValue(null);

        assertNull(rubric.getSimpleValue());
    }

    @Test
    void shouldSetSingleValue() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setSingleValue("testValue");

        assertEquals("testValue", rubric.getSimpleValue());
        assertEquals(1, rubric.getValue().size());
    }

    @Test
    void shouldReturnUppercaseIdAttribute() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setIdAttribute("lowercase_id");

        assertEquals("LOWERCASE_ID", rubric.getIdAttribute());
    }

    @Test
    void shouldReturnTrueWhenEmptyWithEmptyDocuments() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setValue(new ArrayList<>());
        rubric.setLabelLg1("");
        rubric.setLabelLg2("");
        rubric.setCodeList("");
        rubric.setDocumentsLg1(new ArrayList<>());
        rubric.setDocumentsLg2(new ArrayList<>());
        rubric.setUri("");

        assertTrue(rubric.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenNotEmptyWithUri() {
        DocumentationRubric rubric = new DocumentationRubric();
        rubric.setValue(null);
        rubric.setLabelLg1("");
        rubric.setLabelLg2("");
        rubric.setCodeList("");
        rubric.setDocumentsLg1(null);
        rubric.setDocumentsLg2(null);
        rubric.setUri("http://example.com");

        assertFalse(rubric.isEmpty());
    }

}