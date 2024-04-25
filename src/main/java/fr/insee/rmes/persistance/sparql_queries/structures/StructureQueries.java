package fr.insee.rmes.persistance.sparql_queries.structures;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class StructureQueries extends GenericQueries{
	private static final String URI_STRUCTURE = "URI_STRUCTURE";
	private static final String URI_COMPONENT = "URI_COMPONENT";
	public static final String CODES_LISTS_GRAPH = "CODES_LISTS_GRAPH";

	public static String 	getStructures() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		return buildStructureRequest("getStructures.ftlh", params);
	}

	public static String getValidationStatus(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("id", id);
		return buildStructureRequest("getValidationStatus.ftlh", params);
	}
	public static String getStructuresAttachments(String structureId, String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("STRUCTURE_ID", structureId);
		params.put("COMPONENT_SPECIFICATION_ID", id);
		return buildStructureRequest("getAttachment.ftlh", params);
	}

	public static String getComponentsForStructure(Object id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", id);
		return buildStructureRequest("getComponentsForAStructure.ftlh", params);
	}

	public static String getStructureById(String structureId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", structureId);
		return buildStructureRequest("getStructure.ftlh", params);
	}

	public static String checkUnicityMutualizedComponent(String componentId, String conceptUri, String codeListUri, String type) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("COMPONENT_ID", componentId);
		params.put("CONCEPT_URI", INSEE.STRUCTURE_CONCEPT + conceptUri);
		params.put("CODE_LIST_URI", codeListUri);
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("CONCEPT_GRAPH", config.getConceptsGraph());
		params.put("TYPE", type);
		return buildStructureRequest("checkUnicityMutualizedComponent.ftlh", params);
	}
	public static String checkUnicityStructure(String structureId, String[] ids) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("NB_COMPONENT", ids.length);
		params.put("IDS", ids);
		params.put("STRUCTURE_ID", structureId);

		return buildStructureRequest("checkUnicityStructure.ftlh", params);
	}
	
	public static String getComponents(boolean attributes, boolean dimensions, boolean measures) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		List<String> types = new ArrayList<>();
		if(attributes){
			types.add("qb:AttributeProperty");
		}
		if(dimensions){
			types.add("qb:DimensionProperty");
		}
		if(measures){
			types.add("qb:MeasureProperty");
		}
		params.put("TYPES", String.join(",", types));

		return buildStructureRequest("getMutualizedComponents.ftlh", params);
	}

	public static String getComponent(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", id);
		return buildStructureRequest("getMutualizedComponent.ftlh", params);
	}

	public static String getStructuresForComponent(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", id);
		return buildStructureRequest("getStructuresForMutualizedComponent.ftlh", params);
	}

	public static String getComponentType(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", id);
		return buildStructureRequest("getComponentType.ftlh", params);
	}

	public static String lastId(String namespaceSuffix, String type) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("NAMESPACE", namespaceSuffix);
		params.put("TYPE", type);

		return buildStructureRequest("getLastIdByType.ftlh", params);
	}
	public static String lastStructureId() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);

		return buildStructureRequest("getLastIdStructure.ftlh", params);
	}

	public static String getUnValidatedComponent(String structureById) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", structureById);
		return buildStructureRequest("getUnValidatedComponent.ftlh", params);
	}

	public static String getUriClasseOwl(String codeList) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("CODES_LIST", codeList);

		return buildStructureRequest("getUriClasseOwl.ftlh", params);
	}

	private static String buildStructureRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("structures/", fileName, params);
	}


	private static void initParams(HashMap<String, Object> params) {
		params.put("STRUCTURES_COMPONENTS_GRAPH", config.getStructuresComponentsGraph());
		params.put("STRUCTURES_GRAPH", config.getStructuresGraph());
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
	}
	
	  private StructureQueries() {
		    throw new IllegalStateException("Utility class");
	}

	public static String getContributorsByStructureUri(String uriStructure) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(URI_STRUCTURE, uriStructure);
		return buildStructureRequest("getStructureContributorsByUriQuery.ftlh", params);
	}

	public static String getContributorsByComponentUri(String uriComponent) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(URI_COMPONENT, uriComponent);
		return buildStructureRequest("getComponentContributorsByUriQuery.ftlh", params);
	}

}