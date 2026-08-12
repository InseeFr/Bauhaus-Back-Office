package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClassificationFamiliesQueries {

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public ClassificationFamiliesQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	public String familiesQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("GRAPH", graphs.classifFamiliesGraph());
		params.put("LG1", languages.lg1());
		return buildRequest("getFamilies.ftlh", params);
	}

	public String familyQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("GRAPH", graphs.classifFamiliesGraph());
		params.put("LG1", languages.lg1());
		params.put("ID", id);
		return buildRequest("getFamily.ftlh", params);
	}

	public String familyMembersQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		params.put("ID", id);
		return buildRequest("getFamilyMembers.ftlh", params);
	}

	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("classifications/families/", fileName, params);
	}
}
