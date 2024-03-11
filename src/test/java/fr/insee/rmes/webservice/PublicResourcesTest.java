package fr.insee.rmes.webservice;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.external_services.authentication.stamps.StampsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WebMvcTest(
        controllers = PublicResources.class,
        properties = {
                "fr.insee.rmes.bauhaus.env=dev",
                "fr.insee.rmes.bauhaus.lg1=fr",
                "fr.insee.rmes.bauhaus.lg2=en",
                "fr.insee.rmes.bauhaus.concepts.maxLengthScopeNote=35",
                "fr.insee.rmes.bauhaus.concepts.defaultMailSender=email",
                "fr.insee.rmes.bauhaus.concepts.defaultContributor=stamp",
                "fr.insee.rmes.bauhaus.sugoi.ui=sugoUi",
                "fr.insee.rmes.bauhaus.appHost=http://localhost",
                "fr.insee.rmes.bauhaus.activeModules=operations,concepts",
                "fr.insee.rmes.bauhaus.modules=operations,concepts"
        }
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,})
class PublicResourcesTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    StampsService stampsService;

    @Test
    void shouldReturnTheInitPayload() throws Exception {
        mvc.perform(get("/init/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(content().json("""
{
    "authorizationHost":"sugoUi",
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