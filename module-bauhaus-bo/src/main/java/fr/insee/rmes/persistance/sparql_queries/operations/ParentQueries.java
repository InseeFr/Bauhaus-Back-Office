package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ParentQueries {

    private final BauhausLanguagesProperties languages;

	public ParentQueries(BauhausLanguagesProperties languages) {
        this.languages = languages;
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		return params;
	}

	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public String checkIfExists(String uri) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.URI, uri);
		return buildRequest("checkIfExistsQuery.ftlh", params);
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("", fileName, params);
	}

}
