package fr.insee.rmes.model.operations.documentations;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentationRubricTest {

    @Test
    void shouldReturnBooleanWhenDocumentationRubricIsOrIsNotEmpty() {

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
}