package fr.insee.rmes.modules.geographies.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GeographyQueries {

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public GeographyQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	public String getGeoUriIfExists(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		return buildRequest("getGeoUriIfExists.ftlh", params);
	}

	public String getFeaturesQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put("uriFeature", "");
		return buildRequest("getGeoFeatures.ftlh", params);
	}

	public String getUnionsForAFeatureQuery(String uriFeature) throws RmesException {
		return getUnionOrDifferenceForFeature(uriFeature, true);
	}

	public String getDifferenceForAFeatureQuery(String uriFeature) throws RmesException {
		return getUnionOrDifferenceForFeature(uriFeature, false);
	}

	public String getFeatureQuery(String uri) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("uriFeature", uri);
		return buildRequest("getGeoFeatures.ftlh", params);
	}

	private String getUnionOrDifferenceForFeature(String uriFeature, boolean getUnion) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.URI, uriFeature);
		params.put("union", getUnion);
		return buildRequest("getUnionOrDifferenceForUri.ftlh", params);
	}

	public String checkUnicityTerritory(String labelLg1) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("LABEL", labelLg1);
		return buildRequest("checkUnicityTerritory.ftlh", params);
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		params.put("COG_GRAPH", graphs.geographyGraph());
		params.put("GEO_SIMS_GRAPH", graphs.documentationsGeoGraph());
		return params;
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("geography/", fileName, params);
	}
}
