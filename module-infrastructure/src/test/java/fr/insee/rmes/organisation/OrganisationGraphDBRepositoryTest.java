package fr.insee.rmes.organisation;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationGraphDBRepositoryTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    private OrganisationGraphDBRepository repository;

    private static final String BASE_GRAPH = "http://rdf.insee.fr/graphes/";
    private static final String INSEE_GRAPH = "organisations/insee";
    private static final String LANGUAGE = "fr";

    @BeforeEach
    void setUp() {
        repository = new OrganisationGraphDBRepository(repositoryGestion, BASE_GRAPH, INSEE_GRAPH, LANGUAGE);
    }

    @Test
    void shouldReturnOrganisationsFromGraphDB() throws RmesException {
        // Given
        JSONArray mockResponse = new JSONArray();
        JSONObject org1 = new JSONObject();
        org1.put("stamp", "DG75-A001");
        org1.put("label", "Direction Générale 75 - Service A001");

        JSONObject org2 = new JSONObject();
        org2.put("stamp", "DR13-DIR");
        org2.put("label", "Direction Régionale 13 - Direction");

        mockResponse.put(org1);
        mockResponse.put(org2);

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        // When
        List<OrganisationOption> result = repository.getOrganisations();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).stamp()).isEqualTo("DG75-A001");
        assertThat(result.get(0).label()).isEqualTo("Direction Générale 75 - Service A001");
        assertThat(result.get(1).stamp()).isEqualTo("DR13-DIR");
        assertThat(result.get(1).label()).isEqualTo("Direction Régionale 13 - Direction");
    }

    @Test
    void shouldReturnEmptyListWhenNoOrganisations() throws RmesException {
        // Given
        JSONArray emptyResponse = new JSONArray();
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(emptyResponse);

        // When
        List<OrganisationOption> result = repository.getOrganisations();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldBuildQueryWithCorrectParameters() throws RmesException {
        // Given
        JSONArray mockResponse = new JSONArray();
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        // When
        repository.getOrganisations();

        // Then
        verify(repositoryGestion).getResponseAsArray(queryCaptor.capture());
        String query = queryCaptor.getValue();

        assertThat(query).contains("http://rdf.insee.fr/graphes/organisations/insee");
        assertThat(query).contains("'fr'");
        assertThat(query).contains("adms:identifier");
        assertThat(query).contains("skos:prefLabel");
    }
}
