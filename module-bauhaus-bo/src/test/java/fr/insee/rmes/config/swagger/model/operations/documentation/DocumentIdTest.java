package fr.insee.rmes.config.swagger.model.operations.documentation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentIdTest {

    @Test
    void shouldCreateDocumentIdWhenConstructorIsUsed(){
        DocumentId firstDocumentId = new DocumentId("mockedID");
        DocumentId secondDocumentId = new DocumentId(null);
        boolean isFirstDocumentNotNull= firstDocumentId.getDocumentId()!=null;
        boolean isSecondDocumentNull= secondDocumentId.getDocumentId()==null;
        assertTrue(isFirstDocumentNotNull && isSecondDocumentNull);
    }
}