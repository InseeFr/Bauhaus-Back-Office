package fr.insee.rmes.bauhaus_services.structures.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;

@Service
public class StructureImpl  extends RdfService implements StructureService {
	
	static final Logger logger = LogManager.getLogger(StructureImpl.class);
	
	@Autowired
	StructureUtils structureUtils;
	
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

	@Override
	public String getStructureByIdWithDetails(String id) throws RmesException {
		logger.info("Starting to get all details of a structure");
		return this.getStructureById(id);
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
