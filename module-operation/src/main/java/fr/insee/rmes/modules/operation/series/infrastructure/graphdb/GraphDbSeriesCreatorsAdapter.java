package fr.insee.rmes.modules.operation.series.infrastructure.graphdb;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import fr.insee.rmes.modules.operation.series.infrastructure.PublicationToGestionIriRewriter;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class GraphDbSeriesCreatorsAdapter implements SeriesCreatorsPort {

    private static final Logger logger = LoggerFactory.getLogger(GraphDbSeriesCreatorsAdapter.class);

    private final RepositoryGestion repositoryGestion;
    private final GraphsProperties graphs;
    private final PublicationToGestionIriRewriter iriRewriter;

    public GraphDbSeriesCreatorsAdapter(
            RepositoryGestion repositoryGestion, GraphsProperties graphs, PublicationToGestionIriRewriter iriRewriter) {
        this.repositoryGestion = repositoryGestion;
        this.graphs = graphs;
        this.iriRewriter = iriRewriter;
    }

    @Override
    public Map<String, List<String>> getCreatorsForSeries(Collection<String> seriesIris) {
        if (seriesIris == null || seriesIris.isEmpty()) {
            return Map.of();
        }
        try {
            // The Group carries publication IRIs, but the management repository is keyed by gestion
            // IRIs. Translate before querying and remember how to map each result back.
            Map<String, String> publicationByGestionIri = new HashMap<>();
            for (String publicationIri : seriesIris) {
                publicationByGestionIri.put(iriRewriter.toGestion(publicationIri), publicationIri);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("OPERATIONS_GRAPH", SparqlLiterals.iri(graphs.operationsGraph()));
            params.put(
                    "SERIES_IRIS",
                    publicationByGestionIri.keySet().stream()
                            .map(SparqlLiterals::iri)
                            .toList());
            String query = FreeMarkerUtils.buildRequest("operations/series/", "getSeriesCreatorsForIris.ftlh", params);

            JSONArray results = repositoryGestion.getResponseAsArray(query);
            Map<String, List<String>> creatorsByIri = new HashMap<>();
            JSONUtils.stream(results).forEach(row -> {
                String gestionIri = row.optString("seriesIri", null);
                String creator = row.optString("creators", null);
                if (gestionIri != null && !gestionIri.isBlank() && creator != null && !creator.isBlank()) {
                    String publicationIri = publicationByGestionIri.getOrDefault(gestionIri, gestionIri);
                    creatorsByIri
                            .computeIfAbsent(publicationIri, k -> new ArrayList<>())
                            .add(creator);
                }
            });
            return creatorsByIri;
        } catch (RmesException | IllegalArgumentException e) {
            // IllegalArgumentException : une des IRI venant du Group n'est pas injectable telle quelle.
            logger.error("Error fetching creators for series batch", e);
            return Map.of();
        }
    }
}
