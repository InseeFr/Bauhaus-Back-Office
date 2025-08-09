package fr.insee.rmes.infrastructure.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ConceptController {
    final ConceptsService conceptsService;
    final DatasetService datasetService;
    final StructureService structureService;

    public ConceptController(ConceptsService conceptsService, DatasetService datasetService, StructureService structureService) {
        this.conceptsService = conceptsService;
        this.datasetService = datasetService;
        this.structureService = structureService;
    }

    @QueryMapping
    public Concept conceptById(@Argument String id) throws RmesException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Support pour LocalDateTime
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Lecture depuis un fichier JSON
        Concept concept = mapper.readValue(conceptsService.getConceptByID(id), Concept.class);


        return concept;
    }

    @QueryMapping
    public Dataset datasetById(@Argument String id) throws RmesException, JsonProcessingException {
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
