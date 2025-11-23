package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.webservice.PublicResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
        "jwt.stampClaim=" + STAMP_CLAIM,
        "jwt.roleClaim=" + ROLE_CLAIM,
        "jwt.idClaim=" + ID_CLAIM,
        "jwt.roleClaimConfig.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
        "jwt.sourceClaim=source",
        "logging.level.org.springframework.security=DEBUG",
        "logging.level.org.springframework.security.web.access=TRACE",
        "logging.level.fr.insee.rmes.config.auth=TRACE",
    }
)
@Import(JwtProperties.class)
class PublicResourcesAuthorizationsTest extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrganisationService organisationService;

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
