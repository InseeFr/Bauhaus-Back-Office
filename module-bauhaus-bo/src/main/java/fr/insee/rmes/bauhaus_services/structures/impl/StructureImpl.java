package fr.insee.rmes.bauhaus_services.structures.impl;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.bauhaus_services.structures.persistence.StructureRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructure;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.IdGenerator;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StructureImpl extends RdfService implements StructureService {

    static final Logger logger = LoggerFactory.getLogger(StructureImpl.class);

    private final StructureRepository structureRepository;

    private final CodeListService codeListService;

    private final StructureQueries structureQueries;

    private final ConceptConceptsQueries conceptConceptsQueries;

    public StructureImpl(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils,
            StructureRepository structureRepository,
            CodeListService codeListService,
            StructureQueries structureQueries,
            ConceptConceptsQueries conceptConceptsQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.structureRepository = structureRepository;
        this.codeListService = codeListService;
        this.structureQueries = structureQueries;
        this.conceptConceptsQueries = conceptConceptsQueries;
    }

    @Override
    public List<PartialStructure> getStructures() throws RmesException {
        logger.info("Starting to get structures");
        var structures = repoGestion.getResponseAsArray(structureQueries.getStructures());
        return DiacriticSorter.sort(structures, PartialStructure[].class, PartialStructure::labelLg1);
    }

    @Override
    public String getStructuresForSearch() throws RmesException {
        logger.info("Starting to get Structures for advanced Search");
        JSONArray structures = repoGestion.getResponseAsArray(structureQueries.getStructures());
        return structureRepository.formatStructuresForSearch(structures).toString();
    }

    @Override
    public String getStructureById(String id) throws RmesException {
        logger.info("Starting to get structure");
        JSONObject structure = repoGestion.getResponseAsObject(structureQueries.getStructureById(id));

        IRI iri = RdfUtils.structureIRI(id);
        this.repoGestion.getMultipleTripletsForObject(
                structure, "contributor", structureQueries.getStructureContributors(iri), "contributor");

        return structureRepository.formatStructure(structure, id).toString();
    }

    private void removeEmptyAttachment(JSONObject cd) {
        if (((JSONArray) cd.get("attachment")).isEmpty()) {
            cd.remove("attachment");
        }
    }

    @Override
    public String getStructureByIdWithDetails(String id) throws RmesException {
        logger.info("Starting to get all details of a structure");
        JSONObject structure = repoGestion.getResponseAsObject(structureQueries.getStructureById(id));
        JSONObject structureWithComponentSpecifications = structureRepository.formatStructure(structure, id);
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
            if (type.equalsIgnoreCase(RdfUtils.toString(QB.ATTRIBUTE_PROPERTY))) {
                component.put("type", "attribute");
            } else if (type.equalsIgnoreCase(RdfUtils.toString(QB.MEASURE_PROPERTY))) {
                component.put("type", "measure");
            } else if (type.equalsIgnoreCase(RdfUtils.toString(QB.DIMENSION_PROPERTY))) {
                component.put("type", "dimension");
            }

            // If the codelist is defined, we have to remove the range property and fetch the codes list
            if (!component.isNull(Constants.CODELIST)) {
                component.remove("range");

                JSONObject codeList = new JSONObject();
                codeList.put(Constants.ID, component.getString(Constants.CODELIST));
                try {
                    codeList.put(
                            "codes",
                            new JSONArray(
                                    this.codeListService.getCodesListByIRI(component.getString(Constants.CODELIST))));
                } catch (RmesException e) {
                    logger.error("Cannot fetch code list of the structure " + id, e);
                }

                component.put(Constants.CODELIST, codeList);
            }

            if (!component.isNull(Constants.CONCEPT)) {
                try {
                    JSONObject concept =
                            repoGestion.getResponseAsObject(conceptConceptsQueries.conceptQueryForDetailStructure(
                                    component.getString(Constants.CONCEPT)));
                    component.put(Constants.CONCEPT, concept);
                } catch (RmesException e) {
                    logger.error("Cannot fetch concept of the structure " + id, e);
                }
            }
        });

        return structureWithComponentSpecifications.toString();
    }

    @Override
    public String publishStructureById(String id) throws RmesException {
        return structureRepository.publishStructure(new JSONObject(this.getStructureById(id)));
    }

    /**
     * Create new Structure
     * @throws RmesException
     */
    @Override
    public String setStructure(String body) throws RmesException {
        return structureRepository.setStructure(body);
    }

    /**
     * Update a Structure
     * @throws RmesException
     */
    @Override
    public String setStructure(String id, String body) throws RmesException {
        return structureRepository.setStructure(id, body);
    }

    @Override
    public void deleteStructure(String structureId) throws RmesException {
        structureRepository.deleteStructure(structureId);
    }
}
