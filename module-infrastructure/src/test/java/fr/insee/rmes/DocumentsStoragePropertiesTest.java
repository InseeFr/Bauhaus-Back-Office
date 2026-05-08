package fr.insee.rmes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = DocumentsStorageProperties.class)
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.storage.document.gestion=/storage/gestion",
        "fr.insee.web4g.baseURL=  http://web4g.test/  "
})
class DocumentsStoragePropertiesTest {

    @Autowired
    private DocumentsStorageProperties storage;

    @Test
    void shouldBindStorageGestion() {
        assertEquals("/storage/gestion", storage.storageGestion());
    }

    @Test
    void baseUrlShouldBeTrimmed() {
        assertEquals("http://web4g.test/", storage.baseUrl());
    }
}
