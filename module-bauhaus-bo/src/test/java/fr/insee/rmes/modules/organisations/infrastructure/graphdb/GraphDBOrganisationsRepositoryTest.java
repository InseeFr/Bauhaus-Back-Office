package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphDBOrganisationsRepositoryTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    private GraphDBOrganisationsRepository repository;

    @BeforeAll
    static void initGenericQueries() {
        GenericQueries.setConfig(new ConfigStub());
    }

    @BeforeEach
    void setUp() {
        repository = new GraphDBOrganisationsRepository(repositoryGestion);
    }

    @Test
    void shouldGetCompactOrganisation() throws RmesException, OrganisationFetchException {
        // Given
        String organisationId = "ORG-001";
        String jsonResponse = """
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_1234",
                "identifier": "ORG-001",
                "label": "Direction des statistiques",
                "label_lg": "fr"
            }
            """;

        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject(jsonResponse));

        // When
        CompactOrganisation result = repository.getCompactOrganisation(organisationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.identifier()).isEqualTo("ORG-001");
        assertThat(result.label().value()).isEqualTo("Direction des statistiques");
        assertThat(result.iri().stringValue()).isEqualTo("http://rdf.insee.fr/def/base#OrganismUnit_1234");

        verify(repositoryGestion).getResponseAsObject(anyString());
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenRepositoryThrowsRmesException() throws RmesException {
        // Given
        String organisationId = "ORG-001";
        when(repositoryGestion.getResponseAsObject(anyString()))
            .thenThrow(new RmesException(500, "Database error", "Error accessing repository"));

        // When/Then
        assertThatThrownBy(() -> repository.getCompactOrganisation(organisationId))
            .isInstanceOf(OrganisationFetchException.class);

        verify(repositoryGestion).getResponseAsObject(anyString());
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenJsonParsingFails() throws RmesException {
        // Given
        String organisationId = "ORG-001";
        // Valid JSON but missing required fields for GraphDbCompactOrganisation
        String incompleteJson = """
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_1234"
            }
            """;

        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject(incompleteJson));

        // When/Then
        assertThatThrownBy(() -> repository.getCompactOrganisation(organisationId))
            .isInstanceOf(OrganisationFetchException.class);

        verify(repositoryGestion).getResponseAsObject(anyString());
    }

    @Test
    void shouldCallRepositoryWithCorrectQuery() throws RmesException, OrganisationFetchException {
        // Given
        String organisationId = "ORG-123";
        String jsonResponse = """
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_123",
                "identifier": "ORG-123",
                "label": "Test Organisation",
                "label_lg": "en"
            }
            """;

        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject(jsonResponse));

        // When
        repository.getCompactOrganisation(organisationId);

        // Then
        verify(repositoryGestion, times(1)).getResponseAsObject(anyString());
    }

    @Test
    void shouldHandleOrganisationWithEnglishLabel() throws RmesException, OrganisationFetchException {
        // Given
        String organisationId = "ORG-002";
        String jsonResponse = """
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_5678",
                "identifier": "ORG-002",
                "label": "Statistics Department",
                "label_lg": "en"
            }
            """;

        when(repositoryGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject(jsonResponse));

        // When
        CompactOrganisation result = repository.getCompactOrganisation(organisationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.identifier()).isEqualTo("ORG-002");
        assertThat(result.label().value()).isEqualTo("Statistics Department");
        assertThat(result.label().lang().toString()).isEqualTo("EN");
    }

    @Test
    void shouldGetMultipleCompactOrganisations() throws RmesException, OrganisationFetchException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-001", "ORG-002", "ORG-003");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject("""
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_1234",
                "identifier": "ORG-001",
                "label": "Direction des statistiques",
                "label_lg": "fr"
            }
            """));
        jsonArray.put(new JSONObject("""
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_5678",
                "identifier": "ORG-002",
                "label": "Statistics Department",
                "label_lg": "en"
            }
            """));
        jsonArray.put(new JSONObject("""
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_9999",
                "identifier": "ORG-003",
                "label": "Service des données",
                "label_lg": "fr"
            }
            """));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(jsonArray);

        // When
        List<CompactOrganisation> result = repository.getCompactOrganisations(organisationIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).identifier()).isEqualTo("ORG-001");
        assertThat(result.get(0).label().value()).isEqualTo("Direction des statistiques");
        assertThat(result.get(1).identifier()).isEqualTo("ORG-002");
        assertThat(result.get(1).label().value()).isEqualTo("Statistics Department");
        assertThat(result.get(2).identifier()).isEqualTo("ORG-003");
        assertThat(result.get(2).label().value()).isEqualTo("Service des données");

        verify(repositoryGestion).getResponseAsArray(anyString());
    }

    @Test
    void shouldReturnEmptyListWhenNoOrganisationsFound() throws RmesException, OrganisationFetchException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-999");
        JSONArray emptyArray = new JSONArray();

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(emptyArray);

        // When
        List<CompactOrganisation> result = repository.getCompactOrganisations(organisationIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(repositoryGestion).getResponseAsArray(anyString());
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenRepositoryThrowsRmesExceptionForMultiple() throws RmesException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-001", "ORG-002");
        when(repositoryGestion.getResponseAsArray(anyString()))
            .thenThrow(new RmesException(500, "Database error", "Error accessing repository"));

        // When/Then
        assertThatThrownBy(() -> repository.getCompactOrganisations(organisationIds))
            .isInstanceOf(OrganisationFetchException.class);

        verify(repositoryGestion).getResponseAsArray(anyString());
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenJsonParsingFailsForMultiple() throws RmesException {
        // Given
        List<String> organisationIds = Arrays.asList("ORG-001", "ORG-002");
        JSONArray jsonArray = new JSONArray();
        // Valid JSON but missing required fields
        jsonArray.put(new JSONObject("""
            {
                "iri": "http://rdf.insee.fr/def/base#OrganismUnit_1234"
            }
            """));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(jsonArray);

        // When/Then
        assertThatThrownBy(() -> repository.getCompactOrganisations(organisationIds))
            .isInstanceOf(OrganisationFetchException.class);

        verify(repositoryGestion).getResponseAsArray(anyString());
    }

    @Test
    void shouldReturnTrueWhenOrganisationExists() throws RmesException, OrganisationFetchException {
        // Given
        String iri = "http://bauhaus/organisations/insee/HIE2000052";

        when(repositoryGestion.getResponseAsBoolean(anyString())).thenReturn(true);

        // When
        boolean result = repository.checkIfOrganisationExists(iri);

        // Then
        assertThat(result).isTrue();

        verify(repositoryGestion).getResponseAsBoolean(anyString());
    }

    @Test
    void shouldReturnFalseWhenOrganisationDoesNotExist() throws RmesException, OrganisationFetchException {
        // Given
        String iri = "http://bauhaus/organisations/insee/NON_EXISTENT";

        when(repositoryGestion.getResponseAsBoolean(anyString())).thenReturn(false);

        // When
        boolean result = repository.checkIfOrganisationExists(iri);

        // Then
        assertThat(result).isFalse();

        verify(repositoryGestion).getResponseAsBoolean(anyString());
    }

    @Test
    void shouldThrowOrganisationFetchExceptionWhenCheckExistenceFails() throws RmesException {
        // Given
        String iri = "http://bauhaus/organisations/insee/HIE2000052";
        when(repositoryGestion.getResponseAsBoolean(anyString()))
            .thenThrow(new RmesException(500, "Database error", "Error accessing repository"));

        // When/Then
        assertThatThrownBy(() -> repository.checkIfOrganisationExists(iri))
            .isInstanceOf(OrganisationFetchException.class);

        verify(repositoryGestion).getResponseAsBoolean(anyString());
    }
}
