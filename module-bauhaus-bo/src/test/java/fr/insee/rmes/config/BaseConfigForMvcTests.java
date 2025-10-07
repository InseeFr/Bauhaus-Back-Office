package fr.insee.rmes.config;

import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@Import({UserProviderFromSecurityContext.class, DefaultSecurityContext.class, FakeUserConfiguration.class})
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.force.ssl = false"
})
public class BaseConfigForMvcTests {
}
