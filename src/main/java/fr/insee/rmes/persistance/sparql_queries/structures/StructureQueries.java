package fr.insee.rmes.persistance.sparql_queries.structures;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructureQueries extends GenericQueries{

	public static String 	getStructures() throws RmesException {
		HashMap<String, Object> params = initParams();
		return buildRequest("getStructures.ftlh", params);
	}

	public static String getValidationStatus(String id) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("id", id);
		return buildRequest("getValidationStatus.ftlh", params);
	}
	public static String getStructuresAttachments(String structureId, String id) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("STRUCTURE_ID", structureId);
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

	public static String checkUnicityMutualizedComponent(String componentId, String conceptUri, String codeListUri, String type) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("COMPONENT_ID", componentId);
		params.put("CONCEPT_URI", INSEE.STRUCTURE_CONCEPT + conceptUri);
		params.put("CODE_LIST_URI", codeListUri);
		params.put("CODES_LISTS_GRAPH", config.getCodeListGraph());
		params.put("CONCEPT_GRAPH", config.getConceptsGraph());
		params.put("TYPE", type);
		return buildRequest("checkUnicityMutualizedComponent.ftlh", params);
	}
	public static String checkUnicityStructure(String structureId, String[] ids) throws RmesException {
		HashMap<String, Object> params = initParams();

		params.put("NB_COMPONENT", ids.length);
		params.put("IDS", ids);
		params.put("STRUCTURE_ID", structureId);

		return buildRequest("checkUnicityStructure.ftlh", params);
	}
	
	public static String getComponents(boolean attributes, boolean dimensions, boolean measures) throws RmesException {
		HashMap<String, Object> params = initParams();

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
	public static String lastStructureId() throws RmesException {
		HashMap<String, Object> params = initParams();

		return buildRequest("getLastIdStructure.ftlh", params);
	}

	public static String getUnValidatedComponent(String structureById) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("ID", structureById);
		return buildRequest("getUnValidatedComponent.ftlh", params);
	}

	public static String getUriClasseOwl(String codeList) throws RmesException {
		HashMap<String, Object> params = initParams();
		params.put("CODES_LISTS_GRAPH", config.getCodeListGraph());
		params.put("CODES_LIST", codeList);

		return buildRequest("getUriClasseOwl.ftlh", params);
	}

	private static String buildRequest(String fileName, HashMap<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("structures/", fileName, params);
	}

	private static HashMap<String, Object> initParams() {
		HashMap<String, Object> params = new HashMap<>();
		params.put("STRUCTURES_COMPONENTS_GRAPH", config.getStructuresComponentsGraph());
		params.put("STRUCTURES_GRAPH", config.getStructuresGraph());
		params.put("CODES_LISTS_GRAPH", config.getCodeListGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}
	
	  private StructureQueries() {
		    throw new IllegalStateException("Utility class");
	}

}