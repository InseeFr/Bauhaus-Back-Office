package fr.insee.rmes.bauhaus_services.structures.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;

@Service
public class StructureImpl  extends RdfService implements StructureService {
	
	static final Logger logger = LogManager.getLogger(StructureImpl.class);
	
	@Autowired
	StructureUtils structureUtils;

	@Autowired
	CodeListService codeListService;

	@Override
	public String getStructures() throws RmesException {
		logger.info("Starting to get structures");
		return repoGestion.getResponseAsArray(StructureQueries.getStructures()).toString();
	}

	@Override
	public String getStructuresForSearch() throws RmesException {
		logger.info("Starting to get Structures for advanced Search");
		JSONArray structures = repoGestion.getResponseAsArray(StructureQueries.getStructures());
		return structureUtils.formatStructuresForSearch(structures).toString();
	}
	
	@Override
	public String getStructureById(String id) throws RmesException {
		logger.info("Starting to get structure");
		JSONObject structure = repoGestion.getResponseAsObject(StructureQueries.getStructureById(id));
		return structureUtils.formatStructure(structure, id).toString();
	}

	private void removeEmptyAttachment(JSONObject cd){
		if(((JSONArray) cd.get("attachment")).length() == 0){
			cd.remove("attachment");
		}
	}
	@Override
	public String getStructureByIdWithDetails(String id) throws RmesException {
		logger.info("Starting to get all details of a structure");
		JSONObject structure = repoGestion.getResponseAsObject(StructureQueries.getStructureById(id));
		JSONObject structureWithComponentSpecifications = structureUtils.formatStructure(structure, id);
		JSONArray componentDefinitions = (JSONArray) structureWithComponentSpecifications.get("componentDefinitions");
		componentDefinitions.forEach(o -> {
			JSONObject cd = (JSONObject) o;
			removeEmptyAttachment((JSONObject) o);
			cd.remove("id");
			cd.remove("created");
			cd.remove("modified");

			JSONObject component = (JSONObject) cd.get("component");

			// We first have to rename the type property
			String type = (String) component.get("type");
			if(type.equalsIgnoreCase(QB.ATTRIBUTE_PROPERTY.toString())){
				component.put("type", "attribute");
			}
			if(type.equalsIgnoreCase(QB.MEASURE_PROPERTY.toString())){
				component.put("type", "measure");
			}
			if(type.equalsIgnoreCase(QB.DIMENSION_PROPERTY.toString())){
				component.put("type", "dimension");
			}

			// If the codelist is defined, we have to remove the range property and fetch the codes list
			if(!component.isNull("codeList")){
				component.remove("range");

				JSONObject codeList = new JSONObject();
				codeList.put("id", component.getString("codeList"));
				try {
					codeList.put("codes", new JSONArray(this.codeListService.geCodesListByIRI(component.getString("codeList"))));
				} catch (RmesException e) {
					logger.error("Cannot fetch code list of the structure " + id);
					logger.error(e);
				}

				component.put("codeList", codeList);
			}

			if(!component.isNull("concept")){
				try {
					JSONObject concept = repoGestion.getResponseAsObject(ConceptsQueries.conceptQueryForDetailStructure(component.getString("concept")));
					component.put("concept", concept);
				} catch (RmesException e) {
					logger.error("Cannot fetch concept of the structure " + id);
					logger.error(e);
				}

			}

		});

		return structureWithComponentSpecifications.toString();
	}

	/**
	 * Create new Structure
	 * @throws RmesException 
	 */
	@Override
	public String setStructure(String body) throws RmesException {
		return structureUtils.setStructure(body);
	}
	
	/**
	 * Update a Structure
	 * @throws RmesException 
	 */
	@Override
	public String setStructure(String id, String body) throws RmesException {
		return structureUtils.setStructure(id, body);
	}

	@Override
	public void deleteStructure(String structureId) throws RmesException {
		structureUtils.deleteStructure(structureId);
	}


}
