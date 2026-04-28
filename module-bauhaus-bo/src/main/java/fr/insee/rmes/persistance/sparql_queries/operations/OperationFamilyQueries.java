package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component("legacyOperationFamilyQueries")
public class OperationFamilyQueries {

	private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";

	private final Config config;

	public OperationFamilyQueries(Config config) {
		this.config = config;
	}

	public String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "/operations/famille/");
		params.put("TYPE", "insee:StatisticalOperationFamily");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

}