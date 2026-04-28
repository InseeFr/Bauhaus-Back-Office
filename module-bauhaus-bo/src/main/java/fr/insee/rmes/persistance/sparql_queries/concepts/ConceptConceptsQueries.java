package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptConceptsQueries {

	private static final String URI_CONCEPT = "uriConcept";
	public static final String CONCEPTS_GRAPH = "CONCEPTS_GRAPH";

	private final Config config;

	public ConceptConceptsQueries(Config config) {
		this.config = config;
	}

	public String lastConceptID() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getLastConceptId.ftlh", params);
	}

	public String conceptsQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConcepts.ftlh", params);
	}

	public String conceptsSearchQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptsForAdvancedSearch.ftlh", params);
	}

	public String conceptsToValidateQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptsToValidateQuery.ftlh", params);
	}

	public String conceptQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("conceptQuery.ftlh", params);
	}

	public String conceptQueryForDetailStructure(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("conceptQueryForDetailStructure.ftlh", params);
	}

	public String altLabel(String id, String lang) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG", lang);
		params.put("ID", id);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("altLabel.ftlh", params);
	}

	public String conceptNotesQuery(String id, int conceptVersion) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put("CONCEPT_VERSION", conceptVersion);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("conceptNotesQuery.ftlh", params);
	}

	public String conceptLinks(String idConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID_CONCEPT", idConcept);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptLinksById.ftlh", params);
	}

	public String getNarrowers(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return buildConceptRequest("getNarrowers.ftlh", params);
	}

	public String hasBroader(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return buildConceptRequest("hasBroader.ftlh", params);
	}

	public String getGraphWithConceptQuery(String uriConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getGraphWithConceptQuery.ftlh", params);
	}

	public String getRelatedConceptsQuery(String uriConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getLinkedConceptsQuery.ftlh", params);
	}

	public String deleteConcept(String uriConcept, String uriGraph) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		params.put("uriGraph", uriGraph);
		return buildConceptRequest("deleteConceptAndNotesQuery.ftlh", params);
	}

	public String isConceptValidated(String conceptId) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		params.put("ID", conceptId);
		return buildConceptRequest("isConceptValidated.ftlh", params);
	}

	public String checkIfExists(String id) {
		return "ASK \n"
				+ "WHERE  \n"
				+ "{ ?uri ?b ?c .\n "
				+ "FILTER(STRENDS(STR(?uri),'/concepts/definition/" + id + "')) . }";
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}

	private String buildConceptRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("concepts/", fileName, params);
	}
}
