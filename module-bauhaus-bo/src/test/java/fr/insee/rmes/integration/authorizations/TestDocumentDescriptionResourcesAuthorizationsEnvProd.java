package fr.insee.rmes.integration.authorizations;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentDescriptionRepository;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.webservice.DocumentDescriptionResources;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@WebMvcTest(
        controllers = DocumentDescriptionResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
            "fr.insee.rmes.bauhaus.modules.operations.enabled=true",
            "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        })
@Import({DocumentDescriptionResources.class, UserAuthTestConfiguration.class})
class TestDocumentDescriptionResourcesAuthorizationsEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
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
    }

    private static Stream<Arguments> provideDocumentDescriptionGet() {
        return Stream.of(
                Arguments.of("/operations/documents/1", 200, true),
                Arguments.of("/operations/documents/1", 403, false),
                Arguments.of("/operations/liens/1", 200, true),
                Arguments.of("/operations/liens/1", 403, false));
    }

    @MethodSource("provideDocumentDescriptionGet")
    @ParameterizedTest
    void getDocumentDescription(String url, Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}
