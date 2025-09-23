package fr.insee.rmes.bauhaus_services.structures.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.exceptions.errors.CodesListErrorCodes;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.structures.ComponentDefinition;
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.DateUtils;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StructureUtils extends RdfService {

    private static final String IO_EXCEPTION = "IOException";
	static final Logger logger = LoggerFactory.getLogger(StructureUtils.class);
    public static final String ATTACHMENT = "attachment";
    public static final String REQUIRED = "required";
    public static final String ORDER = "order";
    public static final String NOTATION = "notation";
    public static final String LABEL_LG1 = "csLabelLg1";
    public static final String LABEL_LG2 = "csLabelLg2";
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
            JSONArray attachmentsArray = repoGestion.getResponseAsArray(StructureQueries.getStructuresAttachments(id, componentDefinitionFlat.getString(COMPONENT_DEFINITION_ID)));

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
            if(componentDefinitionFlat.has(NOTATION)){
                componentDefinition.put(NOTATION, componentDefinitionFlat.getString(NOTATION));
            }
            if(componentDefinitionFlat.has(LABEL_LG1)){
                componentDefinition.put("labelLg1", componentDefinitionFlat.getString(LABEL_LG1));
            }
            if(componentDefinitionFlat.has(LABEL_LG2)){
                componentDefinition.put("labelLg2", componentDefinitionFlat.getString(LABEL_LG2));
            }

            componentDefinitionFlat.remove(REQUIRED);
            componentDefinitionFlat.remove(NOTATION);
            componentDefinitionFlat.remove(LABEL_LG1);
            componentDefinitionFlat.remove(LABEL_LG2);
            componentDefinitionFlat.remove(ORDER);
            componentDefinitionFlat.remove(COMPONENT_DEFINITION_CREATED);
            componentDefinitionFlat.remove(COMPONENT_DEFINITION_MODIFIED);
            componentDefinitionFlat.remove(COMPONENT_DEFINITION_ID);

            this.repoGestion.getMultipleTripletsForObject(componentDefinitionFlat, "contributor", StructureQueries.getComponentContributors(componentDefinitionFlat.getString("component")), "contributor");
            componentDefinitionFlat.remove("component");
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
        Structure structure;
        try {
            structure = mapper.readValue(body, Structure.class);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }
        validateStructure(structure);
        structure.setCreated(DateUtils.getCurrentDate());
        structure.setUpdated(DateUtils.getCurrentDate());
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
        if (json.isEmpty()) {
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
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
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
     * Structure to rdf
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
        List<ComponentDefinition> componentsWithoutId = structure.getComponentDefinitions().stream().filter((ComponentDefinition cd) -> 
            cd.getComponent().getId() == null
        ).collect(Collectors.toList());

        if(componentsWithoutId.isEmpty()){
            String[] ids = structure.getComponentDefinitions().stream().map(cd -> cd.getComponent().getId())
            		.map(Object::toString).collect(Collectors.toList()).toArray(new String[0]);
            boolean structureWithSameComponents = ids.length > 0 && repoGestion.getResponseAsBoolean(StructureQueries.checkUnicityStructure(structure.getId(), ids));
            if(structureWithSameComponents){
                throw new RmesBadRequestException(ErrorCodes.STRUCTURE_UNICITY,
                        "A structure with the same components already exists", "");
            }
        }
    }
    public void createRdfStructure(Structure structure, String structureId, IRI structureIri, Resource graph, ValidationStatus status) throws RmesException {

        repoGestion.clearStructureNodeAndComponents(structureIri);

        Model model = new LinkedHashModel();

        model.add(structureIri, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, graph);
        /*Required*/
        model.add(structureIri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(structureId), graph);
        model.add(structureIri, SKOS.NOTATION, RdfUtils.setLiteralString(structure.getIdentifiant()), graph);
        model.add(structureIri, RDFS.LABEL, RdfUtils.setLiteralString(structure.getLabelLg1(), config.getLg1()), graph);
        model.add(structureIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(status.toString()), graph);

        /*Optional*/
        RdfUtils.addTripleDateTime(structureIri, DCTERMS.CREATED, structure.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(structureIri, DCTERMS.MODIFIED, structure.getUpdated(), model, graph);

        RdfUtils.addTripleString(structureIri, RDFS.LABEL, structure.getLabelLg2(), config.getLg2(), model, graph);
        RdfUtils.addTripleString(structureIri, RDFS.COMMENT, structure.getDescriptionLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(structureIri, RDFS.COMMENT, structure.getDescriptionLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleString(structureIri, DC.CREATOR, structure.getCreator(), model, graph);
        structure.getContributor().forEach(contributor -> RdfUtils.addTripleString(structureIri, DC.CONTRIBUTOR, contributor, model, graph));

        RdfUtils.addTripleUri(structureIri, INSEE.DISSEMINATIONSTATUS, structure.getDisseminationStatus(), model, graph);

        repoGestion.loadSimpleObject(structureIri, model, null);

        createRdfComponentSpecifications(structureIri, structure.getComponentDefinitions(), graph);
    }

    public void createRdfComponentSpecifications(IRI structureIRI, List<ComponentDefinition> componentList, Resource graph) throws RmesException {
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
            componentDefinition.setId("cs" + (1000 + i) );
            createRdfComponentSpecification(structureIRI, componentDefinition, graph);
        }
    }

    public void createMutualizedComponent(MutualizedComponent component) throws RmesException {
        String id = structureComponentUtils.createComponent(component, new JSONObject());
        component.setId(id);
    }

    public void createRdfComponentSpecification(IRI structureIRI, ComponentDefinition componentDefinition, Resource graph) throws RmesException {
        Model model = new LinkedHashModel();

        IRI componentSpecificationIRI;

        componentSpecificationIRI = getComponentDefinitionIRI(RdfUtils.toString(structureIRI), componentDefinition.getId());


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
        if(componentDefinition.getNotation() != null){
            model.add(componentSpecificationIRI, SKOS.NOTATION, RdfUtils.setLiteralString(componentDefinition.getNotation()), graph);
        }
        if(componentDefinition.getLabelLg1() != null){
            model.add(componentSpecificationIRI, RDFS.LABEL, RdfUtils.setLiteralString(componentDefinition.getLabelLg1(), config.getLg1()), graph);
        }
        if(componentDefinition.getLabelLg2() != null){
            model.add(componentSpecificationIRI, RDFS.LABEL, RdfUtils.setLiteralString(componentDefinition.getLabelLg2(), config.getLg2()), graph);
        }
        MutualizedComponent component = componentDefinition.getComponent();
        if (component.getType().equals(RdfUtils.toString(QB.DIMENSION_PROPERTY))) {
            model.add(componentSpecificationIRI, QB.DIMENSION, getDimensionIRI(component.getId()), graph);
        }
        if (component.getType().equals(RdfUtils.toString(QB.ATTRIBUTE_PROPERTY))) {
            for(String attachment : componentDefinition.getAttachment()){
                IRI attachmentIRI ;
                try {
                    attachmentIRI = RdfUtils.createIRI(attachment);
                } catch (Exception e){
                    attachmentIRI = RdfUtils.structureComponentMeasureIRI(attachment);
                }

                model.add(componentSpecificationIRI, QB.COMPONENT_ATTACHMENT, attachmentIRI, graph);
            }

            model.add(componentSpecificationIRI, QB.ATTRIBUTE, getAttributeIRI(component.getId()), graph);
            model.add(componentSpecificationIRI, QB.COMPONENT_REQUIRED, RdfUtils.setLiteralBoolean(componentDefinition.getRequired()), graph);
        }
        if (component.getType().equals(RdfUtils.toString(QB.MEASURE_PROPERTY))) {
            model.add(componentSpecificationIRI, QB.MEASURE, getMeasureIRI(component.getId()), graph);
        }
        repoGestion.loadSimpleObject(componentSpecificationIRI, model);
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
        if (structure.getIdentifiant() == null) {
            throw new RmesBadRequestException("The property identifiant is required");
        }
        if (structure.getLabelLg1() == null) {
            throw new RmesBadRequestException("The property labelLg1 is required");
        }
        if (structure.getLabelLg2() == null) {
            throw new RmesBadRequestException("The property labelLg2 is required");
        }
    }

    public void deleteStructure(String structureId) throws RmesException {
        String structureState = getValidationStatus(structureId);
        if(!structureState.equalsIgnoreCase("Unpublished")){
            throw new RmesBadRequestException(CodesListErrorCodes.STRUCTURE_DELETE_ONLY_UNPUBLISHED, "Only unpublished codelist can be deleted");
        }
        else {
            IRI structureIri = RdfUtils.structureIRI(structureId);
            repoGestion.clearStructureNodeAndComponents(structureIri);
            repoGestion.deleteObject(structureIri, null);
        }
    }

    public String publishStructure(JSONObject structure) throws RmesException {
        if(structure.isNull(Constants.CREATOR) || "".equals(structure.getString(Constants.CREATOR))){
            throw new RmesBadRequestException(ErrorCodes.COMPONENT_PUBLICATION_EMPTY_CREATOR, "The creator should not be empty", new JSONArray());
        }

        if(structure.isNull("disseminationStatus") || "".equals(structure.getString("disseminationStatus"))){
            throw new RmesBadRequestException(ErrorCodes.COMPONENT_PUBLICATION_EMPTY_STATUS, "The dissemination status should not be empty", new JSONArray());
        }

        String id = structure.getString(Constants.ID);
        JSONArray ids = repoGestion.getResponseAsArray(StructureQueries.getUnValidatedComponent(id));
        for (int i = 0; i < ids.length(); i++) {
            String idComponent = ((JSONObject) ids.get(i)).getString(Constants.ID);
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
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }

        IRI structureIri = RdfUtils.structureIRI(structureObject.getId());

        this.structurePublication.publish(structureIri);

        structureObject.setUpdated(DateUtils.getCurrentDate());
        repoGestion.clearStructureNodeAndComponents(structureIri);
        createRdfStructure(structureObject, ValidationStatus.VALIDATED);

        return structure.toString();
    }
}
