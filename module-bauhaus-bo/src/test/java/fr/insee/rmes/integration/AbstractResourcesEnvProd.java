package fr.insee.rmes.integration;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class AbstractResourcesEnvProd {

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    protected DocumentsService documentsService;

    @MockitoBean(name = "propertiesAccessPrivilegesChecker")
    protected AccessPrivilegesCheckerService checker;

    @Autowired
    protected MockMvc mvc;

    @MockitoBean
    protected JwtProperties jwtProperties;

    @MockitoBean
    protected OrganisationsService organisationsService;

    protected final String idep = "xxxxxx";
    protected final String timbre = "XX59-YYY";
}
