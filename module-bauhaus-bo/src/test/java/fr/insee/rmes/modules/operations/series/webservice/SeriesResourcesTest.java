package fr.insee.rmes.modules.operations.series.webservice;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
import fr.insee.rmes.model.operations.PartialOperationSeries;
import fr.insee.rmes.model.operations.PartialOperationSeriesBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SeriesResources.class, properties = {
        "fr.insee.rmes.bauhaus.force.ssl = false"
})
@Import({UserProviderFromSecurityContext.class, DefaultSecurityContext.class})
class SeriesResourcesTest {

    @MockitoBean
    protected OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;

    @MockitoBean
    FakeUserConfiguration fakeUserConfiguration;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getSeries_shouldReturnListOfSeriesWithHalJson() throws Exception {
        // Given
        PartialOperationSeries series1 = PartialOperationSeriesBuilder.builder()
                .id("s1")
                .iri("http://example.org/series/s1")
                .label("Series 1")
                .altLabel("Alt Series 1")
                .build();

        PartialOperationSeries series2 = PartialOperationSeriesBuilder.builder()
                .id("s2")
                .iri("http://example.org/series/s2")
                .label("Series 2")
                .altLabel("Alt Series 2")
                .build();

        List<PartialOperationSeries> seriesList = List.of(series1, series2);
        when(operationsService.getSeries()).thenReturn(seriesList);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/operations/series"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("s1")))
                .andExpect(jsonPath("$[0].label", is("Series 1")))
                .andExpect(jsonPath("$[0].altLabel", is("Alt Series 1")))
                .andExpect(jsonPath("$[0]._links.self.href", is("http://localhost/operations/series/s1")))
                .andExpect(jsonPath("$[1].id", is("s2")))
                .andExpect(jsonPath("$[1].label", is("Series 2")))
                .andExpect(jsonPath("$[1].altLabel", is("Alt Series 2")))
                .andExpect(jsonPath("$[1]._links.self.href", is("http://localhost/operations/series/s2")));
    }

    @Test
    void getSeries_shouldReturnEmptyListWhenNoSeries() throws Exception {
        // Given
        when(operationsService.getSeries()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/operations/series"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}