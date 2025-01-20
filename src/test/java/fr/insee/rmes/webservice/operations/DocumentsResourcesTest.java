package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentsResourcesTest {

    static final Logger logger = LoggerFactory.getLogger(DocumentsResourcesTest.class);

    @Test
    @DisplayName("Verifier que les extensions dans Bauhaus-core.properties puissent etre extraites. ")
    void test1ExtensionsAutorisees() throws IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        List<String> extensionsTheoriques = Arrays.asList("ods","pdf","xls");
        //When
        List<String> extensions = DocumentsResources.extensionsAutorisees();
        //Then
        assertEquals(extensionsTheoriques,extensions);
    }

    @Test
    @DisplayName("Verifier qu'une Rmes exception est renvoyee quand l'extension est incorrecte. ")
    public void gestionExtensions1() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier="image/png";
        //Then
        assertThrows(RmesException.class,()->{
            DocumentsResources.gestionExtensions(nomFichier);});
    }

    @Test
    @DisplayName("Verifier qu'un fichier nom.extension_autorisée ne renvoie pas d'exception.")
    public void gestionExtensions2() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "image/pdf";
        //Then
        DocumentsResources.gestionExtensions(nomFichier);
    }

    @Test
    @DisplayName("Verifier que le nom d'un fichier avec syntaxiquement plusieurs points - et dont l'extension est autorisée - ne renvoie pas d'exception.")
    public void gestionExtensions3() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "Livre/Roman/pdf";
        //Then
        DocumentsResources.gestionExtensions(nomFichier);
    }

    @Test
    @DisplayName("Verifier que le nom d'un fichier avec syntaxiquement plusieurs points - et dont l'extension est interdite - renvoie une Rmes exception.")
    public void gestionExtensions4() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "Livre.Roman.jpg";
        //Then
        assertThrows(RmesException.class,()->{
            DocumentsResources.gestionExtensions(nomFichier);});
    }

    @Test
    @DisplayName("Verifier qu'un nom de fichier sans extension renvoie une Rmes exception.")
    public void gestionExtensions5() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "Livre";
        //Then
        assertThrows(RmesException.class,()->{
            DocumentsResources.gestionExtensions(nomFichier);});
    }

    @Test
    @DisplayName("Verifier que le nom d'un fichier complexe - et dont l'extension est autorisée - ne renvoie pas d'exception.")
    public void gestionExtensions6() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "C:\\\\Users\\\\THJIOC\\\\Documents\\\\listeBLANCHE\\\\travaux/pdf";
        //Then
        DocumentsResources.gestionExtensions(nomFichier);
    }

    @Test
    @DisplayName("Verifier que le nom d'un fichier composé d'extension et se terminant par une extension autorisée ne renvoie pas d'exception.")
    public void gestionExtensions7() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "jpg/xlsx/svg/pdf";
        //Then
        DocumentsResources.gestionExtensions(nomFichier);
    }

    @Test
    @DisplayName("Verifier que le nom complet d'un fichier avec une extension sans prefixe ne renvoie pas d'exception.")
    public void gestionExtensions8() throws RmesException, IOException {
        //Given -- Bauhaus-core.properties (key extensions)
        //When
        String nomFichier = "/pdf";
        //Then
        DocumentsResources.gestionExtensions(nomFichier);
    }

}