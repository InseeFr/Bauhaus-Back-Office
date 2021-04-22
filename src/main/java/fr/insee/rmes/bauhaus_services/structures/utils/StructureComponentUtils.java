package fr.insee.rmes.bauhaus_services.structures.utils;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.BadRequestException;

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.rdf4j.model.vocabulary.SKOS;
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
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.DateUtils;

@Component
public class StructureComponentUtils extends RdfService {
    static final Logger logger = LogManager.getLogger(StructureComponentUtils.class);
    public static final String VALIDATED = "Validated";
    public static final String MODIFIED = "Modified";

    @Autowired
    ComponentPublication componentPublication;

    public JSONObject formatComponent(String id, JSONObject response) throws RmesException {
        response.put(Constants.ID, id);
        addCodeListRange(response);
        addStructures(response, id);
        return response;

    }

    private void addStructures(JSONObject response, String id) throws RmesException {
        JSONArray structures = repoGestion.getResponseAsArray(StructureQueries.getStructuresForComponent(id));
        response.put("structures", structures);
    }

    private void addCodeListRange(JSONObject response) {
        if (response.has("codeList")) {
            response.put("range", ((SimpleIRI)INSEE.CODELIST).toString());
        }
    }

    private String getValidationStatus(String id) throws RmesException {
        return repoGestion.getResponseAsObject(StructureQueries.getValidationStatus(id)).getString("state");
    }

    public String updateComponent(String componentId, String body) throws RmesException {
        MutualizedComponent component;
        try {
            component = deserializeBody(body);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
        }

        if (component.getId() == null || !component.getId().equals(componentId)) {
            throw new BadRequestException("The id of the component should be the same as the one defined in the request");
        }

        validateComponent(component);

        component.setUpdated(DateUtils.getCurrentDate());
        String status= getValidationStatus(componentId);
        if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
            createRDFForComponent(component, ValidationStatus.UNPUBLISHED);
        } else {
            createRDFForComponent(component, ValidationStatus.MODIFIED);
        }


