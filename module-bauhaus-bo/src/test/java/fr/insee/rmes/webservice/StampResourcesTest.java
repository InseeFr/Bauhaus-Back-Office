package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampResourcesTest {

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private StampResources stampResources;

    @Test
    void shouldReturnStampsWhenGetStamps() throws RmesException {
        List<String> expectedStamps = List.of("DG75-A001", "DG75-B001", "DG75-C001");
        when(organisationService.getStamps()).thenReturn(expectedStamps);

        ResponseEntity<List<String>> response = stampResources.getStamps();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedStamps);
    }

    @Test
    void shouldReturnEmptyListWhenNoStamps() throws RmesException {
        List<String> expectedStamps = List.of();
        when(organisationService.getStamps()).thenReturn(expectedStamps);

        ResponseEntity<List<String>> response = stampResources.getStamps();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturnOrganisationOptionsWhenGetOrganisationOptions() throws RmesException {
        List<OrganisationOption> expectedOptions = List.of(
                new OrganisationOption("DG75-A001", "Direction Générale 75 - Service A001"),
                new OrganisationOption("DR13-DIR", "Direction Régionale 13 - Direction")
        );
        when(organisationService.getOrganisations()).thenReturn(expectedOptions);

        ResponseEntity<List<OrganisationOption>> response = stampResources.getOrganisationOptions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).stamp()).isEqualTo("DG75-A001");
        assertThat(response.getBody().get(0).label()).isEqualTo("Direction Générale 75 - Service A001");
        assertThat(response.getBody().get(1).stamp()).isEqualTo("DR13-DIR");
        assertThat(response.getBody().get(1).label()).isEqualTo("Direction Régionale 13 - Direction");
    }

    @Test
    void shouldReturnEmptyListWhenNoOrganisations() throws RmesException {
        List<OrganisationOption> expectedOptions = List.of();
        when(organisationService.getOrganisations()).thenReturn(expectedOptions);

        ResponseEntity<List<OrganisationOption>> response = stampResources.getOrganisationOptions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
