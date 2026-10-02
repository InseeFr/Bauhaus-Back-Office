package fr.insee.rmes.bauhaus_services.operations.series;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.bauhaus_services.operations.series.validation.SeriesValidator;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.modules.operation.domain.event.BilingualLabel;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.modules.operations.series.domain.model.SeriesLink;
import fr.insee.rmes.modules.operations.series.domain.model.commands.SeriesCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.*;
import java.io.IOException;
import java.util.*;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@Repository
public class SeriesRepository {

    private static final String ID_SERIE = "idSerie";

    private static final String THEMES = "themes";

    final RepositoryGestion repositoryGestion;

    final CodeListService codeListService;

    final OrganizationsService organizationsService;

    private final OrganisationLookup organisationLookup;

    final OperationsObjectMapper operationsObjectMapper;

    final OperationsParentRepository operationsParentRepository;

    final SeriesPublication seriesPublication;

    private final DocumentationsUtils documentationsUtils;

    private static final Logger logger = LoggerFactory.getLogger(SeriesRepository.class);
    private final BauhausUriBuilder bauhausUriBuilder;
    private final BauhausLanguagesProperties languages;

    private final SeriesValidator validator;

    private final OperationSeriesQueries operationSeriesQueries;

    private final ApplicationEventPublisher events;

    public SeriesRepository(
            BauhausLanguagesProperties languages,
            RepositoryGestion repositoryGestion,
            CodeListService codeListService,
            OrganizationsService organizationsService,
            OperationsObjectMapper operationsObjectMapper,
            OperationsParentRepository operationsParentRepository,
            SeriesPublication seriesPublication,
            DocumentationsUtils documentationsUtils,
            BauhausUriBuilder bauhausUriBuilder,
            SeriesValidator validator,
            OperationSeriesQueries operationSeriesQueries,
            OrganisationLookup organisationLookup,
            ApplicationEventPublisher events) {
        this.languages = languages;
        this.repositoryGestion = repositoryGestion;
        this.codeListService = codeListService;
        this.organizationsService = organizationsService;
        this.operationsObjectMapper = operationsObjectMapper;
        this.operationsParentRepository = operationsParentRepository;
        this.seriesPublication = seriesPublication;
        this.documentationsUtils = documentationsUtils;
        this.bauhausUriBuilder = bauhausUriBuilder;
        this.validator = validator;
        this.operationSeriesQueries = operationSeriesQueries;
        this.organisationLookup = organisationLookup;
        this.events = events;
    }

    /*READ*/