        return component.getId();
    }

    public String createComponent(String body) throws RmesException {
        MutualizedComponent component;
        try {
            component = deserializeBody(body);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");

        }
        return createComponent(component);
    }

    public String createComponent(MutualizedComponent component) throws RmesException {
        if (component.getId() != null) {
            throw new BadRequestException("During the creation of a new component, the id property should be null");
        }
        String id = generateNextId(component.getType());

        return createComponent(component, id);
    }

    public String createComponent(MutualizedComponent component, String id) throws RmesException {
        validateComponent(component);

        component.setId(id);

        String currentDate = DateUtils.getCurrentDate();
        component.setCreated(currentDate);
        component.setUpdated(currentDate);
        createRDFForComponent(component, ValidationStatus.UNPUBLISHED);
        return id;
    }


    private void createRDFForComponent(MutualizedComponent component, ValidationStatus status) throws RmesException {


        if(StringUtils.isNotEmpty(component.getConcept()) && StringUtils.isNotEmpty(component.getCodeList())){
            Boolean componentsWithSameCodelistAndConcept = repoGestion.getResponseAsBoolean(StructureQueries.checkUnicityMutualizedComponent(component.getId(), component.getConcept(), component.getCodeList(), component.getType()));

            if(componentsWithSameCodelistAndConcept){
                throw new RmesUnauthorizedException(ErrorCodes.COMPONENT_UNICITY,
                        "A component with the same code list and concept already exists", "");
            }
        }

        String type = component.getType();
        if (type.equals(((SimpleIRI)QB.ATTRIBUTE_PROPERTY).toString())) {
            createRDFForComponent(component, QB.ATTRIBUTE_PROPERTY, RdfUtils.structureComponentAttributeIRI(component.getId()), status);
        } else if (type.equals(((SimpleIRI)QB.MEASURE_PROPERTY).toString())) {
            createRDFForComponent(component, QB.MEASURE_PROPERTY, RdfUtils.structureComponentMeasureIRI(component.getId()), status);
        } else {
            createRDFForComponent(component, QB.DIMENSION_PROPERTY, RdfUtils.structureComponentDimensionIRI(component.getId()), status);
        }
    }

    private void createRDFForComponent(MutualizedComponent component, Resource resource, IRI componentURI, ValidationStatus status) throws RmesException {
        Model model = new LinkedHashModel();
        Resource graph = RdfUtils.structureComponentGraph();

        /*Const*/
        model.add(componentURI, RDF.TYPE, resource, graph);

        /*Required*/
        model.add(componentURI, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(component.getId()), graph);

        model.add(componentURI, RDFS.LABEL, RdfUtils.setLiteralString(component.getLabelLg1(), Config.LG1), graph);
        model.add(componentURI, RDFS.LABEL, RdfUtils.setLiteralString(component.getLabelLg2(), Config.LG2), graph);
        model.add(componentURI, SKOS.NOTATION, RdfUtils.setLiteralString(component.getIdentifiant()), graph);
        model.add(componentURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(status), graph);
        model.add(componentURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(component.getCreated()), graph);
        model.add(componentURI, DCTERMS.MODIFIED, RdfUtils.setLiteralDateTime(component.getUpdated()), graph);
        RdfUtils.addTripleString(componentURI, DC.CREATOR, component.getCreator(), model, graph);
        RdfUtils.addTripleString(componentURI, DC.CONTRIBUTOR, component.getContributor(), model, graph);
        RdfUtils.addTripleUri(componentURI, INSEE.DISSEMINATIONSTATUS, component.getDisseminationStatus(), model, graph);

        if(component.getConcept() != null){
            RdfUtils.addTripleUri(componentURI, QB.CONCEPT, INSEE.STRUCTURE_CONCEPT + component.getConcept(), model, graph);
        }

        if (component.getRange() != null && component.getRange().equals(((SimpleIRI)INSEE.CODELIST).toString())) {
            RdfUtils.addTripleUri(componentURI, RDF.TYPE, QB.CODED_PROPERTY, model, graph);

            JSONObject object = repoGestion.getResponseAsObject(StructureQueries.getUriClasseOwl(component.getCodeList()));

            if(object.has("uriClasseOwl")){
                RdfUtils.addTripleUri(componentURI, RDFS.RANGE, object.getString("uriClasseOwl"), model, graph);
            } else {
                RdfUtils.addTripleUri(componentURI, RDFS.RANGE, SKOS.CONCEPT, model, graph);
            }
        } else {
            RdfUtils.addTripleUri(componentURI, RDFS.RANGE, component.getRange(), model, graph);
        }

        RdfUtils.addTripleUri(componentURI, QB.CODE_LIST, component.getCodeList(), model, graph);
        RdfUtils.addTripleString(componentURI, RDFS.COMMENT, component.getDescriptionLg1(), Config.LG1, model, graph);
        RdfUtils.addTripleString(componentURI, RDFS.COMMENT, component.getDescriptionLg2(), Config.LG2, model, graph);

        repoGestion.loadSimpleObject(componentURI, model, null);
    }

    private String generateNextId(String type) throws RmesException {
        if (type.equals(((SimpleIRI)QB.ATTRIBUTE_PROPERTY).toString())) {
            return generateNextId("a", "attribut", QB.ATTRIBUTE_PROPERTY);
        }
        if (type.equals(((SimpleIRI)QB.MEASURE_PROPERTY).toString())) {
            return generateNextId("m", "mesure", QB.MEASURE_PROPERTY);
        }
        return generateNextId("d", "dimension", QB.DIMENSION_PROPERTY);

    }


    private String generateNextId(String prefix, String namespaceSuffix, IRI type) throws RmesException {
        logger.info("Generate id for component");
        JSONObject json = repoGestion.getResponseAsObject(StructureQueries.lastId(namespaceSuffix, ((SimpleIRI)type).toString()));
        logger.debug("JSON when generating the id of a component : {}", json);
        if (json.length() == 0) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (id.equals(Constants.UNDEFINED)) {
            return prefix + "1000";
        }
        return prefix + (Integer.parseInt(id) + 1);
    }


    private void validateComponent(MutualizedComponent component) {
        if (component.getIdentifiant() == null) {
            throw new BadRequestException("The property identifiant is required");
        }
        if (component.getLabelLg1() == null) {
            throw new BadRequestException("The property labelLg1 is required");
        }
        if (component.getLabelLg2() == null) {
            throw new BadRequestException("The property labelLg2 is required");
        }
        if (component.getType() == null) {
            throw new BadRequestException("The property type is required");
        }
        if (!Arrays.asList(QB.getURIForComponent()).contains(component.getType())) {
            throw new BadRequestException("The property type is not valid");
        }

    }



    private MutualizedComponent deserializeBody(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(body, MutualizedComponent.class);
    }

    public void deleteComponent(JSONObject component, String id, String type) throws RmesException {
        String state = component.getString("validationState");
        if(state.equals(VALIDATED) || state.equals(MODIFIED)){
            throw new RmesException(ErrorCodes.COMPONENT_FORBIDDEN_DELETE, "You cannot delete a validated component", new JSONArray());
        }
        JSONArray structures = component.getJSONArray("structures");

        boolean findPublishedStructure = false;
        for (int i = 0; i < structures.length(); i++) {
            JSONObject structure = (JSONObject) structures.get(i);
            String stateStructure = structure.getString("validationState"); //update state to test foreach
            if(stateStructure.equals(VALIDATED) || stateStructure.equals(MODIFIED)){
                findPublishedStructure = true;
                break;
            }
        }

        if(findPublishedStructure){
            throw new RmesException(ErrorCodes.COMPONENT_FORBIDDEN_DELETE, "You cannot delete a validated component", new JSONArray());
        }
        IRI componentIri;
        if (type.equalsIgnoreCase(((SimpleIRI)QB.ATTRIBUTE_PROPERTY).toString())) {
            componentIri =  RdfUtils.structureComponentAttributeIRI(id);
        } else if (type.equalsIgnoreCase(((SimpleIRI)QB.MEASURE_PROPERTY).toString())) {
            componentIri =  RdfUtils.structureComponentMeasureIRI(id);
        } else {
            componentIri =  RdfUtils.structureComponentDimensionIRI(id);
        }
        repoGestion.deleteObject(componentIri, null);
    }

    public String publishComponent(JSONObject component) throws RmesException {

        if(component.isNull("creator") || "".equals(component.getString("creator"))){
            throw new RmesUnauthorizedException(ErrorCodes.COMPONENT_PUBLICATION_EMPTY_CREATOR, "The creator should not be empty", new JSONArray());
        }

        if(component.isNull("disseminationStatus") || "".equals(component.getString("disseminationStatus"))){
            throw new RmesUnauthorizedException(ErrorCodes.COMPONENT_PUBLICATION_EMPTY_STATUS, "The dissemination status should not be empty", new JSONArray());
        }

        if(!component.isNull("concept") && !"".equals(component.getString("concept"))){
            if(!repoGestion.getResponseAsBoolean(ConceptsQueries.isConceptValidated(component.getString("concept")))){
                throw new RmesUnauthorizedException(ErrorCodes.COMPONENT_PUBLICATION_VALIDATED_CONCEPT, "The concept should be validated", new JSONArray());
            }
        }

        if(!component.isNull("codeList") && !"".equals(component.getString("codeList"))){
            if(!repoGestion.getResponseAsBoolean(CodeListQueries.isCodesListValidated(component.getString("codeList")))){
                throw new RmesUnauthorizedException(ErrorCodes.COMPONENT_PUBLICATION_VALIDATED_CODESLIST, "The codes list should be validated", new JSONArray());
            }
        }


        MutualizedComponent mutualizedComponent;
        try {
            mutualizedComponent = deserializeBody(component.toString());
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
        }
        mutualizedComponent.setUpdated(DateUtils.getCurrentDate());

        String type = component.getString("type");
        String id = component.getString("id");

        if (type.equals(((SimpleIRI)QB.ATTRIBUTE_PROPERTY).toString())) {
            componentPublication.publishComponent(RdfUtils.structureComponentAttributeIRI(id), QB.ATTRIBUTE_PROPERTY);
        }
        if (type.equals(((SimpleIRI)QB.MEASURE_PROPERTY).toString())) {
            componentPublication.publishComponent(RdfUtils.structureComponentMeasureIRI(id), QB.MEASURE_PROPERTY);
        }
        if (type.equals(((SimpleIRI)QB.DIMENSION_PROPERTY).toString())) {
            componentPublication.publishComponent(RdfUtils.structureComponentDimensionIRI(id), QB.DIMENSION_PROPERTY);
        }

        createRDFForComponent(mutualizedComponent, ValidationStatus.VALIDATED);

        return id;
    }
}
