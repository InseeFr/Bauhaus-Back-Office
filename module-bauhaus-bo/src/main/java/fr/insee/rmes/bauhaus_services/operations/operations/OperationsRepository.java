package fr.insee.rmes.bauhaus_services.operations.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.*;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class OperationsRepository extends RdfService {
    private final BauhausLanguagesProperties languages;

    static final Logger logger = LoggerFactory.getLogger(OperationsRepository.class);

    private final OperationsObjectMapper operationsObjectMapper;

    private final DocumentationsUtils documentationsUtils;

    private final OperationsParentRepository operationsParentRepository;

    private final OperationPublication operationPublication;

    private final OperationsOperationQueries operationsOperationQueries;

    private final OperationSeriesQueries operationSeriesQueries;

    public OperationsRepository(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            BauhausLanguagesProperties languages,
            PublicationUtils publicationUtils,
            OperationsObjectMapper operationsObjectMapper,
            DocumentationsUtils documentationsUtils,
            OperationsParentRepository operationsParentRepository,
            OperationPublication operationPublication,
            OperationsOperationQueries operationsOperationQueries,
            OperationSeriesQueries operationSeriesQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.languages = languages;
        this.operationsObjectMapper = operationsObjectMapper;
        this.documentationsUtils = documentationsUtils;
        this.operationsParentRepository = operationsParentRepository;
        this.operationPublication = operationPublication;
        this.operationsOperationQueries = operationsOperationQueries;
        this.operationSeriesQueries = operationSeriesQueries;
    }

    private void validate(Operation operation) throws RmesException {
        if (repoGestion.getResponseAsBoolean(operationsOperationQueries.checkPrefLabelUnicity(
                operation.getId(), operation.getPrefLabelLg1(), languages.lg1()))) {
            throw new RmesBadRequestException(
                    ErrorCodes.OPERATION_OPERATION_EXISTING_PREF_LABEL_LG1,
                    "This prefLabelLg1 is already used by another operation.");
        }
        if (repoGestion.getResponseAsBoolean(operationsOperationQueries.checkPrefLabelUnicity(
                operation.getId(), operation.getPrefLabelLg2(), languages.lg2()))) {
            throw new RmesBadRequestException(
                    ErrorCodes.OPERATION_OPERATION_EXISTING_PREF_LABEL_LG2,
                    "This prefLabelLg2 is already used by another operation.");
        }
    }

    public Operation getOperationById(String id) throws RmesException {
        return buildOperationFromJson(getOperationJsonById(id));
    }

    public JSONObject getOperationJsonById(String id) throws RmesException {
        JSONObject operation = repoGestion.getResponseAsObject(operationsOperationQueries.operationQuery(id));
        getOperationSeries(id, operation);
        return operation;
    }

    private void getOperationSeries(String id, JSONObject operation) throws RmesException {
        JSONObject series = repoGestion.getResponseAsObject(operationsOperationQueries.seriesQuery(id));
        JSONArray creators = repoGestion.getResponseAsJSONList(
                operationSeriesQueries.getCreatorsById(series.getString(Constants.ID)));
        series.put(Constants.CREATORS, creators);
        operation.put("series", series);
    }

    private Operation buildOperationFromJson(JSONObject operationJson) throws RmesException {
        Operation operation = Deserializer.deserializeJsonString(operationJson.toString(), Operation.class);
        IdLabelTwoLangs series =
                operationsObjectMapper.buildIdLabelTwoLangsFromJson(operationJson.getJSONObject("series"));
        operation.setSeries(series);
        return operation;
    }

    /**
     * CREATE
     * @param body
     * @return
     * @throws RmesException
     */
    public String setOperation(String body) throws RmesException {
        String id = operationsObjectMapper.createId();
        Operation operation = Deserializer.deserializeJsonString(body, Operation.class);
        operation.setId(id);
        // Tester l'existence de la série
        String idSeries = operation.getSeries().getId();
        if (!operationsObjectMapper.checkIfObjectExists(ObjectType.SERIES, idSeries)) {
            throw new RmesNotFoundException(ErrorCodes.OPERATION_UNKNOWN_SERIES, "Unknown series: ", idSeries);
        }
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, idSeries);

        operation.setCreated(DateUtils.getCurrentDate());
        operation.setModified(DateUtils.getCurrentDate());

        createRdfOperation(operation, seriesURI, ValidationStatus.UNPUBLISHED);
        logger.info("Create operation : {} - {}", operation.getId(), operation.getPrefLabelLg1());

        return operation.getId();
    }

    public void setOperation(String id, String body) throws RmesException {
        Operation operation = Operation.of(id);
        try {
            operation = Deserializer.deserializeJsonString(body, Operation.class);
        } catch (RmesException e) {
            logger.error(e.getMessage());
        }

        operation.setModified(DateUtils.getCurrentDate());

        String status = operationsParentRepository.getValidationStatus(id);
        documentationsUtils.updateDocumentationTitle(
                operation.getIdSims(), operation.getPrefLabelLg1(), operation.getPrefLabelLg2());
        if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
            createRdfOperation(operation, null, ValidationStatus.UNPUBLISHED);
        } else {
            createRdfOperation(operation, null, ValidationStatus.MODIFIED);
        }
        logger.info("Update operation : {} - {}", operation.getId(), operation.getPrefLabelLg1());
    }

    void createRdfOperation(Operation operation, IRI serieUri, ValidationStatus newStatus) throws RmesException {
        validate(operation);

        Model model = new LinkedHashModel();
        IRI operationURI = RdfUtils.objectIRI(ObjectType.OPERATION, operation.getId());
        /*Const*/
        model.add(operationURI, RDF.TYPE, INSEE.OPERATION, RdfUtils.operationsGraph());
        model.add(
                operationURI,
                ADMS.HAS_IDENTIFIER,
                RdfUtils.setLiteralString(operation.getId()),
                RdfUtils.operationsGraph());
        /*Required*/
        model.add(
                operationURI,
                SKOS.PREF_LABEL,
                RdfUtils.setLiteralString(operation.getPrefLabelLg1(), languages.lg1()),
                RdfUtils.operationsGraph());
        model.add(
                operationURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(newStatus.toString()),
                RdfUtils.operationsGraph());
        /*Optional*/
        RdfUtils.addTripleString(
                operationURI,
                SKOS.PREF_LABEL,
                operation.getPrefLabelLg2(),
                languages.lg2(),
                model,
                RdfUtils.operationsGraph());
        RdfUtils.addTripleString(
                operationURI,
                SKOS.ALT_LABEL,
                operation.getAltLabelLg1(),
                languages.lg1(),
                model,
                RdfUtils.operationsGraph());
        RdfUtils.addTripleString(
                operationURI,
                SKOS.ALT_LABEL,
                operation.getAltLabelLg2(),
                languages.lg2(),
                model,
                RdfUtils.operationsGraph());
        RdfUtils.addTripleDateTime(
                operationURI, DCTERMS.CREATED, operation.getCreated(), model, RdfUtils.operationsGraph());
        RdfUtils.addTripleDateTime(
                operationURI, DCTERMS.MODIFIED, operation.getModified(), model, RdfUtils.operationsGraph());

        if (operation.getYear() != null) {
            model.add(
                    operationURI,
                    DCTERMS.TEMPORAL,
                    RdfUtils.createLiteral(operation.getYear().toString(), XSD.GYEAR),
                    RdfUtils.operationsGraph());
        }

        if (serieUri != null) {
            // case CREATION : link operation to series
            RdfUtils.addTripleUri(operationURI, DCTERMS.IS_PART_OF, serieUri, model, RdfUtils.operationsGraph());
            RdfUtils.addTripleUri(serieUri, DCTERMS.HAS_PART, operationURI, model, RdfUtils.operationsGraph());
        }

        repoGestion.keepHierarchicalOperationLinks(operationURI, model);
        repoGestion.loadSimpleObject(operationURI, model);
    }

    public void setOperationValidation(String idOperation) throws RmesException {
        Model model = new LinkedHashModel();

        // PUBLISH
        JSONObject operationJson = getOperationJsonById(idOperation);
        operationPublication.publishOperation(idOperation, operationJson);

        // UPDATE GESTION TO MARK AS PUBLISHED
        IRI operationURI = RdfUtils.objectIRI(ObjectType.OPERATION, idOperation);
        model.add(
                operationURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.VALIDATED),
                RdfUtils.operationsGraph());
        model.remove(
                operationURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
                RdfUtils.operationsGraph());
        model.remove(
                operationURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.MODIFIED),
                RdfUtils.operationsGraph());
        logger.info("Validate operation : {}", operationURI);
        repoGestion.objectValidation(operationURI, model);
    }
}
