package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.external.services.authentication.stamps.StampsService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.webservice.PublicResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.PUBLIC_RESOURCES_ANT_PATTERNS;
import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers= PublicResources.class,
    properties = {
        "fr.insee.rmes.bauhaus.env=PROD",
        "jwt.stamp-claim=" + STAMP_CLAIM,
        "jwt.role-claim=" + ROLE_CLAIM,
        "jwt.id-claim=" + ID_CLAIM,
        "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
        "logging.level.org.springframework.security=DEBUG",
        "logging.level.org.springframework.security.web.access=TRACE",
        "logging.level.fr.insee.rmes.config.auth=TRACE",
    }
)
class PublicResourcesAuthorizationsTest extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StampsService stampsService;

    public static Stream<Arguments> endpointsProvider() {
        return Stream.of(PUBLIC_RESOURCES_ANT_PATTERNS).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("endpointsProvider")
    void ok_withoutAuth(String endpoint) throws Exception {
        this.mvc.perform(get(endpoint).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
