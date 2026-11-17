package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static fr.insee.rmes.modules.ddi.physical_instances.webservice.DdiResourcesTestSupport.assertOkListOfSize;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith({MockitoExtension.class, LocalhostRequestContextExtension.class})
class LogicalProductResourcesTest {

    @Mock
    private DDIService ddiService;

    private LogicalProductResources logicalProductResources;

    @BeforeEach
    void setUp() {
        logicalProductResources = new LogicalProductResources(ddiService);
    }

    @Test
    void shouldGetLogicalProducts() {
        List<PartialLogicalProduct> expectedProducts = new ArrayList<>();
        expectedProducts.add(new PartialLogicalProduct("lp-1", "Logical Product 1", new Date(), "fr.insee"));
        expectedProducts.add(new PartialLogicalProduct("lp-2", "Logical Product 2", new Date(), "fr.insee"));
        when(ddiService.getLogicalProducts()).thenReturn(expectedProducts);

        ResponseEntity<List<PartialLogicalProductResponse>> response = logicalProductResources.getLogicalProducts();

        List<PartialLogicalProductResponse> result = assertOkListOfSize(response, 2);

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