    public IdLabelTwoLangs getSeriesLabelById(String id) throws RmesException {
        return operationsObjectMapper.buildIdLabelTwoLangsFromJson(getSeriesJsonById(id, EncodingType.MARKDOWN));
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
            id = operationsObjectMapper.createId();
        }
        Series series = new Series();
        try {
            if (EncodingType.XML.equals(encode))
                series = mapper.readValue(XMLUtils.solveSpecialXmlcharacters(seriesJson.toString()), Series.class);
            else series = mapper.readValue(seriesJson.toString(), Series.class);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Can't parse series", e.getMessage());
        }
        if (StringUtils.isEmpty(series.getId())) {
            series.id = id;
        }
        return series;
    }

    public JSONObject getSeriesJsonById(String id, EncodingType encode) throws RmesException {
        JSONObject series = repositoryGestion.getResponseAsObject(operationSeriesQueries.oneSeriesQuery(id));
        // check that the series exist
        if (JSONUtils.isEmpty(series)) {
            throw new RmesNotFoundException(
                    ErrorCodes.SERIES_UNKNOWN_ID, "Series not found", "The series " + id + " cannot be found.");
        }
        if (EncodingType.MARKDOWN.equals(encode)) {
            XhtmlToMarkdownUtils.convertJSONObject(series);
        }

        addSeriesOperations(id, series);
        addSeriesFamily(id, series);
        addSeriesLinks(id, series);
        addSeriesCreators(id, series);
        addSeriesThemes(id, series);
        addGeneratedWith(id, series);
        return series;
    }

    public String getSeriesForSearch(String stamp) throws RmesException {
        JSONArray resQuery = repositoryGestion.getResponseAsArray(operationSeriesQueries.getSeriesForSearch(stamp));
        JSONArray result = new JSONArray();
        Map<String, List<String>> creators = getAllSeriesCreators();
        Map<String, JSONArray> contribs = getOneTypeOfLink(DCTERMS.CONTRIBUTOR, Constants.ORGANIZATIONS);
        Map<String, JSONArray> dataCollectors = getOneTypeOfLink(INSEE.DATA_COLLECTOR, Constants.ORGANIZATIONS);
        Map<String, JSONArray> publishers = getOneTypeOfLink(DCTERMS.PUBLISHER, Constants.ORGANIZATIONS);
        JSONUtils.stream(resQuery).forEach(series -> {
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
            operationsObjectMapper.fixOrganizationsNames(series);
            result.put(series);
        });
        return QueryUtils.correctEmptyGroupConcat(result.toString());
    }

    private void addSeriesOperations(String idSeries, JSONObject series) throws RmesException {
        JSONArray operations = repositoryGestion.getResponseAsArray(operationSeriesQueries.getOperations(idSeries));
        if (!operations.isEmpty()) {
            series.put(Constants.OPERATIONS, operations);
        }
    }

    private void addGeneratedWith(String idSeries, JSONObject series) throws RmesException {
        JSONArray generated = repositoryGestion.getResponseAsArray(operationSeriesQueries.getGeneratedWith(idSeries));
        if (!generated.isEmpty()) {
            generated = QueryUtils.transformRdfTypeInString(generated);
            series.put("generate", generated);
        }
    }

    private void addSeriesFamily(String idSeries, JSONObject series) throws RmesException {
        JSONObject family = repositoryGestion.getResponseAsObject(operationSeriesQueries.getFamily(idSeries));
        series.put(Constants.FAMILY, family);
    }

    private void addSeriesLinks(String idSeries, JSONObject series) throws RmesException {
        addOneTypeOfLink(idSeries, series, DCTERMS.REPLACES, Constants.OPERATIONS);
        addOneTypeOfLink(idSeries, series, DCTERMS.IS_REPLACED_BY, Constants.OPERATIONS);
        addOneTypeOfLink(idSeries, series, RDFS.SEEALSO, Constants.OPERATIONS);
        addOneTypeOfLink(idSeries, series, DCTERMS.CONTRIBUTOR, Constants.ORGANIZATIONS);
        addOneTypeOfLink(idSeries, series, INSEE.DATA_COLLECTOR, Constants.ORGANIZATIONS);
        addOneTypeOfLink(idSeries, series, DCTERMS.PUBLISHER, Constants.ORGANIZATIONS);
        operationsObjectMapper.fixOrganizationsNames(series);
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

        JSONArray links =
                repositoryGestion.getResponseAsArray(operationSeriesQueries.seriesLinks(id, predicate, resultType));
        if (!links.isEmpty()) {
            links = QueryUtils.transformRdfTypeInString(links);
        }
        series.put(predicate.getLocalName(), links);
    }

    private Map<String, JSONArray> getOneTypeOfLink(IRI predicate, String resultType) throws RmesException {
        JSONArray links =
                repositoryGestion.getResponseAsArray(operationSeriesQueries.seriesLinks("", predicate, resultType));
        Map<String, JSONArray> map = new HashMap<>();

        if (!links.isEmpty()) {
            links = QueryUtils.transformRdfTypeInString(links);
            JSONUtils.stream(links).forEach(l -> {
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
            });
        }
        return map;
    }

    private void addSeriesCreators(String id, JSONObject series) throws RmesException {
        JSONArray creators = repositoryGestion.getResponseAsJSONList(operationSeriesQueries.getCreatorsById(id));
        series.put(Constants.CREATORS, creators);
    }

    private void addSeriesThemes(String id, JSONObject series) throws RmesException {
        String seriesIri = RdfUtils.objectIRI(ObjectType.SERIES, id).stringValue();
        series.put(
                THEMES,
                repositoryGestion.getResponseAsJSONList(operationSeriesQueries.getThemesBySeriesIri(seriesIri)));
    }

    private Map<String, List<String>> getAllSeriesCreators() throws RmesException {
        Map<String, List<String>> map = new HashMap<>();
        JSONArray creators = repositoryGestion.getResponseAsArray(operationSeriesQueries.getCreatorsById(""));
        if (!creators.isEmpty()) {
            JSONUtils.stream(creators).forEach(crea -> {
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
            });
        }
        return map;
    }

    public void addMulltiLangValues(
            Model model, IRI seriesIri, Resource graph, String valueLg1, String valueLg2, IRI predicate) {
        RdfUtils.addTripleStringMdToXhtml(seriesIri, predicate, valueLg1, languages.lg1(), model, graph);
        RdfUtils.addTripleStringMdToXhtml(seriesIri, predicate, valueLg2, languages.lg2(), model, graph);
    }

    void createRdfSeries(Series series, IRI familyURI, ValidationStatus newStatus) throws RmesException {
        this.validator.validate(series);

        Model model = new LinkedHashModel();
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, series.getId());
        /*Const*/
        model.add(seriesURI, RDF.TYPE, INSEE.SERIES, RdfUtils.operationsGraph());
        model.add(
                seriesURI, ADMS.HAS_IDENTIFIER, RdfUtils.setLiteralString(series.getId()), RdfUtils.operationsGraph());
        /*Required*/
        model.add(
                seriesURI,
                SKOS.PREF_LABEL,
                RdfUtils.setLiteralString(series.getPrefLabelLg1(), languages.lg1()),
                RdfUtils.operationsGraph());
        model.add(
                seriesURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(newStatus.toString()),
                RdfUtils.operationsGraph());
        /*Optional*/
        RdfUtils.addTripleString(
                seriesURI,
                SKOS.PREF_LABEL,
                series.getPrefLabelLg2(),
                languages.lg2(),
                model,
                RdfUtils.operationsGraph());
        RdfUtils.addTripleString(
                seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg1(), languages.lg1(), model, RdfUtils.operationsGraph());
        RdfUtils.addTripleString(
                seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg2(), languages.lg2(), model, RdfUtils.operationsGraph());
        RdfUtils.addTripleDateTime(seriesURI, DCTERMS.CREATED, series.getCreated(), model, RdfUtils.operationsGraph());
        RdfUtils.addTripleDateTime(seriesURI, DCTERMS.MODIFIED, series.getUpdated(), model, RdfUtils.operationsGraph());

        addMulltiLangValues(
                model,
                seriesURI,
                RdfUtils.operationsGraph(),
                series.getAbstractLg1(),
                series.getAbstractLg2(),
                DCTERMS.ABSTRACT);
        addMulltiLangValues(
                model,
                seriesURI,
                RdfUtils.operationsGraph(),
                series.getHistoryNoteLg1(),
                series.getHistoryNoteLg2(),
                SKOS.HISTORY_NOTE);

        addCreators(model, seriesURI, series.getCreators());

        // Thèmes, par dcterms:subject comme pour les familles (les jeux de données utilisent dcat:theme)
        Optional.ofNullable(series.getThemes())
                .ifPresent(themes -> themes.forEach(theme ->
                        RdfUtils.addTripleUri(seriesURI, DCTERMS.SUBJECT, theme, model, RdfUtils.operationsGraph())));

        // Organismes responsables
        addOperationLinksOrganization(series.getPublishers(), DCTERMS.PUBLISHER, model, seriesURI);

        // partenaires
        addOperationLinksOrganization(series.getContributors(), DCTERMS.CONTRIBUTOR, model, seriesURI);

        // Data_collector
        addOperationLinksOrganization(series.getDataCollectors(), INSEE.DATA_COLLECTOR, model, seriesURI);

        // Type
        addCodeList(series.getTypeList(), series.getTypeCode(), DCTERMS.TYPE, model, seriesURI);
        // PERIODICITY
        addCodeList(
                series.getAccrualPeriodicityList(),
                series.getAccrualPeriodicityCode(),
                DCTERMS.ACCRUAL_PERIODICITY,
                model,
                seriesURI);

        addOperationLinks(series.getSeeAlso(), RDFS.SEEALSO, model, seriesURI);

        List<OperationsLink> replaces = series.getReplaces();
        Optional.ofNullable(replaces).orElseGet(Collections::emptyList).stream()
                .filter(repl -> !repl.isEmpty())
                .forEach(replace -> {
                    String replUri = this.bauhausUriBuilder.getCompleteUriGestion(replace.getType(), replace.getId());
                    addReplacesAndReplacedBy(model, RdfUtils.toURI(replUri), seriesURI);
                });

        List<OperationsLink> isReplacedBys = series.getIsReplacedBy();
        Optional.ofNullable(isReplacedBys).orElseGet(Collections::emptyList).stream()
                .filter(isRepl -> !isRepl.isEmpty())
                .forEach(isRepl -> {
                    String isReplUri = this.bauhausUriBuilder.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
                    addReplacesAndReplacedBy(model, seriesURI, RdfUtils.toURI(isReplUri));
                });

        if (familyURI != null) {
            // case CREATION : link series to family
            RdfUtils.addTripleUri(seriesURI, DCTERMS.IS_PART_OF, familyURI, model, RdfUtils.operationsGraph());
            RdfUtils.addTripleUri(familyURI, DCTERMS.HAS_PART, seriesURI, model, RdfUtils.operationsGraph());
        }

        repositoryGestion.keepHierarchicalOperationLinks(seriesURI, model);

        repositoryGestion.loadObjectWithReplaceLinks(seriesURI, model);
    }

    void addCreators(Model model, IRI seriesURI, List<String> creators) {
        addCreators(model, seriesURI, creators, RdfUtils.operationsGraph());
    }

    void addCreators(Model model, IRI seriesURI, List<String> creators, Resource graph) {
        if (creators == null) {
            return;
        }
        for (String creatorIri : creators) {
            RdfUtils.addTripleUri(seriesURI, DC.CREATOR, creatorIri, model, graph);
        }
    }

    private void addReplacesAndReplacedBy(Model model, IRI previous, IRI next) {
        RdfUtils.addTripleUri(previous, DCTERMS.IS_REPLACED_BY, next, model, RdfUtils.operationsGraph());
        RdfUtils.addTripleUri(next, DCTERMS.REPLACES, previous, model, RdfUtils.operationsGraph());
    }

    private void addOperationLinks(List<OperationsLink> links, IRI predicate, Model model, IRI seriesURI) {
        if (links != null) {
            for (OperationsLink link : links) {
                if (!link.isEmpty()) {
                    String linkUri = this.bauhausUriBuilder.getCompleteUriGestion(link.getType(), link.getId());
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

    void addOperationLinksOrganization(List<OperationsLink> data, IRI predicate, Model model, IRI seriesURI)
            throws RmesException {
        addOperationLinksOrganization(data, predicate, model, seriesURI, RdfUtils.operationsGraph());
    }

    void addOperationLinksOrganization(
            List<OperationsLink> data, IRI predicate, Model model, IRI seriesURI, Resource graph) throws RmesException {
        if (data != null) {
            for (OperationsLink d : data) {
                if (!d.isEmpty()) {
                    Optional<String> resolved = organisationLookup.resolve(d.getId());
                    if (resolved.isPresent()) {
                        RdfUtils.addTripleUri(seriesURI, predicate, resolved.get(), model, graph);
                    }
                }
            }
        }
    }

    /** Création : l'identifiant est généré, le reste vient de la commande. */
    public String createSeries(SeriesCommand command) throws RmesException {
        Series series = toSeries(operationsObjectMapper.createId(), command);

        // Tester l'existence de la famille
        String idFamily = command.familyId();
        if (!operationsObjectMapper.checkIfObjectExists(ObjectType.FAMILY, idFamily)) {
            throw new RmesBadRequestException(
                    ErrorCodes.SERIES_UNKNOWN_FAMILY, "Unknown family: " + idFamily, new JSONArray());
        }

        IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, idFamily);
        series.setCreated(DateUtils.getCurrentDate());
        series.setUpdated(DateUtils.getCurrentDate());

        createRdfSeries(series, familyURI, ValidationStatus.UNPUBLISHED);
        logger.info("Create series : {} - {}", series.getId(), series.getPrefLabelLg1());
        publishSeriesSaved(series);

        return series.getId();
    }

    /** Mise à jour : l'identifiant vient de l'appelant, la commande réécrit la série entière. */
    public void setSeries(String id, SeriesCommand command) throws RmesException {
        Series series = toSeries(id, command);
        series.setUpdated(DateUtils.getCurrentDate());

        String status = operationsParentRepository.getFamOpSerValidationStatus(id);
        documentationsUtils.updateDocumentationTitle(
                series.getIdSims(), series.getPrefLabelLg1(), series.getPrefLabelLg2());
        if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
            createRdfSeries(series, null, ValidationStatus.UNPUBLISHED);
        } else {
            createRdfSeries(series, null, ValidationStatus.MODIFIED);
        }
        logger.info("Update series : {} - {}", series.getId(), series.getPrefLabelLg1());
        publishSeriesSaved(series);
    }

    /**
     * Projette la commande sur le modèle du dépôt. {@code created} (en création) et {@code updated}
     * sont posés par les appelants ci-dessus ; la famille n'est liée qu'à la création, par
     * {@code createRdfSeries}.
     */
    private static Series toSeries(String id, SeriesCommand command) {
        Series series = new Series();
        series.setId(id);
        series.setPrefLabelLg1(command.prefLabelLg1());
        series.setPrefLabelLg2(command.prefLabelLg2());
        series.setAltLabelLg1(command.altLabelLg1());
        series.setAltLabelLg2(command.altLabelLg2());
        series.setAbstractLg1(command.abstractLg1());
        series.setAbstractLg2(command.abstractLg2());
        series.setHistoryNoteLg1(command.historyNoteLg1());
        series.setHistoryNoteLg2(command.historyNoteLg2());
        series.setTypeCode(command.typeCode());
        series.setTypeList(command.typeList());
        series.setAccrualPeriodicityCode(command.accrualPeriodicityCode());
        series.setAccrualPeriodicityList(command.accrualPeriodicityList());
        series.setPublishers(toOperationsLinks(command.publishers()));
        series.setContributors(toOperationsLinks(command.contributors()));
        series.setDataCollectors(toOperationsLinks(command.dataCollectors()));
        series.setCreators(command.creators());
        series.setSeeAlso(toOperationsLinks(command.seeAlso()));
        series.setReplaces(toOperationsLinks(command.replaces()));
        series.setIsReplacedBy(toOperationsLinks(command.isReplacedBy()));
        series.setThemes(command.themes());
        series.setIdSims(command.idSims());
        series.setCreated(command.created());
        return series;
    }

    private static List<OperationsLink> toOperationsLinks(List<SeriesLink> links) {
        if (links == null) {
            return null;
        }
        return links.stream()
                .map(link -> OperationsLink.of(link.id(), link.type(), null, null))
                .toList();
    }

    /**
     * Signale que la série vient d'être écrite en RDF. L'événement porte l'IRI de publication,
     * identifiant canonique de la série hors de Bauhaus ; ce que ses consommateurs en font ne
     * regarde pas ce dépôt.
     */
    private void publishSeriesSaved(Series series) {
        events.publishEvent(new SeriesSaved(
                bauhausUriBuilder.getCompleteUriPublication(ObjectType.SERIES.labelType(), series.getId()),
                series.getId(),
                new BilingualLabel(series.getPrefLabelLg1(), series.getPrefLabelLg2()),
                new BilingualLabel(series.getAltLabelLg1(), series.getAltLabelLg2())));
    }

    public void setSeriesValidation(String id) throws RmesException {
        IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, id);

        Model model = new LinkedHashModel();
        JSONObject serieJson = getSeriesJsonById(id, EncodingType.XML);
        seriesPublication.publishSeries(id, serieJson);

        model.add(
                seriesURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.VALIDATED),
                RdfUtils.operationsGraph());
        model.remove(
                seriesURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
                RdfUtils.operationsGraph());
        model.remove(
                seriesURI,
                INSEE.VALIDATION_STATE,
                RdfUtils.setLiteralString(ValidationStatus.MODIFIED),
                RdfUtils.operationsGraph());
        logger.info("Validate series : {}", seriesURI);

        repositoryGestion.objectValidation(seriesURI, model);
    }

    public boolean isSeriesAndOperationsExist(List<String> iris) throws RmesException {
        var length = repositoryGestion
                .getResponseAsArray(operationSeriesQueries.checkIfSeriesExists(iris))
                .length();
        return length == iris.size();
    }
}
