package fr.insee.rmes.modules.datasets.distributions.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.datasets.distributions.model.Distribution;
import fr.insee.rmes.modules.datasets.distributions.model.DistributionsForSearch;
import fr.insee.rmes.modules.datasets.datasets.model.PartialDataset;
import fr.insee.rmes.modules.datasets.distributions.model.PartialDistribution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class DistributionResourcesTest {
    @InjectMocks
    private DistributionResources distributionResources;

    @Mock
    DistributionService distributionService;

    @Mock
    DatasetService datasetService;

    @Mock
    UserDecoder userDecoder;


    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDistributionsForSearch() throws RmesException {
        List<DistributionsForSearch> distributions = new ArrayList<>();
        distributions.add(new DistributionsForSearch(
                "id",
                "labelLg1",
                "validationStatus",
                "created",
                "updated",
                "altIdentifier",
                "id",
                "labelLg1",
                "creator",
                "disseminationStatus",
                "validationStatus",
                "wasGeneratedIRIs",
                "created",
                "updated"
        ));

        when(distributionService.getDistributionsForSearch()).thenReturn(distributions);
        Assertions.assertEquals(1, distributionResources.getDistributionsForSearch().size());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDistributionById() throws RmesException {
        Distribution distribution = new Distribution();
        distribution.setId("1");

        when(distributionService.getDistributionByID("1")).thenReturn(distribution);
        Assertions.assertEquals(distribution, distributionResources.getDistribution("1"));
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasetsForDistributionCreationAndAdmin() throws RmesException, MissingUserInformationException {
        List<PartialDataset> datasets = new ArrayList<>();
        datasets.add(new PartialDataset(
                "1",
                "label"
        ));

        when(datasetService.getDatasets()).thenReturn(datasets);
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("fakeUser", List.of(Roles.ADMIN), Set.of("fakeStampForDvAndQf"))));
        Assertions.assertEquals(1, distributionResources.getDatasetsForDistributionCreation(null).size());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasetsForDistributionCreationAndNotAdmin() throws RmesException, MissingUserInformationException {
        List<PartialDataset> datasets = new ArrayList<>();
        datasets.add(new PartialDataset(
                "1",
                "label"
        ));

        when(datasetService.getDatasetsForDistributionCreation(Set.of("fakeStampForDvAndQf"))).thenReturn(datasets);
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("fakeUser", List.of(), Set.of("fakeStampForDvAndQf"))));
        Assertions.assertEquals(1, distributionResources.getDatasetsForDistributionCreation(null).size());
    }

    @Test
    void shouldReturn201IfRmesExceptionWhenPostingADistribution() throws RmesException {
        when(distributionService.create(anyString())).thenReturn("result");
        Assertions.assertEquals("result", distributionResources.createDistribution(""));
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenUpdatingADistribution() throws RmesException {
        when(distributionService.update(anyString(), anyString())).thenReturn("result");
        Assertions.assertEquals("result", distributionResources.updateDistribution("", ""));
    }

    @Test
    void shouldCallPublishDistribution() throws RmesException {
        when(distributionService.publishDistribution("1")).thenReturn("result");
        Assertions.assertDoesNotThrow(() -> distributionResources.publishDistribution("1"));
    }

    @Test
    void getDistributions_shouldReturnListOfDistributions() throws RmesException {
        // Given
        List<PartialDistribution> distributions = new ArrayList<>();
        distributions.add(new PartialDistribution(
                "d1",
                "dataset1",
                "Distribution 1",
                "Distribution 1 FR",
                "Description 1",
                "Description 1 FR",
                "2024-01-01",
                "2024-01-15",
                "CSV",
                "1024",
                "http://example.com/dist1"
        ));
        distributions.add(new PartialDistribution(
                "d2",
                "dataset2",
                "Distribution 2",
                "Distribution 2 FR",
                "Description 2",
                "Description 2 FR",
                "2024-02-01",
                "2024-02-15",
                "JSON",
                "2048",
                "http://example.com/dist2"
        ));

        when(distributionService.getDistributions()).thenReturn(distributions);

        // When
        var result = distributionResources.getDistributions();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(200, result.getStatusCode().value());
        Assertions.assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(2, result.getBody().size());
    }

    @Test
    void getDistributions_shouldReturnEmptyListWhenNoDistributions() throws RmesException {
        // Given
        when(distributionService.getDistributions()).thenReturn(List.of());

        // When
        var result = distributionResources.getDistributions();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(200, result.getStatusCode().value());
        Assertions.assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(0, result.getBody().size());
    }

}


@WebMvcTest(
    value = DistributionResources.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class DistributionResourcesWebTest {

    @MockitoBean
    protected DistributionService distributionService;

    @MockitoBean
    protected DatasetService datasetService;

    @MockitoBean
    protected UserDecoder userDecoder;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getDistributions_shouldReturnListOfDistributionsWithHalJson() throws Exception {
        // Given
        PartialDistribution dist1 = new PartialDistribution(
                "d1",
                "dataset1",
                "Distribution 1",
                "Distribution 1 FR",
                "Description 1",
                "Description 1 FR",
                "2024-01-01",
                "2024-01-15",
                "CSV",
                "1024",
                "http://example.com/dist1"
        );

        PartialDistribution dist2 = new PartialDistribution(
                "d2",
                "dataset2",
                "Distribution 2",
                "Distribution 2 FR",
                "Description 2",
                "Description 2 FR",
                "2024-02-01",
                "2024-02-15",
                "JSON",
                "2048",
                "http://example.com/dist2"
        );

        List<PartialDistribution> distributions = List.of(dist1, dist2);
        when(distributionService.getDistributions()).thenReturn(distributions);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/distribution"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("d1")))
                .andExpect(jsonPath("$[0].idDataset", is("dataset1")))
                .andExpect(jsonPath("$[0].labelLg1", is("Distribution 1")))
                .andExpect(jsonPath("$[0].labelLg2", is("Distribution 1 FR")))
                .andExpect(jsonPath("$[0].format", is("CSV")))
                .andExpect(jsonPath("$[0]._links.self.href", is("http://localhost/distribution/d1")))
                .andExpect(jsonPath("$[1].id", is("d2")))
                .andExpect(jsonPath("$[1].idDataset", is("dataset2")))
                .andExpect(jsonPath("$[1].labelLg1", is("Distribution 2")))
                .andExpect(jsonPath("$[1].labelLg2", is("Distribution 2 FR")))
                .andExpect(jsonPath("$[1].format", is("JSON")))
                .andExpect(jsonPath("$[1]._links.self.href", is("http://localhost/distribution/d2")));
    }

    @Test
    void getDistributions_shouldReturnEmptyListWhenNoDistributions() throws Exception {
        // Given
        when(distributionService.getDistributions()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/distribution"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
