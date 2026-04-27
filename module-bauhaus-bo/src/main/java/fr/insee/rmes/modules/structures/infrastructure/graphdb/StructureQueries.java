package fr.insee.rmes.modules.structures.infrastructure.graphdb;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StructureQueries {

	private static final String URI_STRUCTURE = "URI_STRUCTURE";
	private static final String URI_COMPONENT = "URI_COMPONENT";
	public static final String CODES_LISTS_GRAPH = "CODES_LISTS_GRAPH";

	private final Config config;

	public StructureQueries(Config config) {
		this.config = config;
	}

	public String getStructures() throws RmesException {
		Map<String, Object> params = initParams();
		return buildStructureRequest("getStructures.ftlh", params);
	}

	public String getValidationStatus(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("id", id);
		return buildStructureRequest("getValidationStatus.ftlh", params);
	}

	public String getStructuresAttachments(String structureId, String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("STRUCTURE_ID", structureId);
		params.put("COMPONENT_SPECIFICATION_ID", id);
		return buildStructureRequest("getAttachment.ftlh", params);
	}

	public String getComponentsForStructure(Object id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", id);
		return buildStructureRequest("getComponentsForAStructure.ftlh", params);
	}

	public String getStructureById(String structureId) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", structureId);
		return buildStructureRequest("getStructure.ftlh", params);
	}

	public String checkUnicityMutualizedComponent(String componentId, String conceptUri, String codeListUri, String type) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("COMPONENT_ID", componentId);
		params.put("CONCEPT_URI", INSEE.STRUCTURE_CONCEPT + conceptUri);
		params.put("CODE_LIST_URI", codeListUri);
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("CONCEPT_GRAPH", config.getConceptsGraph());
		params.put("TYPE", type);
		return buildStructureRequest("checkUnicityMutualizedComponent.ftlh", params);
	}

	public String checkUnicityStructure(String structureId, String[] ids) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("NB_COMPONENT", ids.length);
		params.put("IDS", ids);
		params.put("STRUCTURE_ID", structureId);
		return buildStructureRequest("checkUnicityStructure.ftlh", params);
	}

	public String getComponents(boolean attributes, boolean dimensions, boolean measures) throws RmesException {
		Map<String, Object> params = initParams();
		List<String> types = new ArrayList<>();
		if (attributes) {
			types.add("qb:AttributeProperty");
		}
		if (dimensions) {
			types.add("qb:DimensionProperty");
		}
		if (measures) {
			types.add("qb:MeasureProperty");
		}
		params.put("TYPES", String.join(",", types));
		return buildStructureRequest("getMutualizedComponents.ftlh", params);
	}

	public String getComponent(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", id);
		return buildStructureRequest("getMutualizedComponent.ftlh", params);
	}

	public String getStructuresForComponent(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", id);
		return buildStructureRequest("getStructuresForMutualizedComponent.ftlh", params);
	}

	public String getComponentType(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", id);
		return buildStructureRequest("getComponentType.ftlh", params);
	}

	public String lastId(String namespaceSuffix, String type) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("NAMESPACE", namespaceSuffix);
		params.put("TYPE", type);
		return buildStructureRequest("getLastIdByType.ftlh", params);
	}

	public String lastStructureId() throws RmesException {
		Map<String, Object> params = initParams();
		return buildStructureRequest("getLastIdStructure.ftlh", params);
	}

	public String getUnValidatedComponent(String structureById) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", structureById);
		return buildStructureRequest("getUnValidatedComponent.ftlh", params);
	}

	public String getUriClasseOwl(String codeList) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("CODES_LIST", codeList);
		return buildStructureRequest("getUriClasseOwl.ftlh", params);
	}

	public String getContributorsByStructureUri(String uriStructure) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_STRUCTURE, uriStructure);
		return buildStructureRequest("getStructureContributorsByUriQuery.ftlh", params);
	}

	public String getContributorsByComponentUri(String uriComponent) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_COMPONENT, uriComponent);
		return buildStructureRequest("getComponentContributorsByUriQuery.ftlh", params);
	}

	public String getStructureContributors(IRI iri) throws RmesException {
		Map<String, Object> params = Map.of("GRAPH", config.getStructuresGraph(), "IRI", iri, "PREDICATE", "dc:contributor");
		return FreeMarkerUtils.buildRequest("common/", "getContributors.ftlh", params);
	}

	public String getComponentContributors(String iri) throws RmesException {
		Map<String, Object> params = Map.of("GRAPH", config.getStructuresComponentsGraph(), "IRI", iri, "PREDICATE", "dc:contributor");
		return FreeMarkerUtils.buildRequest("common/", "getContributors.ftlh", params);
	}

	private String buildStructureRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("structures/", fileName, params);
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("STRUCTURES_COMPONENTS_GRAPH", config.getStructuresComponentsGraph());
		params.put("STRUCTURES_GRAPH", config.getStructuresGraph());
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}
}