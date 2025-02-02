package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.webservice.datasets.DistributionResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldReturn200IfRmesExceptionWhenFetchingDatasets() throws RmesException {
        when(distributionService.getDistributions()).thenReturn("result");
        Assertions.assertEquals("result", distributionResources.getDistributions());
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
        when(datasetService.getDatasets()).thenReturn("result");
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("fakeUser", List.of(Roles.ADMIN), "fakeStampForDvAndQf")));
        Assertions.assertEquals("result", distributionResources.getDatasetsForDistributionCreation(null));
    }

    @Test
    void shouldReturn200IfRmesExceptionWhenFetchingDatasetsForDistributionCreationAndNotAdmin() throws RmesException {
        when(datasetService.getDatasetsForDistributionCreation("fakeStampForDvAndQf")).thenReturn("result");
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("fakeUser", List.of(), "fakeStampForDvAndQf")));
        Assertions.assertEquals("result", distributionResources.getDatasetsForDistributionCreation(null));
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
        when(distributionResources.publishDistribution("1")).thenReturn("result");
        Assertions.assertEquals("result", distributionResources.publishDistribution("1"));
    }

}
