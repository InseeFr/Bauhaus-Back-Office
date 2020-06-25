package fr.insee.rmes.bauhaus_services.structures.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.ComponentDefinition;
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.DateUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StructureUtils extends RdfService {

    static final Logger logger = LogManager.getLogger(StructureUtils.class);

    @Autowired
    StructureComponentUtils structureComponentUtils;

    public JSONArray formatStructuresForSearch(JSONArray structures) throws RmesException {
        for (int i = 0; i < structures.length(); i++) {
            JSONObject current = structures.getJSONObject(i);
            current.put("components", repoGestion.getResponseAsArray(StructureQueries.getComponentsForStructure(current.get("id"))));
        }
        return structures;
    }

    public JSONObject formatStructure(JSONObject structure, String id) throws RmesException {
        structure.put("id", id);


        JSONArray componentDefinitions = new JSONArray();

        JSONArray componentDefinitionsFlat = repoGestion.getResponseAsArray(StructureQueries.getComponentsForStructure(id));


        for (int i = 0; i < componentDefinitionsFlat.length(); i++) {
            JSONObject componentDefinitionFlat = componentDefinitionsFlat.getJSONObject(i);
            JSONArray attachmentsArray = repoGestion.getResponseAsArray(StructureQueries.getStructuresAttachments(componentDefinitionFlat.getString("componentDefinitionId")));

            List<String> attachments = new ArrayList<String>();
            for(int j = 0; j < attachmentsArray.length(); j++){
                attachments.add(attachmentsArray.getJSONObject(j).getString("attachment"));
            }


            JSONObject componentDefinition= new JSONObject();

             componentDefinition.put("attachment", attachments);


            if(componentDefinitionFlat.has("required")){
                componentDefinition.put("required", Boolean.parseBoolean(componentDefinitionFlat.getString("required")));
            }


            if(componentDefinitionFlat.has("order")){
                componentDefinition.put("order", componentDefinitionFlat.getString("order"));
            }
            if(componentDefinitionFlat.has("componentDefinitionCreated")){
                componentDefinition.put("created", componentDefinitionFlat.getString("componentDefinitionCreated"));

            }
            if(componentDefinitionFlat.has("componentDefinitionModified")){
                componentDefinition.put("modified", componentDefinitionFlat.getString("componentDefinitionModified"));

            }
            if(componentDefinitionFlat.has("componentDefinitionId")){
                componentDefinition.put("id", componentDefinitionFlat.getString("componentDefinitionId"));

            }
            componentDefinitionFlat.remove("required");
            componentDefinitionFlat.remove("order");
            componentDefinitionFlat.remove("componentDefinitionCreated");
            componentDefinitionFlat.remove("componentDefinitionModified");
            componentDefinitionFlat.remove("componentDefinitionId");
            componentDefinition.put("component", componentDefinitionFlat);

            componentDefinitions.put(componentDefinition);
        }
        structure.put("componentDefinitions", componentDefinitions);
        return structure;
    }

    public String setStructure(String body) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Structure structure = null;
        try {
            structure = mapper.readValue(body, Structure.class);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
        }
        validateStructure(structure);
        structure.setCreated(DateUtils.getCurrentDate());
        structure.setUpdated(DateUtils.getCurrentDate());

        createRdfStructure(structure);
        logger.info("Create Structure : {} - {}", structure.getId(), structure.getLabelLg1());
        return structure.getId().replace(" ", "-").toLowerCase();
    }

    public String setStructure(String id, String body) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Structure structure = new Structure(id);
        try {
            structure = mapper.readerForUpdating(structure).readValue(body);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
        }


        validateStructure(structure);

        structure.setUpdated(DateUtils.getCurrentDate());
        String structureId = structure.getId().replace(" ", "-").toLowerCase();
        URI structureUri = RdfUtils.structureIRI(structureId);
        repoGestion.clearStructureNodeAndComponents(structureUri);
        createRdfStructure(structure);
        logger.info("Update Structure : {} - {}", structure.getId(), structure.getLabelLg1());
        return structureId;
    }

    /**
     * Structure to sesame
     *
     * @throws RmesException
     */

    public void createRdfStructure(Structure structure) throws RmesException {
        String structureId = structure.getId().replace(" ", "-").toLowerCase();
        URI structureUri = RdfUtils.structureIRI(structureId);
        Resource graph = RdfUtils.structureGraph();

        createRdfStructure(structure, structureId, structureUri, graph);
    }

    public void createRdfStructure(Structure structure, String structureId, URI structureUri, Resource graph) throws RmesException {
        Model model = new LinkedHashModel();

        model.add(structureUri, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, graph);
        /*Required*/
        model.add(structureUri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(structureId), graph);
        model.add(structureUri, RDFS.LABEL, RdfUtils.setLiteralString(structure.getLabelLg1(), Config.LG1), graph);

        /*Optional*/
        RdfUtils.addTripleDateTime(structureUri, DCTERMS.CREATED, structure.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(structureUri, DCTERMS.MODIFIED, structure.getUpdated(), model, graph);

        RdfUtils.addTripleString(structureUri, RDFS.LABEL, structure.getLabelLg2(), Config.LG2, model, graph);
        RdfUtils.addTripleString(structureUri, DC.DESCRIPTION, structure.getDescriptionLg1(), Config.LG1, model, graph);
        RdfUtils.addTripleString(structureUri, DC.DESCRIPTION, structure.getDescriptionLg2(), Config.LG2, model, graph);


        createRdfComponentSpecifications(structureUri, structure.getComponentDefinitions(), model, graph);

        repoGestion.loadSimpleObject(structureUri, model, null);
    }

    public void createRdfComponentSpecifications(URI structureIRI, List<ComponentDefinition> componentList, Model model, Resource graph) throws RmesException {
        int nextID = getNextComponentSpecificationID();
        for (int i = 0; i < componentList.size(); i++) {
            ComponentDefinition componentDefinition = componentList.get(i);
            MutualizedComponent component = componentDefinition.getComponent();
            if (component.getId() == null) {
                try {
                    createMutualizedComponent(component);
                    componentDefinition.setComponent(component);
                } catch (RmesException e) {
                    logger.info("Cannot create component  : {}", component.getLabelLg1());
                }
            }

            if(componentDefinition.getCreated() == null){
                componentDefinition.setCreated(DateUtils.getCurrentDate());
            }
            componentDefinition.setModified(DateUtils.getCurrentDate());
            if (componentDefinition.getId() == null) {
                componentDefinition.setId("cs" + (nextID + i) );
            }
            createRdfComponentSpecification(structureIRI, model, componentDefinition, graph);
        }
    }

    public void createMutualizedComponent(MutualizedComponent component) throws RmesException {
        String id = structureComponentUtils.createComponent(component);
        component.setId(id);
    }

    public void createRdfComponentSpecification(URI structureIRI, Model model, ComponentDefinition componentDefinition, Resource graph) throws RmesException {

        URI componentSpecificationIRI;

        componentSpecificationIRI = getComponentDefinitionIRI(structureIRI.toString(), componentDefinition.getId());


        model.add(structureIRI, QB.COMPONENT, componentSpecificationIRI, graph);

        model.add(componentSpecificationIRI, RDF.TYPE, QB.COMPONENT_SPECIFICATION, graph);

        model.add(componentSpecificationIRI, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(componentDefinition.getId()), graph);
        if (componentDefinition.getCreated() != null) {
            model.add(componentSpecificationIRI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(componentDefinition.getCreated()), graph);
        }
        if (componentDefinition.getModified() != null) {
            model.add(componentSpecificationIRI, DCTERMS.MODIFIED, RdfUtils.setLiteralDateTime(componentDefinition.getModified()), graph);
        }
        if (componentDefinition.getOrder() != null) {
            model.add(componentSpecificationIRI, QB.ORDER, RdfUtils.setLiteralInt(componentDefinition.getOrder()), graph);
        }

        model.add(componentSpecificationIRI, QB.COMPONENT_REQUIRED, RdfUtils.setLiteralBoolean(componentDefinition.getRequired()), graph);

        for(String attachment : componentDefinition.getAttachment()){
            URI attachmentURI ;
            try {
                attachmentURI = RdfUtils.createIRI(attachment);
            } catch (Exception e){
                attachmentURI = RdfUtils.structureComponentMeasureIRI(attachment);
            }

            model.add(componentSpecificationIRI, QB.COMPONENT_ATTACHMENT, attachmentURI, graph);
        }
        MutualizedComponent component = componentDefinition.getComponent();
        if (component.getType().equals(QB.DIMENSION_PROPERTY.toString())) {

            model.add(componentSpecificationIRI, QB.DIMENSION, getDimensionIRI(component.getId()), graph);
        }
        if (component.getType().equals(QB.ATTRIBUTE_PROPERTY.toString())) {
            model.add(componentSpecificationIRI, QB.ATTRIBUTE, getAttributeIRI(component.getId()), graph);
        }
        if (component.getType().equals(QB.MEASURE_PROPERTY.toString())) {
            model.add(componentSpecificationIRI, QB.MEASURE, getMeasureIRI(component.getId()), graph);
        }
    }

    public int getNextComponentSpecificationID() throws RmesException {

        logger.info("Generate id for component");
        JSONObject json = repoGestion.getResponseAsObject(StructureQueries.lastIdForComponentDefinition());
        logger.debug("JSON when generating the id of a component : {}", json);
        if (json.length() == 0) {
            return 1000;
        }
        String id = json.getString(Constants.ID);
        if (id.equals("undefined")) {
            return 1000;
        }
        return (Integer.parseInt(id) + 1);
    }

    public URI getComponentDefinitionIRI(String structureIRI, String componentDefinitionId) {
        return RdfUtils.structureComponentDefinitionIRI(structureIRI, componentDefinitionId);
    }

    public URI getDimensionIRI(String id) {
        return RdfUtils.structureComponentDimensionIRI(id);
    }

    public URI getMeasureIRI(String id) {
        return RdfUtils.structureComponentMeasureIRI(id);
    }

    public URI getAttributeIRI(String id) {
        return RdfUtils.structureComponentAttributeIRI(id);
    }

    private void validateStructure(Structure structure) {
        if (structure.getId() == null) {
            throw new BadRequestException("The property identifiant is required");
        }
        if (structure.getLabelLg1() == null) {
            throw new BadRequestException("The property labelLg1 is required");
        }
        if (structure.getLabelLg2() == null) {
            throw new BadRequestException("The property labelLg2 is required");
        }
    }
}
