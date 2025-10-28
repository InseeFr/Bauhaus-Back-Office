package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
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
}
