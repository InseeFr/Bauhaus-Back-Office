package fr.insee.rmes.integration;

import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsImpl;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.stamps.StampsRestrictionServiceImpl;
import fr.insee.rmes.config.BaseConfigForMvcTests;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.webservice.operations.DocumentsResources;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentsResources.class)
@Import({BaseConfigForMvcTests.class, DocumentsImpl.class})
class TestDocumentsResourcesWithMinio {

    @Autowired
    private MockMvc mockMvc;

//    @MockBean
//    private ParentUtils parentUtils;
//    @MockBean
//    private RepositoryPublication repositoryPublication;
//    @MockBean
//    private StampsRestrictionServiceImpl stampsRestrictionService;
//    @MockBean
//    private IdGenerator idGenerator;
//    @MockBean
//    private PublicationUtils publicationUtils;
//    @MockBean
//    private Config config;
//    @MockBean
//    private RepositoryGestion repositoryGestion;

    @MockBean
    FilesOperations filesOperations;

    private final String fichierId="ID";

    private static final String nomFichier = "nomFichier";

    @Test
    void shouldLogAndReturnInternalException_WhenErrorOccursInMinio() throws Exception {

        String objectName = "directoryGestion/"+nomFichier;
        String bucketName = "metadata_bucket";
        when(filesOperations.read(anyString())).thenThrow(new RmesFileException(nomFichier, "Error reading file: " + nomFichier+
                " as object `"+objectName+"` in bucket "+bucketName, new MinioException()));


        mockMvc.perform(MockMvcRequestBuilders.get("/documents/document/" + fichierId + "/file"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("fileName='" + nomFichier + "'")));
    }

    @TestConfiguration
    static class ConfigurationForTest{

        @Bean
        public DocumentsUtils documentsUtils(FilesOperations filesOperations) {
            return new DocumentsUtils(null, filesOperations){
                @Override
                protected String getDocumentFilename(String id){
                    return nomFichier;
                }
            };
        }

    }

}