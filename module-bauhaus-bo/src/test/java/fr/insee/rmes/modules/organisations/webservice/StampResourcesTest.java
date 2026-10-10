package fr.insee.rmes.modules.organisations.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class StampResourcesTest {

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private StampResources stampResources;

    private List<String> okBodyOfGetStamps(List<String> stamps) throws RmesException {
        when(organisationService.getStamps()).thenReturn(stamps);

        ResponseEntity<List<String>> response = stampResources.getStamps();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private List<OrganisationOption> okBodyOfGetOrganisationOptions(List<OrganisationOption> options)
            throws RmesException {
        when(organisationService.getOrganisations()).thenReturn(options);

        ResponseEntity<List<OrganisationOption>> response = stampResources.getOrganisationOptions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    @Test
    void shouldReturnStampsWhenGetStamps() throws RmesException {
        List<String> expectedStamps = List.of("DG75-A001", "DG75-B001", "DG75-C001");

        assertThat(okBodyOfGetStamps(expectedStamps)).isEqualTo(expectedStamps);
    }

    @Test
    void shouldReturnEmptyListWhenNoStamps() throws RmesException {
        assertThat(okBodyOfGetStamps(List.of())).isEmpty();
    }

    @Test
    void shouldReturnOrganisationOptionsWhenGetOrganisationOptions() throws RmesException {
        List<OrganisationOption> expectedOptions = List.of(
                new OrganisationOption("DG75-A001", "Direction Générale 75 - Service A001"),
                new OrganisationOption("DR13-DIR", "Direction Régionale 13 - Direction"));

        List<OrganisationOption> body = okBodyOfGetOrganisationOptions(expectedOptions);

        assertThat(body).hasSize(2);
        assertThat(body.get(0).stamp()).isEqualTo("DG75-A001");
        assertThat(body.get(0).label()).isEqualTo("Direction Générale 75 - Service A001");
        assertThat(body.get(1).stamp()).isEqualTo("DR13-DIR");
        assertThat(body.get(1).label()).isEqualTo("Direction Régionale 13 - Direction");
    }

    @Test
    void shouldReturnEmptyListWhenNoOrganisations() throws RmesException {
        assertThat(okBodyOfGetOrganisationOptions(List.of())).isEmpty();
    }
}
