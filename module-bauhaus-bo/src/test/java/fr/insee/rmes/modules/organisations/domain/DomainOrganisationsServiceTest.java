package fr.insee.rmes.modules.organisations.domain;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainOrganisationsServiceTest {

    @Mock
    private OrganisationsRepository organisationsRepository;

    private DomainOrganisationsService service;

    @BeforeEach
    void setUp() {
        service = new DomainOrganisationsService(organisationsRepository);
    }

    @Test
    void shouldGetCompactOrganisation() throws OrganisationFetchException {
        // Given
        String organisationId = "ORG-001";
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/def/base#OrganismUnit_1234");
        LocalisedLabel label = new LocalisedLabel("Direction des statistiques", Lang.FR);
        CompactOrganisation expectedOrganisation = new CompactOrganisation(iri, organisationId, label);

        when(organisationsRepository.getCompactOrganisation(organisationId))
            .thenReturn(expectedOrganisation);

        // When
        CompactOrganisation result = service.getCompactOrganisation(organisationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedOrganisation);
        assertThat(result.identifier()).isEqualTo(organisationId);
        assertThat(result.label().value()).isEqualTo("Direction des statistiques");
        assertThat(result.label().lang()).isEqualTo(Lang.FR);
        assertThat(result.iri()).isEqualTo(iri);

        verify(organisationsRepository, times(1)).getCompactOrganisation(organisationId);
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenRepositoryFails() throws OrganisationFetchException {
        // Given
        String organisationId = "ORG-001";
        when(organisationsRepository.getCompactOrganisation(organisationId))
            .thenThrow(new OrganisationFetchException());

        // When/Then
        assertThatThrownBy(() -> service.getCompactOrganisation(organisationId))
            .isInstanceOf(OrganisationFetchException.class);

        verify(organisationsRepository, times(1)).getCompactOrganisation(organisationId);
    }

    @Test
    void shouldDelegateToRepository() throws OrganisationFetchException {
        // Given
        String organisationId = "ORG-123";
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://example.org/org#123");
        LocalisedLabel label = new LocalisedLabel("Test Organisation", Lang.EN);
        CompactOrganisation organisation = new CompactOrganisation(iri, organisationId, label);

        when(organisationsRepository.getCompactOrganisation(organisationId))
            .thenReturn(organisation);

        // When
        service.getCompactOrganisation(organisationId);

        // Then
        verify(organisationsRepository, times(1)).getCompactOrganisation(organisationId);
        verifyNoMoreInteractions(organisationsRepository);
    }

    @Test
    void shouldReturnOrganisationWithCorrectData() throws OrganisationFetchException {
        // Given
        String organisationId = "ORG-999";
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/def/base#OrganismUnit_999");
        LocalisedLabel frenchLabel = new LocalisedLabel("Organisation de test", Lang.FR);
        CompactOrganisation organisation = new CompactOrganisation(iri, organisationId, frenchLabel);

        when(organisationsRepository.getCompactOrganisation(organisationId))
            .thenReturn(organisation);

        // When
        CompactOrganisation result = service.getCompactOrganisation(organisationId);

        // Then
        assertThat(result.identifier()).isEqualTo("ORG-999");
        assertThat(result.label().value()).isEqualTo("Organisation de test");
        assertThat(result.label().lang()).isEqualTo(Lang.FR);
        assertThat(result.iri().stringValue()).isEqualTo("http://rdf.insee.fr/def/base#OrganismUnit_999");
    }

    @Test
    void shouldHandleMultipleConsecutiveCalls() throws OrganisationFetchException {
        // Given
        String orgId1 = "ORG-001";
        String orgId2 = "ORG-002";

        IRI iri1 = SimpleValueFactory.getInstance().createIRI("http://example.org/org#1");
        IRI iri2 = SimpleValueFactory.getInstance().createIRI("http://example.org/org#2");

        CompactOrganisation org1 = new CompactOrganisation(iri1, orgId1, new LocalisedLabel("Org 1", Lang.FR));
        CompactOrganisation org2 = new CompactOrganisation(iri2, orgId2, new LocalisedLabel("Org 2", Lang.EN));

        when(organisationsRepository.getCompactOrganisation(orgId1)).thenReturn(org1);
        when(organisationsRepository.getCompactOrganisation(orgId2)).thenReturn(org2);

        // When
        CompactOrganisation result1 = service.getCompactOrganisation(orgId1);
        CompactOrganisation result2 = service.getCompactOrganisation(orgId2);

        // Then
        assertThat(result1).isEqualTo(org1);
        assertThat(result2).isEqualTo(org2);
        verify(organisationsRepository).getCompactOrganisation(orgId1);
        verify(organisationsRepository).getCompactOrganisation(orgId2);
    }

    @Test
    void shouldGetMultipleCompactOrganisations() throws OrganisationFetchException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-001", "ORG-002", "ORG-003");

        IRI iri1 = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/def/base#OrganismUnit_1");
        IRI iri2 = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/def/base#OrganismUnit_2");
        IRI iri3 = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/def/base#OrganismUnit_3");

        CompactOrganisation org1 = new CompactOrganisation(iri1, "ORG-001", new LocalisedLabel("Direction des statistiques", Lang.FR));
        CompactOrganisation org2 = new CompactOrganisation(iri2, "ORG-002", new LocalisedLabel("Statistics Department", Lang.EN));
        CompactOrganisation org3 = new CompactOrganisation(iri3, "ORG-003", new LocalisedLabel("Service des données", Lang.FR));

        List<CompactOrganisation> expectedOrganisations = Arrays.asList(org1, org2, org3);

        when(organisationsRepository.getCompactOrganisations(organisationIds))
            .thenReturn(expectedOrganisations);

        // When
        List<CompactOrganisation> result = service.getCompactOrganisations(organisationIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(expectedOrganisations);
        assertThat(result.get(0).identifier()).isEqualTo("ORG-001");
        assertThat(result.get(1).identifier()).isEqualTo("ORG-002");
        assertThat(result.get(2).identifier()).isEqualTo("ORG-003");

        verify(organisationsRepository, times(1)).getCompactOrganisations(organisationIds);
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenRepositoryFailsForMultiple() throws OrganisationFetchException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-001", "ORG-002");
        when(organisationsRepository.getCompactOrganisations(organisationIds))
            .thenThrow(new OrganisationFetchException());

        // When/Then
        assertThatThrownBy(() -> service.getCompactOrganisations(organisationIds))
            .isInstanceOf(OrganisationFetchException.class);

        verify(organisationsRepository, times(1)).getCompactOrganisations(organisationIds);
    }

    @Test
    void shouldDelegateToRepositoryForMultiple() throws OrganisationFetchException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-001", "ORG-002");

        IRI iri1 = SimpleValueFactory.getInstance().createIRI("http://example.org/org#1");
        IRI iri2 = SimpleValueFactory.getInstance().createIRI("http://example.org/org#2");

        List<CompactOrganisation> organisations = Arrays.asList(
            new CompactOrganisation(iri1, "ORG-001", new LocalisedLabel("Org 1", Lang.FR)),
            new CompactOrganisation(iri2, "ORG-002", new LocalisedLabel("Org 2", Lang.EN))
        );

        when(organisationsRepository.getCompactOrganisations(organisationIds))
            .thenReturn(organisations);

        // When
        service.getCompactOrganisations(organisationIds);

        // Then
        verify(organisationsRepository, times(1)).getCompactOrganisations(organisationIds);
        verifyNoMoreInteractions(organisationsRepository);
    }

    @Test
    void shouldReturnEmptyListWhenNoOrganisationsFound() throws OrganisationFetchException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-999");
        List<CompactOrganisation> emptyList = List.of();

        when(organisationsRepository.getCompactOrganisations(organisationIds))
            .thenReturn(emptyList);

        // When
        List<CompactOrganisation> result = service.getCompactOrganisations(organisationIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(organisationsRepository, times(1)).getCompactOrganisations(organisationIds);
    }

    @Test
    void shouldReturnTrueWhenOrganisationExists() throws OrganisationFetchException {
        // Given
        String iri = "http://bauhaus/organisations/insee/HIE2000052";
        when(organisationsRepository.checkIfOrganisationExists(iri)).thenReturn(true);

        // When
        boolean result = service.checkIfOrganisationExists(iri);

        // Then
        assertThat(result).isTrue();

        verify(organisationsRepository, times(1)).checkIfOrganisationExists(iri);
    }

    @Test
    void shouldReturnFalseWhenOrganisationDoesNotExist() throws OrganisationFetchException {
        // Given
        String iri = "http://bauhaus/organisations/insee/NON_EXISTENT";
        when(organisationsRepository.checkIfOrganisationExists(iri)).thenReturn(false);

        // When
        boolean result = service.checkIfOrganisationExists(iri);

        // Then
        assertThat(result).isFalse();

        verify(organisationsRepository, times(1)).checkIfOrganisationExists(iri);
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenCheckExistenceFails() throws OrganisationFetchException {
        // Given
        String iri = "http://bauhaus/organisations/insee/HIE2000052";
        when(organisationsRepository.checkIfOrganisationExists(iri))
            .thenThrow(new OrganisationFetchException());

        // When/Then
        assertThatThrownBy(() -> service.checkIfOrganisationExists(iri))
            .isInstanceOf(OrganisationFetchException.class);

        verify(organisationsRepository, times(1)).checkIfOrganisationExists(iri);
    }

    @Test
    void shouldDelegateExistenceCheckToRepository() throws OrganisationFetchException {
        // Given
        String iri = "http://bauhaus/organisations/insee/HIE2000052";
        when(organisationsRepository.checkIfOrganisationExists(iri)).thenReturn(true);

        // When
        service.checkIfOrganisationExists(iri);

        // Then
        verify(organisationsRepository, times(1)).checkIfOrganisationExists(iri);
        verifyNoMoreInteractions(organisationsRepository);
    }
}
