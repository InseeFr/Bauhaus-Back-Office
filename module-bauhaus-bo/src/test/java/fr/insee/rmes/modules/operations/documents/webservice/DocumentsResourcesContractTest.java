package fr.insee.rmes.modules.operations.documents.webservice;

import static fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentFileStorage.URL_PREFIX;
import static fr.insee.rmes.modules.operations.documents.domain.InMemoryManagedDocumentRepository.form;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentManagementService;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentFileStorage;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryManagedDocumentRepository;
import fr.insee.rmes.modules.operations.documents.domain.InMemorySimsOwnersLookup;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentManagementService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Le contrat HTTP que l'IHM utilise, servi par le vrai service de domaine sur des adaptateurs en
 * mémoire, et le vrai gestionnaire d'erreurs (RmesExceptionHandler) : chemins, statuts, champs JSON
 * et codes d'erreur restent ceux de l'implémentation historique.
 */
@WebMvcTest(
        controllers = DocumentsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.operations.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
@Import(DocumentsResourcesContractTest.InMemoryDocuments.class)
class DocumentsResourcesContractTest {

    @TestConfiguration
    static class InMemoryDocuments {
        @Bean
        InMemoryManagedDocumentRepository managedDocumentRepository() {
            return new InMemoryManagedDocumentRepository();
        }

        @Bean
        InMemoryDocumentFileStorage documentFileStorage() {
            return new InMemoryDocumentFileStorage();
        }

        @Bean
        InMemorySimsOwnersLookup simsOwnersLookup() {
            return new InMemorySimsOwnersLookup();
        }

        @Bean
        DocumentManagementService documentManagementService(
                InMemoryManagedDocumentRepository repository,
                InMemoryDocumentFileStorage storage,
                InMemorySimsOwnersLookup owners) {
            return new DomainDocumentManagementService(repository, storage, owners, Set.of("pdf", "odt"));
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    InMemoryManagedDocumentRepository repository;

    @Autowired
    InMemoryDocumentFileStorage storage;

    @Autowired
    InMemorySimsOwnersLookup owners;

    private static final String DOCUMENT_12 = "http://bauhaus/documents/document/12";

    @BeforeEach
    void seed() {
        repository.clear();
        storage.clear();
        storage.put("Note.pdf", "contenu");
        repository.add(new ManagedDocument(
                "12",
                DocumentKind.DOCUMENT,
                DOCUMENT_12,
                form("Note", "Note EN", URL_PREFIX + "Note.pdf"),
                new FileSize(130_048)));
        repository.add(new ManagedDocument(
                "13",
                DocumentKind.LINK,
                "http://bauhaus/documents/page/13",
                form("Page", null, "https://www.insee.fr/page"),
                null));
    }

    private static MockMultipartFile file(String name, String content) {
        return new MockMultipartFile("file", name, MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
    }

    @Test
    void should_list_documents_and_links() throws Exception {
        mvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("12"))
                .andExpect(jsonPath("$[0].uri").value(DOCUMENT_12))
                .andExpect(jsonPath("$[0].size").value(130048))
                .andExpect(jsonPath("$[1].url").value("https://www.insee.fr/page"));
    }

    @Test
    void should_describe_a_document_with_the_quality_reports_citing_it() throws Exception {
        repository.referencedBy(DOCUMENT_12, new SimsReference("1", "Rapport", "Report", "S.3.1", List.of()));
        owners.owners("1", List.of("DG75-L201"));

        mvc.perform(get("/documents/document/12"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": "12",
                          "uri": "http://bauhaus/documents/document/12",
                          "url": "file:///gestion/Note.pdf",
                          "labelLg1": "Note",
                          "labelLg2": "Note EN",
                          "descriptionLg1": "description",
                          "updatedDate": "2026-09-24",
                          "lang": "fr",
                          "size": 130048,
                          "sims": [
                            {"id": "1", "labelLg1": "Rapport", "labelLg2": "Report", "simsRubricId": "S.3.1",
                             "creators": ["DG75-L201"]}
                          ]
                        }
                        """, true));
    }

    @Test
    void should_describe_a_link() throws Exception {
        mvc.perform(get("/documents/link/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://www.insee.fr/page"))
                .andExpect(jsonPath("$.size").doesNotExist())
                .andExpect(jsonPath("$.sims", hasSize(0)));
    }

    @Test
    void should_answer_404_with_the_error_code_for_an_unknown_document() throws Exception {
        mvc.perform(get("/documents/document/404"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("341")));
    }

    @Test
    void should_create_a_document_from_a_multipart_upload_and_record_its_size() throws Exception {
        mvc.perform(multipart("/documents/document")
                        .param("body", "{\"labelLg1\": \"Nouveau\", \"lang\": \"fr\", \"id\": \"ignoré\"}")
                        .file(file("Nouveau.pdf", "12345")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/documents/document/1000")))
                .andExpect(content().string("1000"));

        assertThat(repository.stored(DocumentKind.DOCUMENT, "1000"))
                .hasValueSatisfying(document -> assertThat(document.size()).isEqualTo(new FileSize(5)));
    }

    @Test
    void should_refuse_a_label_already_used_with_its_error_code() throws Exception {
        mvc.perform(multipart("/documents/document")
                        .param("body", "{\"labelLg1\": \"Note\"}")
                        .file(file("Autre.pdf", "x")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1")));
    }

    @Test
    void should_refuse_a_file_name_already_used_with_its_error_code() throws Exception {
        mvc.perform(multipart("/documents/document")
                        .param("body", "{\"labelLg1\": \"Autre\"}")
                        .file(file("Note.pdf", "x")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("302")));
    }

    @Test
    void should_create_a_link_from_a_form_field() throws Exception {
        mvc.perform(multipart("/documents/link")
                        .param("body", "{\"labelLg1\": \"Nouvelle page\", \"url\": \"https://www.insee.fr/nouvelle\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    void should_refuse_a_body_that_is_not_json() throws Exception {
        mvc.perform(multipart("/documents/link").param("body", "pas du json")).andExpect(status().isBadRequest());
    }

    @Test
    void should_refuse_a_link_without_url_with_its_error_code() throws Exception {
        mvc.perform(multipart("/documents/link").param("body", "{\"labelLg1\": \"Nouvelle page\"}"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string(containsString("461")));
    }

    @Test
    void should_update_a_document_and_keep_its_size() throws Exception {
        mvc.perform(put("/documents/document/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"12\", \"labelLg1\": \"Note révisée\", \"sims\": [], \"url\": \"x\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("12"));

        assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).hasValueSatisfying(document -> {
            assertThat(document.form().labelLg1()).isEqualTo("Note révisée");
            assertThat(document.size()).isEqualTo(new FileSize(130_048));
        });
    }

    @Test
    void should_update_a_link() throws Exception {
        mvc.perform(put("/documents/link/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelLg1\": \"Page\", \"url\": \"https://www.insee.fr/autre\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("13"));
    }

    @Test
    void should_give_the_new_url_when_the_replacing_file_has_another_name() throws Exception {
        mvc.perform(multipart(HttpMethod.PUT, "/documents/document/12/file").file(file("Note_v2.pdf", "v2")))
                .andExpect(status().isOk())
                .andExpect(content().string(URL_PREFIX + "Note_v2.pdf"));
    }

    @Test
    void should_give_an_empty_body_when_the_replacing_file_keeps_its_name() throws Exception {
        mvc.perform(multipart(HttpMethod.PUT, "/documents/document/12/file").file(file("Note.pdf", "v2")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void should_refuse_a_replacing_file_whose_extension_is_not_allowed() throws Exception {
        mvc.perform(multipart(HttpMethod.PUT, "/documents/document/12/file").file(file("charge.exe", "x")))
                .andExpect(content().string(containsString("Invalid File Extension")));
    }

    @Test
    void should_delete_a_document() throws Exception {
        mvc.perform(delete("/documents/document/12"))
                .andExpect(status().isOk())
                .andExpect(content().string("12"));

        assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).isEmpty();
    }

    @Test
    void should_refuse_to_delete_a_document_cited_by_a_quality_report_with_its_error_code() throws Exception {
        repository.citedBy(DOCUMENT_12, "http://bauhaus/qualite/attribut/1/S.3.1/texte");

        mvc.perform(delete("/documents/document/12"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("304")));
    }

    @Test
    void should_delete_a_link() throws Exception {
        mvc.perform(delete("/documents/link/13"))
                .andExpect(status().isOk())
                .andExpect(content().string("13"));
    }

    @Test
    void should_download_the_file_of_a_document_as_an_attachment() throws Exception {
        mvc.perform(get("/documents/document/12/file"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"Note.pdf\"")))
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(content().string("contenu"));
    }

    @Test
    void should_answer_404_when_the_file_is_missing_from_the_storage() throws Exception {
        storage.delete("Note.pdf");

        mvc.perform(get("/documents/document/12/file")).andExpect(status().isNotFound());
    }
}
