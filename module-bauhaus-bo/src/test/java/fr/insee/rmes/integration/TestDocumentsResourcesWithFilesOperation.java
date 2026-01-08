package fr.insee.rmes.integration;

import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsImpl;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.BaseConfigForMvcTests;
import fr.insee.rmes.Config;
import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.operations.documents.webservice.DocumentsResources;
import fr.insee.rmes.utils.IdGenerator;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.nio.file.Path;

import static fr.insee.rmes.utils.ConsoleCapture.startCapturingConsole;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentsResources.class)
@Import({BaseConfigForMvcTests.class, DocumentsImpl.class, TestDocumentsResourcesWithFilesOperation.ConfigurationForTest.class})
class TestDocumentsResourcesWithFilesOperation {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepositoryPublication repositoryPublication;

    @MockitoBean
    private IdGenerator idGenerator;
    @MockitoBean
    private PublicationUtils publicationUtils;
    @MockitoBean
    private Config config;
    @MockitoBean
    private RepositoryGestion repositoryGestion;

    static final String nomFichier = "nomFichier";
    static final String objectName = "directoryGestion/"+nomFichier;
    static final String bucketName = "metadata_bucket";

    @Test
    void shouldLogAndReturnInternalException_WhenErrorOccursInMinio() throws Exception {
        var capture=startCapturingConsole();
        String fichierId = "ID";
        mockMvc.perform(MockMvcRequestBuilders.get("/documents/document/" + fichierId + "/file"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("fileName='" + nomFichier + "'")));
        assertThat(capture.standardOut()).asString().contains("Error reading file: "+nomFichier+" as object `"+objectName+"` in bucket "+bucketName);
        assertThat(capture.standardOut()).asString().contains("at fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils.downloadDocumentFile");
        assertThat(capture.standardOut()).asString().contains("at fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsImpl.downloadDocument");
        capture.stop();
    }

    @TestConfiguration
    static class ConfigurationForTest{

        @Bean
        public DocumentsUtils documentsUtils() {
            return new DocumentsUtils(null,  new FilesOperationStub()){
                @Override
                protected String getDocumentFilename(String id){
                    return nomFichier;
                }
            };
        }

    }

    static class FilesOperationStub implements FilesOperations{

        @Override
        public void delete(Path absolutePath) {

        }

        @Override
        public InputStream readInDirectoryGestion(String path) {
            throw new RmesFileException(nomFichier, "Error reading file: " + nomFichier+
                    " as object `"+objectName+"` in bucket "+bucketName, new MinioException());
        }

        @Override
        public void writeToDirectoryGestion(InputStream content, Path destPath) {

        }

        @Override
        public void copyFromGestionToPublication(String srcPath, String destPath) {

        }

        @Override
        public boolean dirExists(Path gestionStorageFolder) {
            return false;
        }

        @Override
        public boolean existsInStorageGestion(String filename) {
            return false;
        }
    }

}