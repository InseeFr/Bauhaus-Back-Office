package fr.insee.rmes.bauhaus_services.structures.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.IdGenerator;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Repository;

@Repository
public class StructureComponentRepository extends RdfService {
    private final BauhausLanguagesProperties languages;

    static final Logger logger = LoggerFactory.getLogger(StructureComponentRepository.class);

    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_LENGTH = "minLength";
    private static final String PATTERN = "pattern";
    private static final String IO_EXCEPTION = "IOException";
    public static final String VALIDATED = "Validated";
    public static final String MODIFIED = "Modified";

    private final ComponentPublication componentPublication;

    private final StructureQueries structureQueries;

    private final CodeListsQueries codeListsQueries;

    private final ConceptConceptsQueries conceptConceptsQueries;

    private final BauhausIriFactory iriFactory;

    public StructureComponentRepository(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            BauhausLanguagesProperties languages,
            PublicationUtils publicationUtils,
            ComponentPublication componentPublication,
            StructureQueries structureQueries,
            CodeListsQueries codeListsQueries,
            ConceptConceptsQueries conceptConceptsQueries,
            BauhausIriFactory iriFactory) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.languages = languages;
        this.iriFactory = iriFactory;
        this.componentPublication = componentPublication;
        this.structureQueries = structureQueries;
        this.codeListsQueries = codeListsQueries;
        this.conceptConceptsQueries = conceptConceptsQueries;
    }

    public JSONObject formatComponent(String id, JSONObject response) throws RmesException {
        response.put(Constants.ID, id);
        addCodeListRange(response);
        addStructures(response, id);
        return response;
    }

    private void addStructures(JSONObject response, String id) throws RmesException {
        JSONArray structures = repoGestion.getResponseAsArray(structureQueries.getStructuresForComponent(id));
        response.put("structures", structures);
    }

    private void addCodeListRange(JSONObject response) {
        if (response.has(Constants.CODELIST)) {
            response.put("range", INSEE.CODELIST.stringValue());
        }
    }

    private String getValidationStatus(String id) throws RmesException {
        return repoGestion
                .getResponseAsObject(structureQueries.getValidationStatus(id))
                .getString("state");
    }

    public String updateComponent(String componentId, String body) throws RmesException {
        MutualizedComponent component;

        JSONObject jsonComponent = new JSONObject(body);

        try {
            component = deserializeBody(body);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }

        if (component.getId() == null || !component.getId().equals(componentId)) {
            throw new RmesBadRequestException(
                    "The id of the component should be the same as the one defined in the request");
        }

        validateComponent(component);

        component.setUpdated(DateUtils.getCurrentDate());
        String status = getValidationStatus(componentId);
        if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
            createRDFForComponent(component, ValidationStatus.UNPUBLISHED, jsonComponent);
        } else {
            createRDFForComponent(component, ValidationStatus.MODIFIED, jsonComponent);
        }

        return component.getId();
    }

    public String createComponent(String body) throws RmesException {
        MutualizedComponent component;

        JSONObject jsonComponent = new JSONObject(body);

        try {
            component = deserializeBody(body);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }
        return createComponent(component, jsonComponent);
    }

    public String createComponent(MutualizedComponent component, JSONObject jsonComponent) throws RmesException {
        if (component.getId() != null) {
            throw new RmesBadRequestException("During the creation of a new component, the id property should be null");
        }
        String id = generateNextId(component.getType());

        return createComponent(component, id, jsonComponent);
    }

    public String createComponent(MutualizedComponent component, String id, JSONObject jsonComponent)
            throws RmesException {
        validateComponent(component);

        component.setId(id);

        String currentDate = DateUtils.getCurrentDate();
        component.setCreated(currentDate);
        component.setUpdated(currentDate);
        createRDFForComponent(component, ValidationStatus.UNPUBLISHED, jsonComponent);
        return id;
    }

    private void createRDFForComponent(MutualizedComponent component, ValidationStatus status, JSONObject jsonComponent)
            throws RmesException {

        if (StringUtils.isNotEmpty(component.getConcept()) && StringUtils.isNotEmpty(component.getCodeList())) {
            boolean componentsWithSameCodelistAndConcept =
                    repoGestion.getResponseAsBoolean(structureQueries.checkUnicityMutualizedComponent(
                            component.getId(), component.getConcept(), component.getCodeList(), component.getType()));

            if (componentsWithSameCodelistAndConcept) {
                throw new RmesBadRequestException(
                        ErrorCodes.COMPONENT_UNICITY,
                        "A component with the same codes list and concept already exists",
                        "");
            }
        }

        String type = component.getType();
        if (type.equals(QB.ATTRIBUTE_PROPERTY.stringValue())) {
            createRDFForComponent(
                    component,
                    QB.ATTRIBUTE_PROPERTY,
                    iriFactory.structureComponentAttribute(component.getId()),
                    status,
                    jsonComponent);
        } else if (type.equals(QB.MEASURE_PROPERTY.stringValue())) {
            createRDFForComponent(
                    component,
                    QB.MEASURE_PROPERTY,
                    iriFactory.structureComponentMeasure(component.getId()),
                    status,
                    jsonComponent);
        } else {
            createRDFForComponent(
                    component,
                    QB.DIMENSION_PROPERTY,
                    iriFactory.structureComponentDimension(component.getId()),
                    status,
                    jsonComponent);
        }
    }

    private void createRDFForComponent(
            MutualizedComponent component,
            Resource resource,
            IRI componentURI,
            ValidationStatus status,
            JSONObject jsonComponent)
            throws RmesException {
        Model model = new LinkedHashModel();
        Resource graph = iriFactory.structureComponentGraph();

        /*Const*/
        model.add(componentURI, RDF.TYPE, resource, graph);

        /*Required*/
        model.add(componentURI, DCTERMS.IDENTIFIER, RdfTriples.string(component.getId()), graph);

        model.add(componentURI, RDFS.LABEL, RdfTriples.string(component.getLabelLg1(), languages.lg1()), graph);
        model.add(componentURI, RDFS.LABEL, RdfTriples.string(component.getLabelLg2(), languages.lg2()), graph);
        model.add(componentURI, SKOS.NOTATION, RdfTriples.string(component.getIdentifiant()), graph);
        model.add(componentURI, INSEE.VALIDATION_STATE, RdfTriples.string(status), graph);
        model.add(componentURI, DCTERMS.CREATED, RdfTriples.dateTime(component.getCreated()), graph);
        model.add(componentURI, DCTERMS.MODIFIED, RdfTriples.dateTime(component.getUpdated()), graph);

        RdfTriples.addString(componentURI, SKOS.ALT_LABEL, component.getAltLabelLg1(), languages.lg1(), model, graph);
        RdfTriples.addString(componentURI, SKOS.ALT_LABEL, component.getAltLabelLg2(), languages.lg2(), model, graph);
        RdfTriples.addUri(componentURI, DC.CREATOR, component.getCreator(), model, graph);

        component
                .getContributor()
                .forEach(contributor -> RdfTriples.addUri(componentURI, DC.CONTRIBUTOR, contributor, model, graph));

        RdfTriples.addUri(componentURI, INSEE.DISSEMINATIONSTATUS, component.getDisseminationStatus(), model, graph);

        jsonComponent.keySet().stream().forEach(key -> {
            if (key.startsWith("attribute_")) {
                String index = key.substring(key.indexOf("_") + 1);
                if (!jsonComponent.getString("attributeValue_" + index).isEmpty()) {
                    String predicate = jsonComponent.getString("attribute_" + index);
                    String value = jsonComponent.getString("attributeValue_" + index);
                    try {
                        RdfTriples.addUri(componentURI, RdfTriples.iri(predicate), value, model, graph);
                    } catch (Exception _) {
                        model.add(componentURI, RdfTriples.iri(predicate), RdfTriples.string(value), graph);
                    }
                }
            }
        });
        if (component.getConcept() != null) {
            RdfTriples.addUri(
                    componentURI, QB.CONCEPT, iriFactory.conceptBaseUri() + "/" + component.getConcept(), model, graph);
        }

        if (component.getRange() != null && component.getRange().equals(INSEE.CODELIST.stringValue())) {
            RdfTriples.addUri(componentURI, RDF.TYPE, QB.CODED_PROPERTY, model, graph);

            JSONObject object =
                    repoGestion.getResponseAsObject(structureQueries.getUriClasseOwl(component.getFullCodeListValue()));

            if (object.has("uriClasseOwl")) {
                RdfTriples.addUri(componentURI, RDFS.RANGE, object.getString("uriClasseOwl"), model, graph);
            } else {
                RdfTriples.addUri(componentURI, RDFS.RANGE, SKOS.CONCEPT, model, graph);
            }
        } else if (component.getRange() != null) {
            RdfTriples.addUri(componentURI, RDFS.RANGE, component.getRange(), model, graph);

            if (component.getRange().equals(XSD.DATE.stringValue())) {
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(PATTERN),
                        component.getPattern(),
                        languages.lg1(),
                        model,
                        graph);
            } else if (component.getRange().equals(XSD.DATETIME.stringValue())) {
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(PATTERN),
                        component.getPattern(),
                        languages.lg1(),
                        model,
                        graph);
            } else if (component.getRange().equals(XSD.INTEGER.stringValue())
                    || component.getRange().equals(XSD.DOUBLE.stringValue())) {
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(MIN_LENGTH),
                        component.getMinLength(),
                        languages.lg1(),
                        model,
                        graph);
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(MAX_LENGTH),
                        component.getMaxLength(),
                        languages.lg1(),
                        model,
                        graph);
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri("minInclusive"),
                        component.getMinInclusive(),
                        languages.lg1(),
                        model,
                        graph);
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri("maxInclusive"),
                        component.getMaxInclusive(),
                        languages.lg1(),
                        model,
                        graph);

            } else if (component.getRange().equals(XSD.STRING.stringValue())) {
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(MIN_LENGTH),
                        component.getMinLength(),
                        languages.lg1(),
                        model,
                        graph);
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(MAX_LENGTH),
                        component.getMaxLength(),
                        languages.lg1(),
                        model,
                        graph);
                RdfTriples.addString(
                        componentURI,
                        RdfTriples.xsdIri(PATTERN),
                        component.getPattern(),
                        languages.lg1(),
                        model,
                        graph);
            }
        }

        String codeListIri = (component.getCodeList() != null && !"".equals(component.getCodeList()))
                ? component.getCodeList()
                : component.getFullCodeListValue();
        RdfTriples.addUri(componentURI, QB.CODE_LIST, codeListIri, model, graph);
        RdfTriples.addString(componentURI, RDFS.COMMENT, component.getDescriptionLg1(), languages.lg1(), model, graph);
        RdfTriples.addString(componentURI, RDFS.COMMENT, component.getDescriptionLg2(), languages.lg2(), model, graph);

        repoGestion.loadSimpleObject(componentURI, model, null);
    }

    private String generateNextId(String type) throws RmesException {
        if (type.equals(QB.ATTRIBUTE_PROPERTY.stringValue())) {
            return generateNextId("a", QB.ATTRIBUTE_PROPERTY);
        }
        if (type.equals(QB.MEASURE_PROPERTY.stringValue())) {
            return generateNextId("m", QB.MEASURE_PROPERTY);
        }
        return generateNextId("d", QB.DIMENSION_PROPERTY);
    }

    private String generateNextId(String prefix, IRI type) throws RmesException {
        logger.info("Generate id for component");
        JSONObject json = repoGestion.getResponseAsObject(structureQueries.lastId(prefix, type.stringValue()));
        logger.debug("JSON when generating the id of a component : {}", json);
        if (json.isEmpty()) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (id.equals(Constants.UNDEFINED)) {
            return prefix + "1000";
        }
        return prefix + (Integer.parseInt(id) + 1);
    }

    private void validateComponent(MutualizedComponent component) throws RmesBadRequestException {
        if (component.getIdentifiant() == null) {
            throw new RmesBadRequestException("The property identifiant is required");
        }
        if (component.getLabelLg1() == null) {
            throw new RmesBadRequestException("The property labelLg1 is required");
        }
        if (component.getLabelLg2() == null) {
            throw new RmesBadRequestException("The property labelLg2 is required");
        }
        if (component.getType() == null) {
            throw new RmesBadRequestException("The property type is required");
        }
        if (!Arrays.asList(QB.getURIForComponent()).contains(component.getType())) {
            throw new RmesBadRequestException("The property type is not valid");
        }
    }

    private MutualizedComponent deserializeBody(String body) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(body, MutualizedComponent.class);
    }

    public void deleteComponent(JSONObject component, String id, String type) throws RmesException {
        String state = component.getString(Constants.VALIDATION_STATE);
        if (state.equals(VALIDATED) || state.equals(MODIFIED)) {
            throw new RmesException(
                    ErrorCodes.COMPONENT_FORBIDDEN_DELETE, "You cannot delete a validated component", new JSONArray());
        }
        JSONArray structures = component.getJSONArray("structures");

        boolean findPublishedStructure = JSONUtils.stream(structures)
                .map(structure -> structure.getString(Constants.VALIDATION_STATE))
                .anyMatch(stateStructure -> stateStructure.equals(VALIDATED) || stateStructure.equals(MODIFIED));

        if (findPublishedStructure) {
            throw new RmesException(
                    ErrorCodes.COMPONENT_FORBIDDEN_DELETE, "You cannot delete a validated component", new JSONArray());
        }
        IRI componentIri;
        if (type.equalsIgnoreCase(QB.ATTRIBUTE_PROPERTY.stringValue())) {
            componentIri = iriFactory.structureComponentAttribute(id);
        } else if (type.equalsIgnoreCase(QB.MEASURE_PROPERTY.stringValue())) {
            componentIri = iriFactory.structureComponentMeasure(id);
        } else {
            componentIri = iriFactory.structureComponentDimension(id);
        }
        repoGestion.deleteObject(componentIri, null);
    }

    public String publishComponent(JSONObject component) throws RmesException {
        PublicationUtils.rejectIfAlreadyPublished(
                "Component", component.optString(Constants.ID), component.optString(Constants.VALIDATION_STATE));

        if (jsonObjecthasPropertyNullOrEmpty(component, Constants.CREATOR)) {
            throw new RmesBadRequestException(
                    ErrorCodes.COMPONENT_PUBLICATION_EMPTY_CREATOR, "The creator should not be empty", new JSONArray());
        }

        if (jsonObjecthasPropertyNullOrEmpty(component, "disseminationStatus")) {
            throw new RmesBadRequestException(
                    ErrorCodes.COMPONENT_PUBLICATION_EMPTY_STATUS,
                    "The dissemination status should not be empty",
                    new JSONArray());
        }

        if (!jsonObjecthasPropertyNullOrEmpty(component, "concept")
                && !repoGestion.getResponseAsBoolean(
                        conceptConceptsQueries.isConceptValidated(component.getString(Constants.CONCEPT)))) {
            throw new RmesBadRequestException(
                    ErrorCodes.COMPONENT_PUBLICATION_VALIDATED_CONCEPT,
                    "The concept should be validated",
                    new JSONArray());
        }

        if (!jsonObjecthasPropertyNullOrEmpty(component, Constants.CODELIST)
                && !repoGestion.getResponseAsBoolean(
                        codeListsQueries.isCodesListValidated(component.getString(Constants.CODELIST)))) {
            throw new RmesBadRequestException(
                    ErrorCodes.COMPONENT_PUBLICATION_VALIDATED_CODESLIST,
                    "The codes list should be validated",
                    new JSONArray());
        }

        MutualizedComponent mutualizedComponent;
        try {
            mutualizedComponent = deserializeBody(component.toString());
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }
        mutualizedComponent.setUpdated(DateUtils.getCurrentDate());

        String type = component.getString("type");
        String id = component.getString(Constants.ID);

        if (type.equals(QB.ATTRIBUTE_PROPERTY.stringValue())) {
            componentPublication.publishComponent(iriFactory.structureComponentAttribute(id), QB.ATTRIBUTE_PROPERTY);
        } else if (type.equals(QB.MEASURE_PROPERTY.stringValue())) {
            componentPublication.publishComponent(iriFactory.structureComponentMeasure(id), QB.MEASURE_PROPERTY);
        } else if (type.equals(QB.DIMENSION_PROPERTY.stringValue())) {
            componentPublication.publishComponent(iriFactory.structureComponentDimension(id), QB.DIMENSION_PROPERTY);
        }

        createRDFForComponent(mutualizedComponent, ValidationStatus.VALIDATED, component);

        return id;
    }

    private boolean jsonObjecthasPropertyNullOrEmpty(JSONObject component, String property) {
        return component.isNull(property) || "".equals(component.getString(property));
    }
}
