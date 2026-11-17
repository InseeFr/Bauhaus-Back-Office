package fr.insee.rmes.domain.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

class SecureXmlTest {

    @Test
    void parse_doesNotExpandExternalEntities(@TempDir Path tempDir) throws Exception {
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "TOP_SECRET");
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE Fragment [ <!ENTITY xxe SYSTEM "%s"> ]>
                <Fragment><CodeList>&xxe;</CodeList></Fragment>
                """.formatted(secret.toUri());

        Document document = SecureXml.parse(xxe);

        assertFalse(
                document.getDocumentElement().getTextContent().contains("TOP_SECRET"),
                "le contenu du fichier local ne doit pas atteindre le DOM");
    }

    @Test
    void parse_doesNotReadTheEntityTarget(@TempDir Path tempDir) throws Exception {
        // La cible n'existe pas : un parseur non durci lèverait une FileNotFoundException.
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE Fragment [ <!ENTITY xxe SYSTEM "%s"> ]>
                <Fragment><CodeList>&xxe;</CodeList></Fragment>
                """.formatted(tempDir.resolve("absent.txt").toUri());

        Document document = SecureXml.parse(xxe);

        assertEquals("Fragment", document.getDocumentElement().getLocalName());
    }

    @Test
    void parse_isNamespaceAware() throws Exception {
        Document document = SecureXml.parse("<ddi:Group xmlns:ddi=\"ddi:group:3_3\"/>");

        assertEquals("ddi:group:3_3", document.getDocumentElement().getNamespaceURI());
        assertEquals("Group", document.getDocumentElement().getLocalName());
    }
}
