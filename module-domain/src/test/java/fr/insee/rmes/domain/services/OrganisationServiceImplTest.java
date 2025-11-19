package fr.insee.rmes.domain.services;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.domain.port.serverside.OrganisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceImplTest {

    @Mock
    private OrganisationRepository organisationRepository;

    private OrganisationServiceImpl organisationService;

    @BeforeEach
    void setUp() {
        organisationService = new OrganisationServiceImpl(organisationRepository);
    }

    @Test
    void shouldReturnStamps() throws RmesException {
        // When
        List<String> stamps = organisationService.getStamps();

        // Then
        assertThat(stamps).isNotEmpty();
        assertThat(stamps).contains("DG75-A001", "DR13-DIR", "SSM-DARES");
    }

    @Test
    void shouldReturnAllOrganisations() throws RmesException {
        // Given
        List<OrganisationOption> expectedOrganisations = List.of(
                new OrganisationOption("DG75-A001", "Direction Générale 75 - Service A001"),
                new OrganisationOption("DR13-DIR", "Direction Régionale 13 - Direction")
        );
        when(organisationRepository.getOrganisations()).thenReturn(expectedOrganisations);

        // When
        List<OrganisationOption> result = organisationService.getOrganisations();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedOrganisations);
        verify(organisationRepository).getOrganisations();
    }

    @Test
    void shouldReturnOrganisationByIdentifier() throws RmesException {
        // Given
        String identifier = "DG75-A001";
        OrganisationOption expectedOrganisation = new OrganisationOption("DG75-A001", "Direction Générale 75 - Service A001");
        when(organisationRepository.getOrganisation(identifier)).thenReturn(expectedOrganisation);

        // When
        OrganisationOption result = organisationService.getOrganisation(identifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.stamp()).isEqualTo("DG75-A001");
        assertThat(result.label()).isEqualTo("Direction Générale 75 - Service A001");
        verify(organisationRepository).getOrganisation(identifier);
    }

    @Test
    void shouldReturnNullWhenOrganisationNotFound() throws RmesException {
        // Given
        String identifier = "UNKNOWN";
        when(organisationRepository.getOrganisation(identifier)).thenReturn(null);

        // When
        OrganisationOption result = organisationService.getOrganisation(identifier);

        // Then
        assertThat(result).isNull();
        verify(organisationRepository).getOrganisation(identifier);
    }

    @Test
    void shouldReturnOrganisationsMapForMultipleIdentifiers() throws RmesException {
        // Given
        List<String> identifiers = List.of("DG75-A001", "DR13-DIR", "SSM-DARES");
        Map<String, OrganisationOption> expectedMap = Map.of(
                "DG75-A001", new OrganisationOption("DG75-A001", "Direction Générale 75 - Service A001"),
                "DR13-DIR", new OrganisationOption("DR13-DIR", "Direction Régionale 13"),
                "SSM-DARES", new OrganisationOption("SSM-DARES", "Service DARES")
        );
        when(organisationRepository.getOrganisationsMap(identifiers)).thenReturn(expectedMap);

        // When
        Map<String, OrganisationOption> result = organisationService.getOrganisationsMap(identifiers);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsKeys("DG75-A001", "DR13-DIR", "SSM-DARES");
        assertThat(result.get("DG75-A001").label()).isEqualTo("Direction Générale 75 - Service A001");
        verify(organisationRepository).getOrganisationsMap(identifiers);
    }

    @Test
    void shouldReturnEmptyMapWhenNoIdentifiersProvided() throws RmesException {
        // Given
        List<String> identifiers = List.of();
        when(organisationRepository.getOrganisationsMap(identifiers)).thenReturn(Map.of());

        // When
        Map<String, OrganisationOption> result = organisationService.getOrganisationsMap(identifiers);

        // Then
        assertThat(result).isEmpty();
        verify(organisationRepository).getOrganisationsMap(identifiers);
    }

    @Test
    void shouldReturnEmptyMapWhenNullIdentifiers() throws RmesException {
        // Given
        when(organisationRepository.getOrganisationsMap(null)).thenReturn(Map.of());

        // When
        Map<String, OrganisationOption> result = organisationService.getOrganisationsMap(null);

        // Then
        assertThat(result).isEmpty();
        verify(organisationRepository).getOrganisationsMap(null);
    }

    @Test
    void shouldReturnPartialMapWhenSomeOrganisationsNotFound() throws RmesException {
        // Given
        List<String> identifiers = List.of("DG75-A001", "UNKNOWN", "DR13-DIR");
        Map<String, OrganisationOption> expectedMap = Map.of(
                "DG75-A001", new OrganisationOption("DG75-A001", "Direction Générale 75 - Service A001"),
                "DR13-DIR", new OrganisationOption("DR13-DIR", "Direction Régionale 13")
                // UNKNOWN is not in the map (not found)
        );
        when(organisationRepository.getOrganisationsMap(identifiers)).thenReturn(expectedMap);

        // When
        Map<String, OrganisationOption> result = organisationService.getOrganisationsMap(identifiers);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("DG75-A001", "DR13-DIR");
        assertThat(result).doesNotContainKey("UNKNOWN");
        verify(organisationRepository).getOrganisationsMap(identifiers);
    }
}