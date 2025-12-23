package fr.insee.rmes.config;

import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
@Import({UserProviderFromSecurityContext.class, OidcUserDecoder.class, JwtProperties.class})
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.force.ssl = false",
        "jwt.stampClaim=timbre",
        "jwt.roleClaim=roles",
        "jwt.idClaim=idep",
        "jwt.roleClaimConfig.roles=roles",
        "jwt.sourceClaim=source"
})
public abstract class BaseConfigForMvcTests {

    @MockitoBean
    protected OrganisationsService organisationsService;
}
