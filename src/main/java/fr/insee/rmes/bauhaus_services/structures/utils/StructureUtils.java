package fr.insee.rmes.bauhaus_services.structures.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;

import fr.insee.rmes.persistance.ontologies.INSEE;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
public class StructureUtils extends RdfService {

    static final Logger logger = LogManager.getLogger(StructureUtils.class);
    public static final String ATTACHMENT = "attachment";
    public static final String REQUIRED = "required";
    public static final String ORDER = "order";
    public static final String COMPONENT_DEFINITION_CREATED = "componentDefinitionCreated";
    public static final String COMPONENT_DEFINITION_MODIFIED = "componentDefinitionModified";
    public static final String COMPONENT_DEFINITION_ID = "componentDefinitionId";

    @Autowired
    StructureComponentUtils structureComponentUtils;

    public JSONArray formatStructuresForSearch(JSONArray structures) throws RmesException {
        for (int i = 0; i < structures.length(); i++) {
            JSONObject current = structures.getJSONObject(i);
            current.put("components", repoGestion.getResponseAsArray(StructureQueries.getComponentsForStructure(current.get(Constants.ID))));
        }
        return structures;
    }

    public JSONObject formatStructure(JSONObject structure, String id) throws RmesException {
        structure.put(Constants.ID, id);


        JSONArray componentDefinitions = new JSONArray();

        JSONArray componentDefinitionsFlat = repoGestion.getResponseAsArray(StructureQueries.getComponentsForStructure(id));


        for (int i = 0; i < componentDefinitionsFlat.length(); i++) {
            JSONObject componentDefinitionFlat = componentDefinitionsFlat.getJSONObject(i);
            JSONArray attachmentsArray = repoGestion.getResponseAsArray(StructureQueries.getStructuresAttachments(componentDefinitionFlat.getString(COMPONENT_DEFINITION_ID)));

            List<String> attachments = new ArrayList<>();
            for(int j = 0; j < attachmentsArray.length(); j++){
                attachments.add(attachmentsArray.getJSONObject(j).getString(ATTACHMENT));
            }


            JSONObject componentDefinition= new JSONObject();

             componentDefinition.put(ATTACHMENT, attachments);


            if(componentDefinitionFlat.has(REQUIRED)){
                componentDefinition.put(REQUIRED, Boolean.parseBoolean(componentDefinitionFlat.getString(REQUIRED)));
            }


            if(componentDefinitionFlat.has(ORDER)){
                componentDefinition.put(ORDER, componentDefinitionFlat.getString(ORDER));
            }
            if(componentDefinitionFlat.has(COMPONENT_DEFINITION_CREATED)){
                componentDefinition.put("created", componentDefinitionFlat.getString(COMPONENT_DEFINITION_CREATED));

            }
            if(componentDefinitionFlat.has(COMPONENT_DEFINITION_MODIFIED)){
                componentDefinition.put("modified", componentDefinitionFlat.getString(COMPONENT_DEFINITION_MODIFIED));

            }
            if(componentDefinitionFlat.has(COMPONENT_DEFINITION_ID)){
                componentDefinition.put(Constants.ID, componentDefinitionFlat.getString(COMPONENT_DEFINITION_ID));

            }
            componentDefinitionFlat.remove(REQUIRED);
            componentDefinitionFlat.remove(ORDER);
            componentDefinitionFlat.remove(COMPONENT_DEFINITION_CREATED);
            componentDefinitionFlat.remove(COMPONENT_DEFINITION_MODIFIED);
            componentDefinitionFlat.remove(COMPONENT_DEFINITION_ID);
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

        String id = generateNextId();
        structure.setId(id);

        createRdfStructure(structure);
        logger.info("Create Structure : {} - {}", structure.getId(), structure.getLabelLg1());
        return structure.getId().replace(" ", "-").toLowerCase();
    }

    private String generateNextId() throws RmesException {
        String prefix = "dsd";
        logger.info("Generate id for structure");
        JSONObject json = repoGestion.getResponseAsObject(StructureQueries.lastStructureId());
        logger.debug("JSON when generating the id of a structure : {}", json);
        if (json.length() == 0) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (id.equals(Constants.UNDEFINED)) {
            return prefix + "1000";
        }
        return prefix + (Integer.parseInt(id) + 1);
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
        IRI structureIri = RdfUtils.structureIRI(structure.getId());
        repoGestion.clearStructureNodeAndComponents(structureIri);
        createRdfStructure(structure);
        logger.info("Update Structure : {} - {}", structure.getId(), structure.getLabelLg1());
        return structure.getId();
    }

    /**
     * Structure to sesame
     *
     * @throws RmesException
     */

    public void createRdfStructure(Structure structure) throws RmesException {
        String structureId = structure.getId();
        IRI structureIri = RdfUtils.structureIRI(structureId);
        Resource graph = RdfUtils.structureGraph();

        createRdfStructure(structure, structureId, structureIri, graph);
    }

    public void createRdfStructure(Structure structure, String structureId, IRI structureIri, Resource graph) throws RmesException {
        Model model = new LinkedHashModel();

        model.add(structureIri, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, graph);
        /*Required*/
        model.add(structureIri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(structureId), graph);
        model.add(structureIri, INSEE.IDENTIFIANT_METIER, RdfUtils.setLiteralString(structure.getIdentifiant()), graph);
        model.add(structureIri, RDFS.LABEL, RdfUtils.setLiteralString(structure.getLabelLg1(), Config.LG1), graph);

        /*Optional*/
        RdfUtils.addTripleDateTime(structureIri, DCTERMS.CREATED, structure.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(structureIri, DCTERMS.MODIFIED, structure.getUpdated(), model, graph);

        RdfUtils.addTripleString(structureIri, RDFS.LABEL, structure.getLabelLg2(), Config.LG2, model, graph);
        RdfUtils.addTripleString(structureIri, DC.DESCRIPTION, structure.getDescriptionLg1(), Config.LG1, model, graph);
        RdfUtils.addTripleString(structureIri, DC.DESCRIPTION, structure.getDescriptionLg2(), Config.LG2, model, graph);


        createRdfComponentSpecifications(structureIri, structure.getComponentDefinitions(), model, graph);

        repoGestion.loadSimpleObject(structureIri, model, null);
    }

    public void createRdfComponentSpecifications(IRI structureIRI, List<ComponentDefinition> componentList, Model model, Resource graph) throws RmesException {
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

    public void createRdfComponentSpecification(IRI structureIRI, Model model, ComponentDefinition componentDefinition, Resource graph) {

        IRI componentSpecificationIRI;

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


        for(String attachment : componentDefinition.getAttachment()){
            IRI attachmentIRI ;
            try {
                attachmentIRI = RdfUtils.createIRI(attachment);
            } catch (Exception e){
                attachmentIRI = RdfUtils.structureComponentMeasureIRI(attachment);
            }

            model.add(componentSpecificationIRI, QB.COMPONENT_ATTACHMENT, attachmentIRI, graph);
        }
        MutualizedComponent component = componentDefinition.getComponent();
        if (component.getType().equals(QB.DIMENSION_PROPERTY.toString())) {

            model.add(componentSpecificationIRI, QB.DIMENSION, getDimensionIRI(component.getId()), graph);
        }
        if (component.getType().equals(QB.ATTRIBUTE_PROPERTY.toString())) {
            model.add(componentSpecificationIRI, QB.ATTRIBUTE, getAttributeIRI(component.getId()), graph);
            model.add(componentSpecificationIRI, QB.COMPONENT_REQUIRED, RdfUtils.setLiteralBoolean(componentDefinition.getRequired()), graph);
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
        if (id.equals(Constants.UNDEFINED)) {
            return 1000;
        }
        return (Integer.parseInt(id) + 1);
    }

    public IRI getComponentDefinitionIRI(String structureIRI, String componentDefinitionId) {
        return RdfUtils.structureComponentDefinitionIRI(structureIRI, componentDefinitionId);
    }

    public IRI getDimensionIRI(String id) {
        return RdfUtils.structureComponentDimensionIRI(id);
    }

    public IRI getMeasureIRI(String id) {
        return RdfUtils.structureComponentMeasureIRI(id);
    }

    public IRI getAttributeIRI(String id) {
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

    public void deleteStructure(String structureId) throws RmesException {
        JSONArray components = repoGestion.getResponseAsArray(StructureQueries.getComponentsForStructure(structureId));
        System.out.println("Deleting structure " + structureId);
        components.forEach(component -> {
            String id = ((JSONObject) component).getString("id");
            String type = ((JSONObject) component).getString("type");
            System.out.println("Deleting component " + id);
            try {
                structureComponentUtils.deleteComponent((JSONObject) component, id, type);
            } catch (RmesException e) {
                System.out.println("Could not delete component " + id);
            }
        });
        IRI structureIri = RdfUtils.structureIRI(structureId);
        repoGestion.clearStructureNodeAndComponents(structureIri);
        repoGestion.deleteObject(structureIri, null);
    }
}
