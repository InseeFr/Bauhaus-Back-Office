package fr.insee.rmes.modules.operations.documents.webservice;

import static fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentFileStorage.URL_PREFIX;
import static fr.insee.rmes.modules.operations.documents.domain.InMemoryManagedDocumentRepository.form;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.modules.AbstractHasAccessResourcesTest;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentManagementService;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentFileStorage;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryManagedDocumentRepository;
import fr.insee.rmes.modules.operations.documents.domain.InMemorySimsOwnersLookup;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentManagementService;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Les droits RBAC de chaque endpoint, devant le vrai service de domaine : un appel autorisé réussit
 * pour de bon sur le document et le lien 1, un appel refusé répond 403.
 */
@WebMvcTest(
        controllers = DocumentsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {"fr.insee.rmes.bauhaus.modules.operations.enabled=true"})
@Import({DocumentsResources.class, UserAuthTestConfiguration.class})
class DocumentsResourcesHasAccessIntegrationTest extends AbstractHasAccessResourcesTest {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        @Bean
        InMemoryManagedDocumentRepository managedDocumentRepository() {
            return new InMemoryManagedDocumentRepository();
        }

        @Bean
        InMemoryDocumentFileStorage documentFileStorage() {
            return new InMemoryDocumentFileStorage();
        }

        @Bean
        DocumentManagementService documentManagementService(
                InMemoryManagedDocumentRepository repository, InMemoryDocumentFileStorage storage) {
            return new DomainDocumentManagementService(
                    repository, storage, new InMemorySimsOwnersLookup(), Set.of("pdf", "odt"));
        }
    }

    @Autowired
    InMemoryManagedDocumentRepository repository;

    @Autowired
    InMemoryDocumentFileStorage storage;

    @BeforeEach
    void seed() {
        repository.clear();
        storage.clear();
        storage.put("Note.pdf", "contenu");
        repository.add(new ManagedDocument(
                "1",
                DocumentKind.DOCUMENT,
                "http://bauhaus/documents/document/1",
                form("Note", null, URL_PREFIX + "Note.pdf"),
                null));
        repository.add(new ManagedDocument(
                "1",
                DocumentKind.LINK,
                "http://bauhaus/documents/page/1",
                form("Page", null, "https://www.insee.fr/page"),
                null));
    }

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/documents", 200, true),
                Arguments.of("/documents/document/1", 200, true),
                Arguments.of("/documents/document/1/file", 200, true),
                Arguments.of("/documents/link/1", 200, true),
                Arguments.of("/documents", 403, false),
                Arguments.of("/documents/document/1", 403, false),
                Arguments.of("/documents/document/1/file", 200, false),
                Arguments.of("/documents/link/1", 403, false));
    }

    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        assertStatusWithAccess(request, code, hasAccessReturn);
    }

    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of("/documents/document/1", 200, true),
                Arguments.of("/documents/document/1", 403, false),
                Arguments.of("/documents/link/1", 200, true),
                Arguments.of("/documents/link/1", 403, false));
    }

    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void updateDocumentOrLink(String url, Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        var request = put(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\", \"labelLg1\": \"Libellé\", \"url\": \"https://www.insee.fr/page\"}");

        assertStatusWithAccess(request, code, hasAccessReturn);
    }

    private static Stream<Arguments> provideDataForDeleteEndpoints() {
        return Stream.of(
                Arguments.of("/documents/document/1", 200, true),
                Arguments.of("/documents/document/1", 403, false),
                Arguments.of("/documents/link/1", 200, true),
                Arguments.of("/documents/link/1", 403, false));
    }

    @MethodSource("provideDataForDeleteEndpoints")
    @ParameterizedTest
    void deleteDocumentOrLink(String url, Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        var request = delete(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        assertStatusWithAccess(request, code, hasAccessReturn);
    }

    private static Stream<Arguments> provideDataForDocumentPostEndpoints() {
        return Stream.of(Arguments.of(201, true), Arguments.of(403, false));
    }

    @MethodSource("provideDataForDocumentPostEndpoints")
    @ParameterizedTest
    void postDocument(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "Nouveau.pdf", MediaType.APPLICATION_PDF_VALUE, "Contenu du fichier".getBytes());
        var request = multipart("/documents/document")
                .file(file)
                .param("body", "{\"labelLg1\": \"Nouveau\"}")
                .contentType(MULTIPART_FORM_DATA_VALUE);

        assertStatusWithAccess(request, code, hasAccessReturn);
    }

    private static Stream<Arguments> provideDataForLinkPostEndpoints() {
        return Stream.of(Arguments.of(200, true), Arguments.of(403, false));
    }

    @MethodSource("provideDataForLinkPostEndpoints")
    @ParameterizedTest
    void postLink(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        var request = post("/documents/link")
                .param("body", "{\"labelLg1\": \"Nouvelle page\", \"url\": \"https://www.insee.fr/nouvelle\"}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        assertStatusWithAccess(request, code, hasAccessReturn);
    }

    private static Stream<Arguments> provideDataForUpdateFileEndpoints() {
        return Stream.of(Arguments.of(200, true), Arguments.of(403, false));
    }

    @MethodSource("provideDataForUpdateFileEndpoints")
    @ParameterizedTest
    void updateFile(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        MockMultipartFile file =
                new MockMultipartFile("file", "Note.pdf", MediaType.APPLICATION_PDF_VALUE, "nouveau".getBytes());
        var request = multipart(HttpMethod.PUT, "/documents/document/1/file")
                .file(file)
                .contentType(MULTIPART_FORM_DATA_VALUE);

        assertStatusWithAccess(request, code, hasAccessReturn);
    }
}
