package fr.insee.rmes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BauhausUriProperties.class)
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://test.gestion/",
        "fr.insee.rmes.bauhaus.links.baseURI=http://test.links/",
        "fr.insee.rmes.bauhaus.codeList.baseURI=http://test.codelist/",
        "fr.insee.rmes.bauhaus.documents.baseURI=http://test.documents/",
        "fr.insee.rmes.bauhaus.products.baseURI=http://test.products/"
})
class BauhausUriPropertiesTest {

    @Autowired
    private BauhausUriProperties uris;

    @Test
    void shouldBindEachUri() {
        assertEquals("http://test.gestion/", uris.baseUriGestion());
        assertEquals("http://test.links/", uris.linksBaseUri());
        assertEquals("http://test.codelist/", uris.codeListBaseUri());
        assertEquals("http://test.documents/", uris.documentsBaseUri());
        assertEquals("http://test.products/", uris.productsBaseUri());
    }
}
