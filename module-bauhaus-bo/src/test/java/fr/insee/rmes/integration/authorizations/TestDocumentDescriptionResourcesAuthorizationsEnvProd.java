package fr.insee.rmes.integration.authorizations;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentDescriptionRepository;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.webservice.DocumentDescriptionResources;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.users.infrastructure.LazyPublicEndpointsMatcher;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Les descriptions de documents et de liens sont publiques : servies sans authentification,
 * derrière la même chaîne de sécurité qu'en production ({@link LazyPublicEndpointsMatcher}).
 */
@WebMvcTest(
        controllers = DocumentDescriptionResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = "fr.insee.rmes.bauhaus.modules.operations.enabled=true")
@Import(TestDocumentDescriptionResourcesAuthorizationsEnvProd.SecurityConfiguration.class)
class TestDocumentDescriptionResourcesAuthorizationsEnvProd {

    @Autowired
    MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/operations/public/documents/1", "/operations/public/liens/1"})
    void should_describe_a_document_or_a_link_to_anonymous_users(String url) throws Exception {
        mvc.perform(get(url).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @TestConfiguration
    @ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
    static class SecurityConfiguration {

        @Bean
        DocumentDescriptionService documentDescriptionService() {
            InMemoryDocumentDescriptionRepository repository = new InMemoryDocumentDescriptionRepository();
            for (DocumentKind kind : DocumentKind.values()) {
                repository.add(
                        kind,
                        "1",
                        new DocumentMetadata(
                                "http://bauhaus/documents/" + kind + "/1",
                                List.of(LocalisedLabel.ofDefaultLanguage("Document")),
                                List.of(),
                                null,
                                null,
                                null,
                                "https://www.insee.fr/document.pdf"),
                        Set.of());
            }
            return new DomainDocumentDescriptionService(repository);
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
