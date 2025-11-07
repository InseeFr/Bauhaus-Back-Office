package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.port.serverside.UserDecoder;
import fr.insee.rmes.domain.auth.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.model.dataset.DistributionsForSearch;
import fr.insee.rmes.model.dataset.PartialDataset;
import fr.insee.rmes.model.dataset.PartialDistribution;
import fr.insee.rmes.onion.infrastructure.webservice.datasets.DistributionResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

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
    void shouldReturn200IfRmesExceptionWhenFetchingDistributions() throws RmesException {
        List<PartialDistribution> distributions = new ArrayList<>();
        distributions.add(new PartialDistribution(
                "1",
                "2",
                "labelLg1",
                "labelLg2",
                "description Lg1",
                "description Lg2",
                "created",
                "updated",
                "format",
                "0",
                "url"
        ));

        when(distributionService.getDistributions()).thenReturn(distributions);
        Assertions.assertEquals(1, distributionResources.getDistributions().size());
    }

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
    void shouldReturn200IfRmesExceptionWhenFetchingDatasetsForDistributionCreationAndAdmin() throws RmesException {
        List<PartialDataset> datasets = new ArrayList<>();
        datasets.add(new PartialDataset(
                "1",
                "value"
        ));

        when(datasetService.getDatasets()).thenReturn(datasets);
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("fakeUser", List.of(Roles.ADMIN), "fakeStampForDvAndQf")));
        Assertions.assertEquals(1, distributionResources.getDatasetsForDistributionCreation(null).size());
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasetsForDistributionCreationAndNotAdmin() throws RmesException {
        List<PartialDataset> datasets = new ArrayList<>();
        datasets.add(new PartialDataset(
                "1",
                "value"
        ));

        when(datasetService.getDatasetsForDistributionCreation("fakeStampForDvAndQf")).thenReturn(datasets);
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("fakeUser", List.of(), "fakeStampForDvAndQf")));
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

}
