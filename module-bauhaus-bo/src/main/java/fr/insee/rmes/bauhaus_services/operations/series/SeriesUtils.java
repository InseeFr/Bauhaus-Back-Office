package fr.insee.rmes.bauhaus_services.operations.series;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.operations.series.validation.SeriesValidator;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.*;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.*;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.utils.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class SeriesUtils {

    private static final String ID_SERIE = "idSerie";

    final RepositoryGestion repositoryGestion;

    final CodeListService codeListService;

    final OrganizationsService organizationsService;

    final FamOpeSerIndUtils famOpeSerIndUtils;

    final ParentUtils ownersUtils;

    final SeriesPublication seriesPublication;

    private final DocumentationsUtils documentationsUtils;

    private static final Logger logger = LoggerFactory.getLogger(SeriesUtils.class);
    private final UriUtils uriUtils;
    private final String lg2;
    private final String lg1;
    private final boolean seriesRichTextNexStructure;

    private final SeriesValidator validator;

    public SeriesUtils(
            @Value("${fr.insee.rmes.bauhaus.feature-flipping.operations.series-rich-text-new-structure}") boolean seriesRichTextNexStructure,
            @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
            @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2,
            RepositoryGestion repositoryGestion,
            CodeListService codeListService,
            OrganizationsService organizationsService,
            FamOpeSerIndUtils famOpeSerIndUtils,
            ParentUtils ownersUtils,
            SeriesPublication seriesPublication,
            DocumentationsUtils documentationsUtils,
            UriUtils uriUtils,
            SeriesValidator validator) {
        this.seriesRichTextNexStructure = seriesRichTextNexStructure;
        this.lg1 = lg1;
        this.lg2 = lg2;
        this.repositoryGestion = repositoryGestion;
        this.codeListService = codeListService;
        this.organizationsService = organizationsService;
        this.famOpeSerIndUtils = famOpeSerIndUtils;
        this.ownersUtils = ownersUtils;
        this.seriesPublication = seriesPublication;
        this.documentationsUtils = documentationsUtils;
        this.uriUtils = uriUtils;
        this.validator = validator;
    }

    /*READ*/

    public IdLabelTwoLangs getSeriesLabelById(String id) throws RmesException {
        return famOpeSerIndUtils.buildIdLabelTwoLangsFromJson(getSeriesJsonById(id, EncodingType.MARKDOWN));
    }

    public Series getSeriesById(String id, EncodingType encode) throws RmesException {
        return buildSeriesFromJson(getSeriesJsonById(id, encode), encode);
    }

    private Series buildSeriesFromJson(JSONObject seriesJson, EncodingType encode) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        String id;
        if (seriesJson.has(Constants.ID) && !seriesJson.getString(Constants.ID).isEmpty()) {
            id = seriesJson.getString(Constants.ID);
        } else {
            id = famOpeSerIndUtils.createId();
        }
        Series series = new Series();
        try {
            if (EncodingType.XML.equals(encode))
                series = mapper.readValue(XMLUtils.solveSpecialXmlcharacters(seriesJson.toString()), Series.class);
            else series = mapper.readValue(seriesJson.toString(), Series.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (series.getId().isEmpty()) {
            series.id = id;
        }
        return series;
    }


    public JSONObject getSeriesJsonById(String id, EncodingType encode) throws RmesException {
        JSONObject series = repositoryGestion.getResponseAsObject(OpSeriesQueries.oneSeriesQuery(id, seriesRichTextNexStructure));
        // check that the series exist
        if (JSONUtils.isEmpty(series)) {
            throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID, "Series not found",
                    "The series " + id + " cannot be found.");
        }
        if (EncodingType.MARKDOWN.equals(encode)) {
            XhtmlToMarkdownUtils.convertJSONObject(series);
        }

        addSeriesOperations(id, series);
        addSeriesFamily(id, series);
        addSeriesLinks(id, series);
        addSeriesCreators(id, series);
        addGeneratedWith(id, series);
        return series;
    }


    public String getSeriesForSearch(String stamp) throws RmesException {
        JSONArray resQuery = repositoryGestion.getResponseAsArray(OpSeriesQueries.getSeriesForSearch(stamp));
        JSONArray result = new JSONArray();
        Map<String, List<String>> creators = getAllSeriesCreators();
        Map<String, JSONArray> contribs = getOneTypeOfLink(DCTERMS.CONTRIBUTOR, Constants.ORGANIZATIONS);
        Map<String, JSONArray> dataCollectors = getOneTypeOfLink(INSEE.DATA_COLLECTOR, Constants.ORGANIZATIONS);
        Map<String, JSONArray> publishers = getOneTypeOfLink(DCTERMS.PUBLISHER, Constants.ORGANIZATIONS);
        for (int i = 0; i < resQuery.length(); i++) {
            JSONObject series = resQuery.getJSONObject(i);
            String idSeries = series.get(Constants.ID).toString();
            if (series.has("hasCreator")) {
                series.put(Constants.CREATORS, creators.get(idSeries));
                series.remove("hasCreator");
            }
            if (series.has("hasContributor")) {
                series.put(DCTERMS.CONTRIBUTOR.getLocalName(), contribs.get(idSeries));
                series.remove("hasContributor");
            }
            if (series.has("hasDataCollector")) {
                series.put(INSEE.DATA_COLLECTOR.getLocalName(), dataCollectors.get(idSeries));
                series.remove("hasDataCollector");
            }
            if (series.has("hasPublisher")) {
                series.put(DCTERMS.PUBLISHER.getLocalName(), publishers.get(idSeries));
                series.remove("hasPublisher");
            }
            famOpeSerIndUtils.fixOrganizationsNames(series);
            result.put(series);
        }
        return QueryUtils.correctEmptyGroupConcat(result.toString());
    }

    private void addSeriesOperations(String idSeries, JSONObject series) throws RmesException {
        JSONArray operations = repositoryGestion.getResponseAsArray(OpSeriesQueries.getOperations(idSeries));
        if (!operations.isEmpty()) {
            series.put(Constants.OPERATIONS, operations);
        }
    }

    private void addGeneratedWith(String idSeries, JSONObject series) throws RmesException {
        JSONArray generated = repositoryGestion.getResponseAsArray(OpSeriesQueries.getGeneratedWith(idSeries));
        if (!generated.isEmpty()) {
            generated = QueryUtils.transformRdfTypeInString(generated);
            series.put("generate", generated);
        }
    }

    private void addSeriesFamily(String idSeries, JSONObject series) throws RmesException {
        JSONObject family = repositoryGestion.getResponseAsObject(OpSeriesQueries.getFamily(idSeries));
        series.put(Constants.FAMILY, family);
    }

    private void addSeriesLinks(String idSeries, JSONObject series) throws RmesException {
        addOneTypeOfLink(idSeries, series, DCTERMS.REPLACES, Constants.OPERATIONS);
        addOneTypeOfLink(idSeries, series, DCTERMS.IS_REPLACED_BY, Constants.OPERATIONS);
        addOneTypeOfLink(idSeries, series, RDFS.SEEALSO, Constants.OPERATIONS);
        addOneTypeOfLink(idSeries, series, DCTERMS.CONTRIBUTOR, Constants.ORGANIZATIONS);
        addOneTypeOfLink(idSeries, series, INSEE.DATA_COLLECTOR, Constants.ORGANIZATIONS);
        addOneTypeOfLink(idSeries, series, DCTERMS.PUBLISHER, Constants.ORGANIZATIONS);
        famOpeSerIndUtils.fixOrganizationsNames(series);
    }


    /**
     * Add to series the link of type "predicate".
     * Links can be multiple
     *
     * @param id
     * @param series
     * @param predicate
     * @throws RmesException
     */
    private void addOneTypeOfLink(String id, JSONObject series, IRI predicate, String resultType) throws RmesException {

        JSONArray links = repositoryGestion.getResponseAsArray(OpSeriesQueries.seriesLinks(id, predicate, resultType));
        if (!links.isEmpty()) {
            links = QueryUtils.transformRdfTypeInString(links);
        }
        series.put(predicate.getLocalName(), links);
    }

    private Map<String, JSONArray> getOneTypeOfLink(IRI predicate, String resultType) throws RmesException {
        JSONArray links = repositoryGestion.getResponseAsArray(OpSeriesQueries.seriesLinks("", predicate, resultType));
        Map<String, JSONArray> map = new HashMap<>();

        if (!links.isEmpty()) {
            links = QueryUtils.transformRdfTypeInString(links);
            for (int i = 0; i < links.length(); i++) {
                JSONObject l = links.getJSONObject(i);
                if (l.has(ID_SERIE)) {
                    String idSerie = l.getString(ID_SERIE);
                    l.remove(ID_SERIE);
                    JSONArray temp;
                    if (map.containsKey(idSerie)) {
                        temp = map.get(idSerie);
                    } else {
                        temp = new JSONArray();
                    }
                    temp.put(l);
                    map.put(idSerie, temp);
                }
            }
        }
        return map;
    }

    private void addSeriesCreators(String id, JSONObject series) throws RmesException {
        JSONArray creators = repositoryGestion.getResponseAsJSONList(OpSeriesQueries.getCreatorsById(id));
        series.put(Constants.CREATORS, creators);
    }

    private Map<String, List<String>> getAllSeriesCreators() throws RmesException {
        Map<String, List<String>> map = new HashMap<>();
        JSONArray creators = repositoryGestion.getResponseAsArray(OpSeriesQueries.getCreatorsById(""));
        if (!creators.isEmpty()) {
            for (int i = 0; i < creators.length(); i++) {
                JSONObject crea = creators.getJSONObject(i);
                if (crea.has(ID_SERIE)) {
                    String idSerie = crea.getString(ID_SERIE);
                    String creaUri = crea.getString(Constants.CREATORS);
                    List<String> temp;
                    if (map.containsKey(idSerie)) {
                        temp = map.get(idSerie);
                    } else {
                        temp = new ArrayList<>();
                    }
                    temp.add(creaUri);
                    map.put(idSerie, temp);
                }
            }
        }
        return map;
    }


    private void addNewSyntaxToMultiLangValues(IRI indicatorIRI, String value, String lang, Model model, Resource graph, IRI predicate) throws RmesException {
        IRI iri = RdfUtils.addTripleStringMdToXhtml2(indicatorIRI, predicate, value, lang, "resume", model, graph);
        if (iri != null) {
            repositoryGestion.deleteObject(iri, null);
        }
    }

    public void addMulltiLangValues(Model model, IRI seriesIri, Resource graph, String valueLg1, String valueLg2, IRI predicate) throws RmesException {
        RdfUtils.addTripleStringMdToXhtml(seriesIri, predicate, valueLg1, lg1, model, graph);
        RdfUtils.addTripleStringMdToXhtml(seriesIri, predicate, valueLg2, lg2, model, graph);

        if (seriesRichTextNexStructure) {
            addNewSyntaxToMultiLangValues(seriesIri, valueLg1, lg1, model, graph, predicate);
            addNewSyntaxToMultiLangValues(seriesIri, valueLg2, lg2, model, graph, predicate);
        }
    }

    private void createRdfSeries(Series series, IRI familyURI, ValidationStatus newStatus) throws RmesException {
        this.validator.validate(series);

        Model model = new LinkedHashModel();
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, series.getId());
        /*Const*/
        model.add(seriesURI, RDF.TYPE, INSEE.SERIES, RdfUtils.operationsGraph());
        /*Required*/
        model.add(seriesURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(series.getPrefLabelLg1(), lg1), RdfUtils.operationsGraph());
        model.add(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.operationsGraph());
        /*Optional*/
        RdfUtils.addTripleString(seriesURI, SKOS.PREF_LABEL, series.getPrefLabelLg2(), lg2, model, RdfUtils.operationsGraph());
        RdfUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg1(), lg1, model, RdfUtils.operationsGraph());
        RdfUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg2(), lg2, model, RdfUtils.operationsGraph());
        RdfUtils.addTripleDateTime(seriesURI, DCTERMS.CREATED, series.getCreated(), model, RdfUtils.operationsGraph());
        RdfUtils.addTripleDateTime(seriesURI, DCTERMS.MODIFIED, series.getUpdated(), model, RdfUtils.operationsGraph());


        addMulltiLangValues(model, seriesURI, RdfUtils.operationsGraph(), series.getAbstractLg1(), series.getAbstractLg2(), DCTERMS.ABSTRACT);
        addMulltiLangValues(model, seriesURI, RdfUtils.operationsGraph(), series.getHistoryNoteLg1(), series.getHistoryNoteLg2(), SKOS.HISTORY_NOTE);

        List<String> creators = series.getCreators();
        if (creators != null) {
            for (String creator : creators) {
                RdfUtils.addTripleString(seriesURI, DC.CREATOR, creator, model, RdfUtils.operationsGraph());
            }
        }

        //Organismes responsables
        addOperationLinksOrganization(series.getPublishers(), DCTERMS.PUBLISHER, model, seriesURI);

        //partenaires
        addOperationLinksOrganization(series.getContributors(), DCTERMS.CONTRIBUTOR, model, seriesURI);

        //Data_collector
        addOperationLinksOrganization(series.getDataCollectors(), INSEE.DATA_COLLECTOR, model, seriesURI);

        //Type
        addCodeList(series.getTypeList(), series.getTypeCode(), DCTERMS.TYPE, model, seriesURI);
        //PERIODICITY
        addCodeList(series.getAccrualPeriodicityList(), series.getAccrualPeriodicityCode(), DCTERMS.ACCRUAL_PERIODICITY, model, seriesURI);

        addOperationLinks(series.getSeeAlso(), RDFS.SEEALSO, model, seriesURI);

        List<OperationsLink> replaces = series.getReplaces();
        Optional.ofNullable(replaces)
                .orElseGet(Collections::emptyList).stream().filter(repl -> !repl.isEmpty()).forEach(replace -> {
                    String replUri = this.uriUtils.getCompleteUriGestion(replace.getType(), replace.getId());
                    addReplacesAndReplacedBy(model, RdfUtils.toURI(replUri), seriesURI);
                });


        List<OperationsLink> isReplacedBys = series.getIsReplacedBy();
        Optional.ofNullable(isReplacedBys)
                .orElseGet(Collections::emptyList).stream().filter(isRepl -> !isRepl.isEmpty()).forEach(isRepl -> {
                    String isReplUri = this.uriUtils.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
                    addReplacesAndReplacedBy(model, seriesURI, RdfUtils.toURI(isReplUri));
                });

        if (familyURI != null) {
            //case CREATION : link series to family
            RdfUtils.addTripleUri(seriesURI, DCTERMS.IS_PART_OF, familyURI, model, RdfUtils.operationsGraph());
            RdfUtils.addTripleUri(familyURI, DCTERMS.HAS_PART, seriesURI, model, RdfUtils.operationsGraph());
        }

        repositoryGestion.keepHierarchicalOperationLinks(seriesURI, model);

        repositoryGestion.loadObjectWithReplaceLinks(seriesURI, model);
    }

    private void addReplacesAndReplacedBy(Model model, IRI previous, IRI next) {
        RdfUtils.addTripleUri(previous, DCTERMS.IS_REPLACED_BY, next, model, RdfUtils.operationsGraph());
        RdfUtils.addTripleUri(next, DCTERMS.REPLACES, previous, model, RdfUtils.operationsGraph());
    }

    private void addOperationLinks(List<OperationsLink> links, IRI predicate, Model model, IRI seriesURI) {
        if (links != null) {
            for (OperationsLink link : links) {
                if (!link.isEmpty()) {
                    String linkUri = this.uriUtils.getCompleteUriGestion(link.getType(), link.getId());
                    RdfUtils.addTripleUri(seriesURI, predicate, linkUri, model, RdfUtils.operationsGraph());
                }
            }
        }
    }

    private void addCodeList(String list, String code, IRI predicate, Model model, IRI seriesURI) throws RmesException {
        if (!StringUtils.isEmpty(list) && !StringUtils.isEmpty(code)) {
            String uri = codeListService.getCodeUri(list, code);
            RdfUtils.addTripleUri(seriesURI, predicate, uri, model, RdfUtils.operationsGraph());
        }
    }

    private void addOperationLinksOrganization(List<OperationsLink> data, IRI predicate, Model model, IRI seriesURI)
            throws RmesException {
        if (data != null) {
            for (OperationsLink d : data) {
                if (!d.isEmpty()) {
                    RdfUtils.addTripleUri(seriesURI, predicate,
                            //			d.getId(),
                            organizationsService.getOrganizationUriById(d.getId()),
                            model, RdfUtils.operationsGraph());
                }
            }
        }
    }

    public String createSeries(String body) throws RmesException {

        Series series = buildSeriesFromJson(new JSONObject(body), EncodingType.MARKDOWN);
        checkSimsWithOperations(series);

        // Tester l'existence de la famille
        String idFamily = series.getFamily().getId();
        if (!famOpeSerIndUtils.checkIfObjectExists(ObjectType.FAMILY, idFamily)) {
            throw new RmesBadRequestException(ErrorCodes.SERIES_UNKNOWN_FAMILY, "Unknown family: " + idFamily, new JSONArray());
        }

        IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, idFamily);
        series.setCreated(DateUtils.getCurrentDate());
        series.setUpdated(DateUtils.getCurrentDate());

        createRdfSeries(series, familyURI, ValidationStatus.UNPUBLISHED);
        logger.info("Create series : {} - {}", series.getId(), series.getPrefLabelLg1());

        return series.getId();
    }

    /**
     * Une série ne peut avoir un Sims que si elle n'a pas d'opération
     *
     * @param series
     * @throws RmesNotAcceptableException
     */
    private void checkSimsWithOperations(Series series) throws RmesNotAcceptableException {
        if (series.getIdSims() != null && !series.getIdSims().isEmpty() && series.getOperations() != null
                && !series.getOperations().isEmpty()) {
            throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,
                    "A series cannot have both a Sims and Operation(s)",
                    series.getPrefLabelLg1() + " " + series.getPrefLabelLg2());
        }
    }

    /* Update Series */
    public void setSeries(String id, String body) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        Series series = new Series();
        try {
            series = mapper.readerForUpdating(series).readValue(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Can't parse series", e.getMessage());
        }

        checkSimsWithOperations(series);

        series.setUpdated(DateUtils.getCurrentDate());

        String status = ownersUtils.getFamOpSerValidationStatus(id);
        documentationsUtils.updateDocumentationTitle(series.getIdSims(), series.getPrefLabelLg1(), series.getPrefLabelLg2());
        if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
            createRdfSeries(series, null, ValidationStatus.UNPUBLISHED);
        } else {
            createRdfSeries(series, null, ValidationStatus.MODIFIED);
        }
        logger.info("Update series : {} - {}", series.getId(), series.getPrefLabelLg1());
    }

    public void setSeriesValidation(String id) throws RmesException {
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, id);

        Model model = new LinkedHashModel();
        JSONObject serieJson = getSeriesJsonById(id, EncodingType.XML);
        seriesPublication.publishSeries(id, serieJson);

        model.add(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED),
                RdfUtils.operationsGraph());
        model.remove(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
                RdfUtils.operationsGraph());
        model.remove(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED),
                RdfUtils.operationsGraph());
        logger.info("Validate series : {}", seriesURI);

        repositoryGestion.objectValidation(seriesURI, model);
    }

    public boolean isSeriesAndOperationsExist(List<String> iris) throws RmesException {
        var length = repositoryGestion.getResponseAsArray(OpSeriesQueries.checkIfSeriesExists(iris)).length();
        return length == iris.size();
    }

}
