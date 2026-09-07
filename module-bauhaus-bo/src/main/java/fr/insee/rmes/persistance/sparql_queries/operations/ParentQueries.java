package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ParentQueries {

	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public String checkIfExists(String uri) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(Constants.URI, SparqlLiterals.iri(uri));
		return buildRequest("checkIfExistsQuery.ftlh", params);
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("", fileName, params);
	}

}
