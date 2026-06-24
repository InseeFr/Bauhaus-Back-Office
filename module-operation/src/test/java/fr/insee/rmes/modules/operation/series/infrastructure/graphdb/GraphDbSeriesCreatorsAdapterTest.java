package fr.insee.rmes.modules.operation.series.infrastructure.graphdb;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operation.series.infrastructure.PublicationToGestionIriRewriter;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphDbSeriesCreatorsAdapterTest {

    @Mock
    private RepositoryGestion repositoryGestion;


    @Mock
    private GraphsProperties graphs;

    private GraphDbSeriesCreatorsAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(graphs.operationsGraph()).thenReturn("http://rdf.insee.fr/graphes/operations");
        PublicationToGestionIriRewriter rewriter =
                new PublicationToGestionIriRewriter("http://id.insee.fr/", "http://bauhaus/");
        adapter = new GraphDbSeriesCreatorsAdapter(repositoryGestion, graphs, rewriter);
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
    void getCreatorsForSeries_queriesGestionWithTranslatedIrisAndRemapsResultToPublicationIris()
            throws RmesException {
        String publicationIri1 = "http://id.insee.fr/operations/serie/s1001";
        String publicationIri2 = "http://id.insee.fr/operations/serie/s1002";
        String gestionIri1 = "http://bauhaus/operations/serie/s1001";
        String gestionIri2 = "http://bauhaus/operations/serie/s1002";

        // The management repository holds gestion-prefixed subjects, so it returns gestion IRIs.
        JSONArray sparqlResult = new JSONArray();
        sparqlResult.put(new JSONObject().put("seriesIri", gestionIri1).put("creators", "stamp-A"));
        sparqlResult.put(new JSONObject().put("seriesIri", gestionIri1).put("creators", "stamp-B"));
        sparqlResult.put(new JSONObject().put("seriesIri", gestionIri2).put("creators", "stamp-C"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(sparqlResult);

        Map<String, List<String>> result =
                adapter.getCreatorsForSeries(List.of(publicationIri1, publicationIri2));

        // The query must target the gestion base, not the publication base.
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(repositoryGestion).getResponseAsArray(queryCaptor.capture());
        String query = queryCaptor.getValue();
        assertThat(query).contains(gestionIri1).contains(gestionIri2);
        assertThat(query).doesNotContain(publicationIri1).doesNotContain(publicationIri2);

        // The result must be keyed by the publication IRIs the caller passed in.
        assertThat(result).hasSize(2);
        assertThat(result.get(publicationIri1)).containsExactlyInAnyOrder("stamp-A", "stamp-B");
        assertThat(result.get(publicationIri2)).containsExactly("stamp-C");
    }

    @Test
    void getCreatorsForSeries_returnsEmptyMap_whenRepositoryThrows() throws RmesException {
        when(repositoryGestion.getResponseAsArray(anyString())).thenThrow(new RmesException(500, "error", "detail"));

        Map<String, List<String>> result = adapter.getCreatorsForSeries(
                List.of("http://id.insee.fr/operations/serie/s1001"));

        assertThat(result).isEmpty();
    }
}
