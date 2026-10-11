package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Lie une requête {@code http://localhost:8080} au thread le temps de chaque test, pour que
 * {@code ServletUriComponentsBuilder} puisse construire les liens HATEOAS des contrôleurs DDI.
 */
class LocalhostRequestContextExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        RequestContextHolder.resetRequestAttributes();
    }
}
