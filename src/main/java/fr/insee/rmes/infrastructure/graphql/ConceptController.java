package fr.insee.rmes.infrastructure.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.JSONUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ConceptController {
    final ConceptsService conceptsService;
    final DatasetService datasetService;
    final StructureService structureService;
    final RepositoryGestion repositoryGestion;
    final RepositoryPublication repositoryPublication;

    @Value("${fr.insee.rmes.bauhaus.lg1}")
    private String lg1;

    @Value("${fr.insee.rmes.bauhaus.lg2}")
    private String lg2;

    public ConceptController(ConceptsService conceptsService, DatasetService datasetService, StructureService structureService, RepositoryGestion repositoryGestion, RepositoryPublication repositoryPublication) {
        this.conceptsService = conceptsService;
        this.datasetService = datasetService;
        this.structureService = structureService;
        this.repositoryGestion = repositoryGestion;
        this.repositoryPublication = repositoryPublication;
    }

    @QueryMapping
    public Concept conceptById(@Argument String id) throws RmesException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Concept concept = mapper.readValue(repositoryGestion.getResponseAsObject(ConceptsQueries.conceptQuery(id)).toString(), Concept.class);

        return concept;
    }

    @SchemaMapping
    public String[] altLabelLg1(Concept concept) throws JsonProcessingException, RmesException {
        ObjectMapper mapper = new ObjectMapper();

        JSONArray altLabelLg1JsonArray = repositoryGestion.getResponseAsArray(ConceptsQueries.altLabel(concept.id(), lg1));
        if(!altLabelLg1JsonArray.isEmpty()) {
            return mapper.readValue(JSONUtils.extractFieldToArray(altLabelLg1JsonArray, "altLabel").toString(), String[].class);
        }

        return new String[0];
    }

    @SchemaMapping
    public String[] altLabelLg2(Concept concept) throws JsonProcessingException, RmesException {
        ObjectMapper mapper = new ObjectMapper();

        JSONArray altLabelLg1JsonArray = repositoryGestion.getResponseAsArray(ConceptsQueries.altLabel(concept.id(), lg2));
        if(!altLabelLg1JsonArray.isEmpty()) {
            return mapper.readValue(JSONUtils.extractFieldToArray(altLabelLg1JsonArray, "altLabel").toString(), String[].class);
        }

        return new String[0];
    }

    @QueryMapping
    public Dataset datasetById(@Argument String id) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Dataset dataset = datasetService.getDatasetByID(id);


        return dataset;
    }

    @SchemaMapping
    public Structure dataStructure(Dataset dataset) throws RmesException, JsonProcessingException {
        var fullUrl = dataset.getDataStructure();
        var id = fullUrl.substring(fullUrl.lastIndexOf("/") + 1);
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Structure structure = mapper.readValue(structureService.getStructureById(id), Structure.class);


        return structure;
    }

}
