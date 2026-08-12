package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClassificationSeriesQueries {

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public ClassificationSeriesQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	public String seriesQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("GRAPH", graphs.classifFamiliesGraph());
		params.put("LG1", languages.lg1());
		return buildRequest("getSeries.ftlh", params);
	}

	public String oneSeriesQuery(String id) throws RmesException {
		return buildRequest("getOneSeries.ftlh", seriesParams(id));
	}

	public String seriesMembersQuery(String id) throws RmesException {
		return buildRequest("getSeriesMembers.ftlh", seriesParams(id));
	}

	private Map<String, Object> seriesParams(String id) {
		Map<String, Object> params = new HashMap<>();
		params.put("GRAPH", graphs.classifFamiliesGraph());
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		params.put("ID", id);
		return params;
	}

	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("classifications/series/", fileName, params);
	}
}