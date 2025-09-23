package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
import fr.insee.rmes.onion.domain.port.serverside.StampsService;
import fr.insee.rmes.onion.infrastructure.webservice.PublicResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = PublicResources.class,
        properties = {
                "fr.insee.rmes.bauhaus.env=dev",
                "fr.insee.rmes.bauhaus.lg1=fr",
                "fr.insee.rmes.bauhaus.lg2=en",
                "fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote=35",
                "fr.insee.rmes.bauhaus.concepts.defaultMailSender=email",
                "fr.insee.rmes.bauhaus.concepts.defaultContributor=stamp",
                "fr.insee.rmes.bauhaus.appHost=http://localhost",
                "fr.insee.rmes.bauhaus.activeModules=operations,concepts",
                "fr.insee.rmes.bauhaus.modules=operations,concepts"
        }
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        FakeUserConfiguration.class})
class PublicResourcesTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    StampsService stampsService;

    @Test
    void shouldReturnResponseEntityWhenGetProperties() throws Exception {
        PublicResources publicResources = new PublicResources(stampsService,
                "env",
                "lg1",
                "lg2",
                "maxLengthScopeNote",
                "defaultMailSender",
                "defaultContributor",
                "appHost",
                List.of("activeModule"),
                List.of("module"),
                "version",
                List.of("extraMandatoryFields"));
        String actual = publicResources.getProperties().toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK"));
    }


    @Test
    void shouldReturnTheInitPayload() throws Exception {
        mvc.perform(get("/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "defaultMailSender":"email",
                            "lg2":"en",
                            "lg1":"fr",
                            "maxLengthScopeNote":"35",
                            "authType":"NoAuthImpl",
                            "defaultContributor":"stamp",
                            "activeModules":["operations","concepts"],
                            "appHost":"http://localhost",
                            "modules":["operations","concepts"]}
                        """));

    }
}