package fr.insee.rmes.modules.operations.documents.webservice;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import fr.insee.rmes.modules.operations.documents.domain.DomainPublishedDocumentFileService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.PublishedDocumentFileService;
import fr.insee.rmes.modules.users.infrastructure.LazyPublicEndpointsMatcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Le fichier diffusé est servi sans authentification, derrière la même chaîne de sécurité qu'en
 * production ({@link LazyPublicEndpointsMatcher}), par le vrai service de domaine lisant un vrai
 * répertoire de publication.
 */
@WebMvcTest(
        controllers = PublishedDocumentFilesResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = "fr.insee.rmes.bauhaus.modules.operations.enabled=true")
@Import(PublishedDocumentFilesResourcesTest.TestConfiguration.class)
class PublishedDocumentFilesResourcesTest {

    @Autowired
    MockMvc mvc;

    @Test
    void should_serve_a_published_file_to_anonymous_users_with_its_media_type() throws Exception {
        mvc.perform(get("/documents/fichier/Note_technique.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(
                        header().string("Content-Disposition", startsWith("inline; filename=\"Note_technique.pdf\"")))
                .andExpect(content().bytes("contenu publié".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void should_serve_html_in_a_sandbox_so_that_it_cannot_run_scripts_on_the_api_origin() throws Exception {
        mvc.perform(get("/documents/fichier/page.html"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/html"))
                .andExpect(header().string("Content-Security-Policy", "sandbox"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void should_answer_404_for_a_file_that_is_not_published() throws Exception {
        mvc.perform(get("/documents/fichier/Brouillon.pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File Brouillon.pdf is not published"));
    }

    @org.springframework.boot.test.context.TestConfiguration
    @ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
    static class TestConfiguration {

        @Bean
        PublishedDocumentFileService publishedDocumentFileService() throws IOException {
            Path publication = Files.createTempDirectory("publication");
            Files.writeString(publication.resolve("Note_technique.pdf"), "contenu publié");
            Files.writeString(publication.resolve("page.html"), "<script>alert(1)</script>");
            return new DomainPublishedDocumentFileService(new FileSystemOperation(), publication.toString());
        }

        @Bean
        RequestMatcher publicEndpointsMatcher(ObjectProvider<RequestMappingHandlerMapping> handlerMapping) {
            return new LazyPublicEndpointsMatcher(handlerMapping);
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http, RequestMatcher publicEndpointsMatcher) {
            http.csrf(AbstractHttpConfigurer::disable)
                    .httpBasic(withDefaults())
                    .authorizeHttpRequests(auth -> auth.requestMatchers(publicEndpointsMatcher)
                            .permitAll()
                            .anyRequest()
                            .authenticated());
            return http.build();
        }
    }
}
