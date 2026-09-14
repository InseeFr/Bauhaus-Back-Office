package fr.insee.rmes.modules.organisations.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationSummary;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class OrganisationsResourcesTest {

    @Mock
    private OrganisationsService organisationsService;

    @Mock
    private OrganizationsService organizationsService;

    @Test
    void shouldListOrganisationsFromNewServiceMappingIdentifierToId() throws Throwable {
        // Given
        when(organisationsService.getOrganisations())
                .thenReturn(List.of(
                        new OrganisationSummary(
                                "http://bauhaus/organisations/ORG-001",
                                "ORG-001",
                                "DG75-L001",
                                "Direction des statistiques",
                                "Statistics Directorate"),
                        new OrganisationSummary(
                                "http://bauhaus/organisations/ORG-002",
                                "ORG-002",
                                "DG75-L002",
                                "Service des données",
                                "Data Department")));
        var resources = new OrganisationsResources(organisationsService, organizationsService);

        // When
        ResponseEntity<Object> response = resources.getOrganizations(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo(List.of(
                        new OrganisationResponse(
                                "http://bauhaus/organisations/ORG-001",
                                "ORG-001",
                                "DG75-L001",
                                "Direction des statistiques",
                                "Statistics Directorate"),
                        new OrganisationResponse(
                                "http://bauhaus/organisations/ORG-002",
                                "ORG-002",
                                "DG75-L002",
                                "Service des données",
                                "Data Department")));
        verifyNoInteractions(organizationsService);
    }
}
