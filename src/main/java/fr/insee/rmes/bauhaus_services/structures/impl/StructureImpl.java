package fr.insee.rmes.bauhaus_services.structures.impl;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.PartialStructure;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.DiacriticSorter;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructureImpl  extends RdfService implements StructureService {
	
	static final Logger logger = LoggerFactory.getLogger(StructureImpl.class);
	
	@Autowired
	StructureUtils structureUtils;

	@Autowired
	CodeListService codeListService;

	@Override
	public List<PartialStructure> getStructures() throws RmesException {
		logger.info("Starting to get structures");
		var structures = repoGestion.getResponseAsArray(StructureQueries.getStructures());
		return DiacriticSorter.sort(structures,
				PartialStructure[].class,
				PartialStructure::labelLg1);

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

		IRI iri = RdfUtils.structureIRI(id);
		getMultipleTripletsForObject(structure, "contributor", StructureQueries.getStructureContributors(iri), "contributor");

		return structureUtils.formatStructure(structure, id).toString();
	}

	private void removeEmptyAttachment(JSONObject cd){
		if(((JSONArray) cd.get("attachment")).isEmpty()){
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
			cd.remove(Constants.ID);
			cd.remove("created");
			cd.remove("modified");

			JSONObject component = (JSONObject) cd.get("component");

			// We first have to rename the type property
			String type = (String) component.get("type");
			if(type.equalsIgnoreCase(RdfUtils.toString(QB.ATTRIBUTE_PROPERTY))){
				component.put("type", "attribute");
			}
			else if(type.equalsIgnoreCase(RdfUtils.toString(QB.MEASURE_PROPERTY))){
				component.put("type", "measure");
			}
			else if(type.equalsIgnoreCase(RdfUtils.toString(QB.DIMENSION_PROPERTY))){
				component.put("type", "dimension");
			}

			// If the codelist is defined, we have to remove the range property and fetch the codes list
			if(!component.isNull(Constants.CODELIST)){
				component.remove("range");

				JSONObject codeList = new JSONObject();
				codeList.put(Constants.ID, component.getString(Constants.CODELIST));
				try {
					codeList.put("codes", new JSONArray(this.codeListService.getCodesListByIRI(component.getString(Constants.CODELIST))));
				} catch (RmesException e) {
					logger.error("Cannot fetch code list of the structure "+id, e);
				}

				component.put(Constants.CODELIST, codeList);
			}

			if(!component.isNull(Constants.CONCEPT)){
				try {
					JSONObject concept = repoGestion.getResponseAsObject(ConceptsQueries.conceptQueryForDetailStructure(component.getString(Constants.CONCEPT)));
					component.put(Constants.CONCEPT, concept);
				} catch (RmesException e) {
					logger.error("Cannot fetch concept of the structure " +id, e);
				}

			}

		});

		return structureWithComponentSpecifications.toString();
	}

	@Override
	public String publishStructureById(String id) throws RmesException {
		return structureUtils.publishStructure(new JSONObject(this.getStructureById(id)));
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
