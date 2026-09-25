package fr.insee.rmes.modules.operations.documents.webservice;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentDescriptionRepository;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.time.LocalDate;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = DocumentDescriptionResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.operations.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
@Import(DocumentDescriptionResourcesTest.InMemoryDocumentDescription.class)
class DocumentDescriptionResourcesTest {

    private static final String DOC_METHOD = "http://ec.europa.eu/eurostat/simsv2/concept/DOC_METHOD";

    @TestConfiguration
    static class InMemoryDocumentDescription {
        @Bean
        InMemoryDocumentDescriptionRepository documentDescriptionRepository() {
            return new InMemoryDocumentDescriptionRepository();
        }

        @Bean
        DocumentDescriptionService documentDescriptionService(InMemoryDocumentDescriptionRepository repository) {
            return new DomainDocumentDescriptionService(repository);
        }
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InMemoryDocumentDescriptionRepository repository;

    @BeforeEach
    void resetRepository() {
        repository.clear();
    }

    @Test
    void should_describe_a_document_in_the_magma_format() throws Exception {
        repository.add(
                DocumentKind.DOCUMENT,
                "1070",
                new DocumentMetadata(
                        "http://id.insee.fr/documents/document/1070",
                        List.of(
                                LocalisedLabel.ofDefaultLanguage("Les projections de population 2026"),
                                LocalisedLabel.ofAlternativeLanguage("Population projections for 2026")),
                        List.of(
                                LocalisedLabel.ofDefaultLanguage("Commentaire sur les projections"),
                                LocalisedLabel.ofAlternativeLanguage("Comment on population projections")),
                        LocalDate.of(2026, 4, 9),
                        "fr",
                        new FileSize(127_000),
                        "https://www.insee.fr/fr/metadonnees/source/fichier/Note_technique.pdf"),
                Set.of(DOC_METHOD));

        mockMvc.perform(get("/operations/documents/1070").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "uri": "http://id.insee.fr/documents/document/1070",
                          "label": [
                            {"contenu": "Les projections de population 2026", "langue": "fr"},
                            {"contenu": "Population projections for 2026", "langue": "en"}
                          ],
                          "commentaire": [
                            {"contenu": "Commentaire sur les projections", "langue": "fr"},
                            {"contenu": "Comment on population projections", "langue": "en"}
                          ],
                          "dateMiseAJour": "2026-04-09",
                          "langue": "fr",
                          "taille": "127 ko",
                          "type": "DOC_METHOD",
                          "url": "https://www.insee.fr/fr/metadonnees/source/fichier/Note_technique.pdf"
                        }
                        """, true));
    }

    @Test
    void should_omit_the_fields_that_are_not_stored_and_give_a_zero_size() throws Exception {
        repository.add(
                DocumentKind.DOCUMENT,
                "2",
                new DocumentMetadata(
                        "http://id.insee.fr/documents/document/2",
                        List.of(LocalisedLabel.ofDefaultLanguage("Document minimal")),
                        List.of(),
                        null,
                        null,
                        null,
                        "https://www.insee.fr/minimal.pdf"),
                Set.of());

        mockMvc.perform(get("/operations/documents/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "uri": "http://id.insee.fr/documents/document/2",
                          "label": [{"contenu": "Document minimal", "langue": "fr"}],
                          "taille": "0 o",
                          "type": "OTHER",
                          "url": "https://www.insee.fr/minimal.pdf"
                        }
                        """, true));
    }

    @Test
    void should_describe_a_link() throws Exception {
        repository.add(
                DocumentKind.LINK,
                "12",
                new DocumentMetadata(
                        "http://id.insee.fr/documents/page/12",
                        List.of(LocalisedLabel.ofDefaultLanguage("Page")),
                        List.of(),
                        null,
                        null,
                        null,
                        "https://www.insee.fr/page"),
                Set.of());

        mockMvc.perform(get("/operations/liens/12").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uri").value("http://id.insee.fr/documents/page/12"))
                .andExpect(jsonPath("$.label", hasSize(1)));
    }

    @Test
    void should_answer_404_for_an_unknown_document() throws Exception {
        mockMvc.perform(get("/operations/documents/404").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Document 404 doesn't exist"));
    }
}
