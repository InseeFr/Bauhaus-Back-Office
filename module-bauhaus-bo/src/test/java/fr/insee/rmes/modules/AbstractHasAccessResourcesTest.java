package fr.insee.rmes.modules;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collections;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;

/** Socle des tests RBAC des contrôleurs : une requête authentifiée face à un contrôle d'accès simulé. */
public abstract class AbstractHasAccessResourcesTest extends AbstractResourcesEnvProd {

    /** Joue la requête authentifiée, le contrôle RBAC répondant {@code hasAccessReturn}. */
    protected void assertStatusWithAccess(
            AbstractMockHttpServletRequestBuilder<?> request, Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}
