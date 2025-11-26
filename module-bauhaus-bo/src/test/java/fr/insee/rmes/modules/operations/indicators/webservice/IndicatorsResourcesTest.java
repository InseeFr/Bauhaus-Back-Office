package fr.insee.rmes.modules.operations.indicators.webservice;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.PartialOperationIndicator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IndicatorsResourcesTest {
    @InjectMocks
    private IndicatorsResources indicatorsResources;

    @Mock
    OperationsService operationsService;

    @Mock
    OperationsDocumentationsService documentationsService;

    @Test
    void get_indicators_should_return_list_of_indicators() throws RmesException {
        // Given
        List<PartialOperationIndicator> indicators = new ArrayList<>();
        indicators.add(new PartialOperationIndicator(
                "ind1",
                "Indicator 1",
                "Indicateur 1"
        ));
        indicators.add(new PartialOperationIndicator(
                "ind2",
                "Indicator 2",
                "Indicateur 2"
        ));

        when(operationsService.getIndicators()).thenReturn(indicators);

        // When
        var result = indicatorsResources.getIndicators();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(200, result.getStatusCode().value());
        Assertions.assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(2, result.getBody().size());
    }

    @Test
    void get_indicators_should_return_empty_list_when_no_indicators() throws RmesException {
        // Given
        when(operationsService.getIndicators()).thenReturn(List.of());

        // When
        var result = indicatorsResources.getIndicators();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(200, result.getStatusCode().value());
        Assertions.assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(0, result.getBody().size());
    }

    @Test
    void get_indicators_should_add_self_link_to_each_indicator() throws RmesException {
        // Given
        List<PartialOperationIndicator> indicators = new ArrayList<>();
        indicators.add(new PartialOperationIndicator(
                "ind1",
                "Indicator 1",
                "Indicateur 1"
        ));

        when(operationsService.getIndicators()).thenReturn(indicators);

        // When
        var result = indicatorsResources.getIndicators();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1, result.getBody().size());

        var indicatorResponse = result.getBody().get(0);
        Assertions.assertTrue(indicatorResponse.hasLinks());
        Assertions.assertTrue(indicatorResponse.getLink("self").isPresent());
    }
}


@WebMvcTest(value = IndicatorsResources.class, properties = {
        "fr.insee.rmes.bauhaus.force.ssl = false"
})
@Import({UserProviderFromSecurityContext.class})
class IndicatorsResourcesWebTest {

    @MockitoBean
    protected OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;


    @Autowired
    MockMvc mockMvc;

    @Test
    void get_indicators_should_return_list_of_indicators_with_hal_json() throws Exception {
        // Given
        PartialOperationIndicator ind1 = new PartialOperationIndicator(
                "ind1",
                "Indicator 1",
                "Indicateur 1"
        );

        PartialOperationIndicator ind2 = new PartialOperationIndicator(
                "ind2",
                "Indicator 2",
                "Indicateur 2"
        );

        List<PartialOperationIndicator> indicators = List.of(ind1, ind2);
        when(operationsService.getIndicators()).thenReturn(indicators);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/operations/indicators"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("ind1")))
                .andExpect(jsonPath("$[0].label", is("Indicator 1")))
                .andExpect(jsonPath("$[0].altLabel", is("Indicateur 1")))
                .andExpect(jsonPath("$[0]._links.self.href", is("http://localhost/operations/indicator/ind1")))
                .andExpect(jsonPath("$[1].id", is("ind2")))
                .andExpect(jsonPath("$[1].label", is("Indicator 2")))
                .andExpect(jsonPath("$[1].altLabel", is("Indicateur 2")))
                .andExpect(jsonPath("$[1]._links.self.href", is("http://localhost/operations/indicator/ind2")));
    }

    @Test
    void get_indicators_should_return_empty_list_when_no_indicators() throws Exception {
        // Given
        when(operationsService.getIndicators()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/operations/indicators"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}