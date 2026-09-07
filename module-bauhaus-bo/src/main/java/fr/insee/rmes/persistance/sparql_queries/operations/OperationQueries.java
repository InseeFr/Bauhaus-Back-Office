package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperationQueries {

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public OperationQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	public String lastId() throws RmesException {
		Map<String, Object> params = initParams();
		return buildOperationRequest("getLastIdQuery.ftlh", params);
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put("OPERATIONS_GRAPH", SparqlLiterals.iri(graphs.operationsGraph()));
		return params;
	}

	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public String checkIfFamOpeSerExists(String uri) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.URI, SparqlLiterals.iri(uri));
		return buildOperationRequest("checkIfFamSerOpeExistsQuery.ftlh", params);
	}

	public String getPublicationState(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.ID, SparqlLiterals.literal(id));
		return buildOperationRequest("getPublicationStatusQuery.ftlh", params);
	}

	public String setPublicationState(IRI familyURI, String newState) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("famopeserURI", SparqlLiterals.iri(familyURI.stringValue()));
		params.put("newState", SparqlLiterals.iri(newState));
		return buildOperationRequest("changePublicationStatusQuery.ftlh", params);
	}

	private String buildOperationRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
	}

}