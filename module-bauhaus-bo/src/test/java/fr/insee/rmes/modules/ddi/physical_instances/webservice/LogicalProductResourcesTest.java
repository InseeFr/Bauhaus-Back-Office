package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialLogicalProductResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class LogicalProductResourcesTest {

    @Mock
    private DDIService ddiService;

    private LogicalProductResources logicalProductResources;

    @BeforeEach
    void setUp() {
        logicalProductResources = new LogicalProductResources(ddiService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldGetLogicalProducts() {
        List<PartialLogicalProduct> expectedProducts = new ArrayList<>();
        expectedProducts.add(new PartialLogicalProduct("lp-1", "Logical Product 1", new Date(), "fr.insee"));
        expectedProducts.add(new PartialLogicalProduct("lp-2", "Logical Product 2", new Date(), "fr.insee"));
        when(ddiService.getLogicalProducts()).thenReturn(expectedProducts);

        ResponseEntity<List<PartialLogicalProductResponse>> response = logicalProductResources.getLogicalProducts();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialLogicalProductResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first product data and links
        assertEquals("lp-1", result.getFirst().getId());
        assertEquals("Logical Product 1", result.getFirst().getLabel());
        assertNotNull(result.getFirst().getLinks());
        assertEquals(1, result.getFirst().getLinks().toList().size());
        assertEquals(
                "http://localhost:8080/ddi/logical-product/fr.insee/lp-1",
                result.getFirst().getRequiredLink("self").getHref());

        // Verify second product data and links
        assertEquals("lp-2", result.get(1).getId());
        assertEquals("Logical Product 2", result.get(1).getLabel());
        assertNotNull(result.get(1).getLinks());
        assertEquals(1, result.get(1).getLinks().toList().size());
        assertEquals(
                "http://localhost:8080/ddi/logical-product/fr.insee/lp-2",
                result.get(1).getRequiredLink("self").getHref());

        verify(ddiService).getLogicalProducts();
    }
}
