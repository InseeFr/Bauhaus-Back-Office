package fr.insee.rmes.modules.operation.series.infrastructure.graphdb;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GraphDbSeriesCreatorsAdapter implements SeriesCreatorsPort {

    private static final Logger logger = LoggerFactory.getLogger(GraphDbSeriesCreatorsAdapter.class);

    private final RepositoryGestion repositoryGestion;
    private final Config config;

    public GraphDbSeriesCreatorsAdapter(RepositoryGestion repositoryGestion, Config config) {
        this.repositoryGestion = repositoryGestion;
        this.config = config;
    }

    @Override
    public Map<String, List<String>> getCreatorsForSeries(Collection<String> seriesIris) {
        if (seriesIris == null || seriesIris.isEmpty()) {
            return Map.of();
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("OPERATIONS_GRAPH", config.getOperationsGraph());
            params.put("SERIES_IRIS", seriesIris);
            String query = FreeMarkerUtils.buildRequest("operations/series/", "getSeriesCreatorsForIris.ftlh", params);

            JSONArray results = repositoryGestion.getResponseAsArray(query);
            Map<String, List<String>> creatorsByIri = new HashMap<>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject row = results.getJSONObject(i);
                String seriesIri = row.optString("seriesIri", null);
                String creator = row.optString("creators", null);
                if (seriesIri != null && !seriesIri.isBlank() && creator != null && !creator.isBlank()) {
                    creatorsByIri.computeIfAbsent(seriesIri, k -> new ArrayList<>()).add(creator);
                }
            }
            return creatorsByIri;
        } catch (RmesException e) {
            logger.error("Error fetching creators for series batch", e);
            return Map.of();
        }
    }
}
