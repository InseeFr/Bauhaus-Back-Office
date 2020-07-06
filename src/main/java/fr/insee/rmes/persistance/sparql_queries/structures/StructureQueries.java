package fr.insee.rmes.persistance.sparql_queries.structures;

import java.util.HashMap;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class StructureQueries {

	public static String getStructures() throws RmesException {
		HashMap<String, Object> params = initParams();
		return buildRequest("getStructures.ftlh", params);
	}

	public static String getStructuresAttachments(String id) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("COMPONENT_SPECIFICATION_ID", id);
		return buildRequest("getAttachment.ftlh", params);
	}

	public static String getComponentsForStructure(Object id) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("ID", id);
		return buildRequest("getComponentsForAStructure.ftlh", params);
	}

	public static String getStructureById(String structureId) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("ID", structureId);
		return buildRequest("getStructure.ftlh", params);
	}
	
	public static String getComponentsForSearch() throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("SEARCH", true);
		return buildRequest("getMutualizedComponents.ftlh", params);
	}

	public static String getComponents() throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("SEARCH", false);
		return buildRequest("getMutualizedComponents.ftlh", params);
	}

	public static String getComponent(String id) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("ID", id);
		return buildRequest("getMutualizedComponent.ftlh", params);
	}

	public static String getStructuresForComponent(String id) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("ID", id);
		return buildRequest("getStructuresForMutualizedComponent.ftlh", params);
	}

	public static String lastId(String namespaceSuffix, String type) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("NAMESPACE", namespaceSuffix);
		params.put("TYPE", type);

		return buildRequest("getLastIdByType.ftlh", params);
	}
	public static String lastIdForComponentDefinition() throws RmesException {
		HashMap<String, Object> params = initParams();
		return buildRequest("getLastIdByComponentDefinition.ftlh", params);
	}

	private static String buildRequest(String fileName, HashMap<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("structures/", fileName, params);
	}

	private static HashMap<String, Object> initParams() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("STRUCTURES_COMPONENTS_GRAPH", Config.STRUCTURES_COMPONENTS_GRAPH);
		params.put("STRUCTURES_GRAPH", Config.STRUCTURES_GRAPH);
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);

		return params;
	}
}