package fr.insee.rmes.bauhaus_services.structures.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.DateUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.Arrays;

@Component
public class StructureComponentUtils extends RdfService {
    static final Logger logger = LogManager.getLogger(StructureComponentUtils.class);

    public String formatComponent(String id, JSONObject response) throws RmesException {
        response.put("id", id);
        addCodeListRange(response);
        addStructures(response, id);
        return response.toString();

    }

    private void addStructures(JSONObject response, String id) throws RmesException {
        JSONArray structures = repoGestion.getResponseAsArray(StructureQueries.getStructuresForComponent(id));
        response.put("structures", structures);
    }

    private void addCodeListRange(JSONObject response) {
        if (response.has("codeList")) {
            response.put("range", INSEE.CODELIST.toString());
        }
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
        createRDFForComponent(component, ValidationStatus.MODIFIED);

        return component.getId();
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

    public String createComponent(String body) throws RmesException {
        MutualizedComponent component;
        try {
            component = deserializeBody(body);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");

        }
        return createComponent(component);
    }

    private void createRDFForComponent(MutualizedComponent component, ValidationStatus status) throws RmesException {
        String type = component.getType();

        if (type.equals(QB.ATTRIBUTE_PROPERTY.toString())) {
            createRDFForComponent(component, QB.ATTRIBUTE_PROPERTY, RdfUtils.structureComponentAttributeIRI(component.getId()), status);
        } else if (type.equals(QB.MEASURE_PROPERTY.toString())) {
            createRDFForComponent(component, QB.MEASURE_PROPERTY, RdfUtils.structureComponentMeasureIRI(component.getId()), status);
        } else {
            createRDFForComponent(component, QB.DIMENSION_PROPERTY, RdfUtils.structureComponentDimensionIRI(component.getId()), status);
        }
    }

    private void createRDFForComponent(MutualizedComponent component, Resource resource, IRI componentURI, ValidationStatus status) throws RmesException {
        Model model = new LinkedHashModel();
        Resource graph = RdfUtils.structuresComponentsGraph();

        /*Const*/
        model.add(componentURI, RDF.TYPE, resource, graph);

        /*Required*/
        model.add(componentURI, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(component.getId()), graph);

        model.add(componentURI, RDFS.LABEL, RdfUtils.setLiteralString(component.getLabelLg1(), Config.LG1), graph);
        model.add(componentURI, RDFS.LABEL, RdfUtils.setLiteralString(component.getLabelLg2(), Config.LG2), graph);
        model.add(componentURI, INSEE.IDENTIFIANT_METIER, RdfUtils.setLiteralString(component.getIdentifiant()), graph);
        model.add(componentURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(status), graph);
        model.add(componentURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(component.getCreated()), graph);
        model.add(componentURI, DCTERMS.MODIFIED, RdfUtils.setLiteralDateTime(component.getUpdated()), graph);

        RdfUtils.addTripleUri(componentURI, QB.CONCEPT, INSEE.STRUCTURE_CONCEPT + component.getConcept(), model, graph);

        if (component.getRange() != null && component.getRange().equals(INSEE.CODELIST.toString())) {
            RdfUtils.addTripleUri(componentURI, RDFS.RANGE, Config.CODE_LIST_BASE_URI + "/" + component.getCodeList() + "/Class", model, graph);
        } else {
            RdfUtils.addTripleUri(componentURI, RDFS.RANGE, component.getRange(), model, graph);
        }

        RdfUtils.addTripleUri(componentURI, QB.CODE_LIST, component.getCodeList(), model, graph);
        RdfUtils.addTripleStringMdToXhtml(componentURI, RDFS.COMMENT, component.getDescriptionLg1(), Config.LG1, model, graph);
        RdfUtils.addTripleStringMdToXhtml(componentURI, RDFS.COMMENT, component.getDescriptionLg2(), Config.LG2, model, graph);

        repoGestion.loadSimpleObject(componentURI, model, null);
    }

    private String generateNextId(String type) throws RmesException {
        if (type.equals(QB.ATTRIBUTE_PROPERTY.toString())) {
            return generateNextId("a", "attributs", QB.ATTRIBUTE_PROPERTY);
        }
        if (type.equals(QB.MEASURE_PROPERTY.toString())) {
            return generateNextId("m", "mesures", QB.MEASURE_PROPERTY);
        }
        return generateNextId("d", "dimensions", QB.DIMENSION_PROPERTY);

    }


    private String generateNextId(String prefix, String namespaceSuffix, IRI type) throws RmesException {
        logger.info("Generate id for component");
        JSONObject json = repoGestion.getResponseAsObject(StructureQueries.lastId(namespaceSuffix, type.toString()));
        logger.debug("JSON when generating the id of a component : {}", json);
        if (json.length() == 0) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (id.equals("undefined")) {
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
        /*if (component.getRange() != null &&
                !(component.getRange().equals(INSEE.CODELIST.toString()) || Arrays.asList(XSD.getURIForRange()).contains(component.getRange()))) {
            throw new BadRequestException("The range is not valid");
        }*/

    }



    private MutualizedComponent deserializeBody(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(body, MutualizedComponent.class);
    }
}
