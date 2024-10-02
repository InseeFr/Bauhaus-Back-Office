package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.accesscontrol.AuthorizationCheckerWithResourceOwnershipByStamp;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.AccessControlConfiguration;
import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties(RBACConfiguration.class)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class,
        AccessControlConfiguration.class,
        AuthorizationCheckerWithResourceOwnershipByStamp.class})
public class ConfigurationForTestWithAuth {
}
