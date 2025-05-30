package fr.insee.rmes.integration;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.rbac.AccessPrivilegesChecker;
import fr.insee.rmes.rbac.PropertiesAccessPrivilegesChecker;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({
    Config.class,
    OpenIDConnectSecurityContext.class,
    DefaultSecurityContext.class,
    CommonSecurityConfiguration.class,
    UserProviderFromSecurityContext.class,
    BauhausMethodSecurityExpressionHandler.class,
    PropertiesAccessPrivilegesChecker.class
})
public abstract class AbstractResourcesEnvProd {

    @MockitoBean
    protected JwtDecoder jwtDecoder;
    @MockitoBean
    protected DocumentsService documentsService;


    @MockitoBean
    protected AccessPrivilegesChecker checker;
}
