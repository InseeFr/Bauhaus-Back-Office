package fr.insee.rmes.bauhaus_services.structures.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
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
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.structures.ComponentDefinition;
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.persistance.ontologies.INSEE;
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
    StructureComponent structureComponent;

    @Autowired
    StructureComponentUtils structureComponentUtils;

    @Autowired
    StructurePublication structurePublication;

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
        //structure.setDisseminationStatus(DisseminationStatus.PUBLIC_GENERIC.getUrl());
        String id = generateNextId();
        structure.setId(id);
        createRdfStructure(structure, ValidationStatus.UNPUBLISHED);
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

    private String getValidationStatus(String id) throws RmesException {
        return repoGestion.getResponseAsObject(StructureQueries.getValidationStatus(id)).getString("state");
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

        String status= getValidationStatus(id);
        if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
            createRdfStructure(structure, ValidationStatus.UNPUBLISHED);
        } else {
            createRdfStructure(structure, ValidationStatus.MODIFIED);
        }

        logger.info("Update Structure : {} - {}", structure.getId(), structure.getLabelLg1());
        return structure.getId();
    }

    /**
     * Structure to sesame
     *
     * @throws RmesException
     */

    public void createRdfStructure(Structure structure, ValidationStatus status) throws RmesException {
        String structureId = structure.getId();
        IRI structureIri = RdfUtils.structureIRI(structureId);
        Resource graph = RdfUtils.structureGraph();

        createRdfStructure(structure, structureId, structureIri, graph, status);
    }

    private void checkUnicityForStructure(Structure structure) throws RmesException {
        List<ComponentDefinition> componentsWithoutId = structure.getComponentDefinitions().stream().filter((ComponentDefinition cd) -> {
            return cd.getComponent().getId() == null;
        }).collect(Collectors.toList());

        if(componentsWithoutId.size() == 0){
            String[] ids = structure.getComponentDefinitions().stream().map(cd -> {
                return cd.getComponent().getId();
            }).map(Object::toString).collect(Collectors.toList()).toArray(new String[0]);
            Boolean structureWithSameComponents = ids.length > 0 && repoGestion.getResponseAsBoolean(StructureQueries.checkUnicityStructure(structure.getId(), ids));
            if(structureWithSameComponents){
                throw new RmesUnauthorizedException(ErrorCodes.STRUCTURE_UNICITY,
                        "A structure with the same components already exists", "");
            }
        }
    }
    public void createRdfStructure(Structure structure, String structureId, IRI structureIri, Resource graph, ValidationStatus status) throws RmesException {


        Model model = new LinkedHashModel();

        model.add(structureIri, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, graph);
        /*Required*/
        model.add(structureIri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(structureId), graph);
        model.add(structureIri, INSEE.IDENTIFIANT_METIER, RdfUtils.setLiteralString(structure.getIdentifiant()), graph);
        model.add(structureIri, RDFS.LABEL, RdfUtils.setLiteralString(structure.getLabelLg1(), Config.LG1), graph);
        model.add(structureIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(status.toString()), graph);

        /*Optional*/
        RdfUtils.addTripleDateTime(structureIri, DCTERMS.CREATED, structure.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(structureIri, DCTERMS.MODIFIED, structure.getUpdated(), model, graph);

        RdfUtils.addTripleString(structureIri, RDFS.LABEL, structure.getLabelLg2(), Config.LG2, model, graph);
        RdfUtils.addTripleString(structureIri, DC.DESCRIPTION, structure.getDescriptionLg1(), Config.LG1, model, graph);
        RdfUtils.addTripleString(structureIri, DC.DESCRIPTION, structure.getDescriptionLg2(), Config.LG2, model, graph);

        RdfUtils.addTripleString(structureIri, DC.CREATOR, structure.getCreator(), model, graph);
        RdfUtils.addTripleString(structureIri, DC.CONTRIBUTOR, structure.getContributor(), model, graph);
        RdfUtils.addTripleUri(structureIri, INSEE.DISSEMINATIONSTATUS, structure.getDisseminationStatus(), model, graph);

        createRdfComponentSpecifications(structure, structureIri, structure.getComponentDefinitions(), model, graph);

        repoGestion.loadSimpleObject(structureIri, model, null);
    }

    public void createRdfComponentSpecifications(Structure structure, IRI structureIRI, List<ComponentDefinition> componentList, Model model, Resource graph) throws RmesException {
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
                componentDefinition.setId("cs" + (1000 + i) );
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

        componentSpecificationIRI = getComponentDefinitionIRI(((SimpleIRI)structureIRI).toString(), componentDefinition.getId());


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
        if (component.getType().equals(((SimpleIRI)QB.DIMENSION_PROPERTY).toString())) {

            model.add(componentSpecificationIRI, QB.DIMENSION, getDimensionIRI(component.getId()), graph);
        }
        if (component.getType().equals(((SimpleIRI)QB.ATTRIBUTE_PROPERTY).toString())) {
            model.add(componentSpecificationIRI, QB.ATTRIBUTE, getAttributeIRI(component.getId()), graph);
            model.add(componentSpecificationIRI, QB.COMPONENT_REQUIRED, RdfUtils.setLiteralBoolean(componentDefinition.getRequired()), graph);
        }
        if (component.getType().equals(((SimpleIRI)QB.MEASURE_PROPERTY).toString())) {
            model.add(componentSpecificationIRI, QB.MEASURE, getMeasureIRI(component.getId()), graph);
        }
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

    private void validateStructure(Structure structure) throws RmesException {
        checkUnicityForStructure(structure);
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
        IRI structureIri = RdfUtils.structureIRI(structureId);
        repoGestion.clearStructureNodeAndComponents(structureIri);
        repoGestion.deleteObject(structureIri, null);
    }

    public String publishStructure(JSONObject structure) throws RmesException {
        String id = structure.getString("id");
        JSONArray ids = repoGestion.getResponseAsArray(StructureQueries.getUnValidatedComponent(id));
        for (int i = 0; i < ids.length(); i++) {
            String idComponent = ((JSONObject) ids.get(i)).getString("id");
            try {
                structureComponent.publishComponent(idComponent);
            } catch (RmesException e) {
                throw new RmesUnauthorizedException(ErrorCodes.STRUCTURE_PUBLICATION_VALIDATED_COMPONENT, "The component " + idComponent + " component can not be published", new JSONArray());
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Structure structureObject = new Structure(id);
        try {
            structureObject = mapper.readerForUpdating(structureObject).readValue(structure.toString());
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
        }

        IRI structureIri = RdfUtils.structureIRI(structureObject.getId());

        this.structurePublication.publish(structureIri);

        structureObject.setUpdated(DateUtils.getCurrentDate());
        repoGestion.clearStructureNodeAndComponents(structureIri);
        createRdfStructure(structureObject, ValidationStatus.VALIDATED);

        return structure.toString();
    }
}
