package fr.insee.rmes.modules.operation.series.infrastructure.graphdb;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphDbSeriesCreatorsAdapterTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private Config config;

    private GraphDbSeriesCreatorsAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(config.getOperationsGraph()).thenReturn("http://rdf.insee.fr/graphes/operations");
        adapter = new GraphDbSeriesCreatorsAdapter(repositoryGestion, config);
    }

    @Test
    void getCreatorsForSeries_returnsEmptyMap_whenInputIsEmpty() {
        Map<String, List<String>> result = adapter.getCreatorsForSeries(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void getCreatorsForSeries_returnsEmptyMap_whenInputIsNull() {
        Map<String, List<String>> result = adapter.getCreatorsForSeries(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getCreatorsForSeries_returnsCreatorsGroupedByIri() throws RmesException {
        String iri1 = "http://id.insee.fr/operations/serie/s1001";
        String iri2 = "http://id.insee.fr/operations/serie/s1002";

        JSONArray sparqlResult = new JSONArray();
        sparqlResult.put(new JSONObject().put("seriesIri", iri1).put("creators", "stamp-A"));
        sparqlResult.put(new JSONObject().put("seriesIri", iri1).put("creators", "stamp-B"));
        sparqlResult.put(new JSONObject().put("seriesIri", iri2).put("creators", "stamp-C"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(sparqlResult);

        Map<String, List<String>> result = adapter.getCreatorsForSeries(List.of(iri1, iri2));

        assertThat(result).hasSize(2);
        assertThat(result.get(iri1)).containsExactlyInAnyOrder("stamp-A", "stamp-B");
        assertThat(result.get(iri2)).containsExactly("stamp-C");
    }

    @Test
    void getCreatorsForSeries_returnsEmptyMap_whenRepositoryThrows() throws RmesException {
        when(repositoryGestion.getResponseAsArray(anyString())).thenThrow(new RmesException(500, "error", "detail"));

        Map<String, List<String>> result = adapter.getCreatorsForSeries(
                List.of("http://id.insee.fr/operations/serie/s1001"));

        assertThat(result).isEmpty();
    }
}
