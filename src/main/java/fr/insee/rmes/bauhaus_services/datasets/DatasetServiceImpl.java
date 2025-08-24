package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.*;
import fr.insee.rmes.persistance.ontologies.ADMS;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.JSONUtils;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

import static fr.insee.rmes.exceptions.ErrorCodes.DATASET_PATCH_INCORRECT_BODY;

@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {

    public static final String CONTRIBUTOR = "contributor";
    private static final Pattern ALT_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");

    public static final String THEME = "theme";
    public static final String CATALOG_RECORD_CREATOR = "catalogRecordCreator";

    public static final String CATALOG_RECORD_CREATED = "catalogRecordCreated";
    public static final String CATALOG_RECORD_UPDATED = "catalogRecordUpdated";
    public static final String CREATOR = "creator";

    @Autowired
    SeriesUtils seriesUtils;

    @Value("${fr.insee.rmes.bauhaus.datasets.graph}")
    private String datasetsGraphSuffix;

    @Value("${fr.insee.rmes.bauhaus.datasets.baseURI}")
    private String datasetsBaseUriSuffix;

    @Value("${fr.insee.rmes.bauhaus.datasets.record.baseURI}")
    private String datasetsRecordBaseUriSuffix;

    @Value("${fr.insee.rmes.bauhaus.baseGraph}")
    private String baseGraph;

    @Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseURI}")
    private String baseUriGestion;

    @Value("${fr.insee.rmes.bauhaus.distribution.baseURI}")
    private String distributionsBaseUriSuffix;

    @Value("${fr.insee.rmes.bauhaus.adms.graph}")
    private String admsGraphSuffix;

    @Value("${fr.insee.rmes.bauhaus.adms.identifiantsAlternatifs.baseURI}")
    private String identifiantsAlternatifsBaseUri;

    private String getDatasetsGraph(){
        return baseGraph + datasetsGraphSuffix;
    }

    private String getAdmsGraph(){
        return baseGraph + admsGraphSuffix;
    }

    private String getDistributionBaseUri(){
        return baseUriGestion + distributionsBaseUriSuffix;
    }

    protected String getDatasetsBaseUri(){
        return baseUriGestion + datasetsBaseUriSuffix;
    }

    protected IRI getDatasetIri(String datasetId){
        return RdfUtils.createIRI(getDatasetsBaseUri() + "/" + datasetId);
    }

    private String getDatasetsAdmsBaseUri(){
        return baseUriGestion + identifiantsAlternatifsBaseUri;
    }

    private String getCatalogRecordBaseUri(){
        return baseUriGestion + datasetsRecordBaseUriSuffix;
    }

    @Override
    public List<PartialDataset> getDatasets() throws RmesException {
        return this.getDatasets(null);
    }

    @Override
    public List<PartialDataset> getDatasetsForDistributionCreation(String stamp) throws RmesException {
        return this.getDatasets(stamp);
    }

    @Override
    public String publishDataset(String id) throws RmesException {
        Model model = new LinkedHashModel();
        IRI iri = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + id);
        IRI catalogRecordIri = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + id);

        publicationUtils.publishResource(iri, Set.of("processStep", "archiveUnit", "validationState"));
        publicationUtils.publishResource(catalogRecordIri, Set.of(CREATOR, CONTRIBUTOR));
        model.add(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.createIRI(getDatasetsGraph()));
        model.remove(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.createIRI(getDatasetsGraph()));
        model.remove(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.createIRI(getDatasetsGraph()));

        repoGestion.objectValidation(iri, model);

        return id;
    }

    private List<PartialDataset> getDatasets(String stamp) throws RmesException {
        var datasets = this.repoGestion.getResponseAsArray(DatasetQueries.getDatasets(getDatasetsGraph(), stamp));
        return DiacriticSorter.sort(datasets,
                PartialDataset[].class,
                PartialDataset::label);
    }

    @Override
    public List<DatasetsForSearch> getDatasetsForSearch() throws RmesException {
        var datasets = this.repoGestion.getResponseAsArray(DatasetQueries.getDatasetsForSearch(getDatasetsGraph()));
        return DiacriticSorter.sort(datasets,
                DatasetsForSearch[].class,
                DatasetsForSearch::labelLg1);
    }

    @Override
    public Dataset getDatasetByID(String id) throws RmesException {
        JSONArray datasetWithThemes =  this.repoGestion.getResponseAsArray(DatasetQueries.getDataset(id, getDatasetsGraph(), getAdmsGraph()));

        if(datasetWithThemes.isEmpty()){
            throw new RmesNotFoundException("This dataset does not exist");
        }

        JSONObject dataset = datasetWithThemes.getJSONObject(0);
        Set<String> themes = new HashSet<>();
        for (int i = 0; i < datasetWithThemes.length(); i++) {
            JSONObject tempDataset = datasetWithThemes.getJSONObject(i);
            if (tempDataset.has(THEME)) {
                themes.add(tempDataset.getString(THEME));
            }
        }
        dataset.put("themes", themes);
        dataset.remove(THEME);

        getMultipleTripletsForObject(dataset, "creators", DatasetQueries.getDatasetCreators(id, getDatasetsGraph()), CREATOR);
        getMultipleTripletsForObject(dataset, "wasGeneratedIRIs", DatasetQueries.getDatasetWasGeneratedIris(id, getDatasetsGraph()), "iri");
        IRI catalogRecordIRI = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + id);
        getMultipleTripletsForObject(dataset, "spacialResolutions", DatasetQueries.getDatasetSpacialResolutions(id, getDatasetsGraph()), "spacialResolution");
        getMultipleTripletsForObject(dataset, "statisticalUnit", DatasetQueries.getDatasetStatisticalUnits(id, getDatasetsGraph()), "statisticalUnit");
        getMultipleTripletsForObject(dataset, "linkedDocuments", DatasetQueries.getLinkedDocuments(id, getDatasetsGraph()), "linkedDocument");
        addKeywordsToDataset(id, dataset);


        JSONObject catalogRecord = new JSONObject();
        getMultipleTripletsForObject(catalogRecord, CONTRIBUTOR, DatasetQueries.getDatasetContributors(catalogRecordIRI, getDatasetsGraph()), CONTRIBUTOR);

        if(dataset.has(CATALOG_RECORD_CREATOR)){
            catalogRecord.put(CREATOR, dataset.getString(CATALOG_RECORD_CREATOR));
            dataset.remove(CATALOG_RECORD_CREATOR);
        }

        if(dataset.has(CATALOG_RECORD_CREATED)){
            catalogRecord.put("created", dataset.getString(CATALOG_RECORD_CREATED));
            dataset.remove(CATALOG_RECORD_CREATED);
        }
        if(dataset.has(CATALOG_RECORD_UPDATED)){
            catalogRecord.put("updated", dataset.getString(CATALOG_RECORD_UPDATED));
            dataset.remove(CATALOG_RECORD_UPDATED);
        }
        dataset.put("catalogRecord", catalogRecord);
        return Deserializer.deserializeJSONObject(dataset, Dataset.class);
    }

    private void addKeywordsToDataset(String id, JSONObject dataset) throws RmesException {
        JSONArray keywords = this.repoGestion.getResponseAsArray(DatasetQueries.getKeywords(id, getDatasetsGraph()));



        List<String> lg1 = new ArrayList<>();
        List<String> lg2 = new ArrayList<>();

        if(keywords != null){
            keywords.forEach(k -> {
                JSONObject keyword = (JSONObject) k;
                if(keyword.getString("lang").equalsIgnoreCase(config.getLg1())){
                    lg1.add(keyword.getString("keyword"));
                }
                if(keyword.getString("lang").equalsIgnoreCase(config.getLg2())){
                    lg2.add(keyword.getString("keyword"));
                }
            });
        }


        JSONObject formattedKeywords = new JSONObject();
        formattedKeywords.put("lg1", lg1);
        formattedKeywords.put("lg2", lg2);
        dataset.put("keywords", formattedKeywords);
    }

    private String update(String datasetId, Dataset dataset) throws RmesException {
        dataset.setId(datasetId);

        if(ValidationStatus.VALIDATED.toString().equalsIgnoreCase(dataset.getValidationState())){
            dataset.setValidationState(ValidationStatus.MODIFIED.toString());
        }

        if(dataset.getCatalogRecord() == null){
            dataset.setCatalogRecord(new CatalogRecord());
        }

        this.validate(dataset);


        dataset.getCatalogRecord().setUpdated(DateUtils.getCurrentDate());

        return this.persist(dataset);
    }
    @Override
    public String update(String datasetId, String body) throws RmesException {
        Dataset dataset = Deserializer.deserializeJsonString(body, Dataset.class);
        return this.update(datasetId, dataset);
    }

    @Override
    public String create(String body) throws RmesException {
        Dataset dataset = Deserializer.deserializeJsonString(body, Dataset.class);
        dataset.setId(idGenerator.generateNextId());
        dataset.setValidationState(ValidationStatus.UNPUBLISHED.toString());

        if(dataset.getCatalogRecord() == null){
            dataset.setCatalogRecord(new CatalogRecord());
        }

        this.validate(dataset);

        dataset.getCatalogRecord().setCreated(DateUtils.getCurrentDate());
        dataset.getCatalogRecord().setUpdated(dataset.getCatalogRecord().getCreated());

        return this.persist(dataset);
    }

    @Override
    public String getDistributions(String id) throws RmesException {
        return this.repoGestion.getResponseAsArray(DistributionQueries.getDatasetDistributions(id, getDatasetsGraph())).toString();
    }

    @Override
    public String getArchivageUnits() throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getArchivageUnits()).toString();
    }

    @Override
    public void patchDataset(String datasetId, PatchDataset patchDataset) throws RmesException {
        Dataset dataset = getDatasetByID(datasetId);
        if  (patchDataset.updated() == null && patchDataset.issued() == null && patchDataset.numObservations() == null
                && patchDataset.numSeries() == null && patchDataset.temporal() == null){
            throw new RmesBadRequestException(DATASET_PATCH_INCORRECT_BODY,"One of these attributes is required : updated, issued, numObservations, numSeries, temporal");
        }

        if ( patchDataset.issued() != null){
            dataset.setIssued(patchDataset.issued());
        }

        if ( patchDataset.updated() != null){
            dataset.setUpdated(patchDataset.updated());
        }

        if ( patchDataset.temporal() != null){
            String temporalCoverageStartDate = patchDataset.temporal().startPeriod();
            String temporalCoverageEndDate = patchDataset.temporal().endPeriod();
            dataset.setTemporalCoverageStartDate(temporalCoverageStartDate);
            dataset.setTemporalCoverageStartDate(temporalCoverageEndDate);
        }

        if ( patchDataset.numObservations() != null && patchDataset.numObservations() > 0){
            dataset.setObservationNumber(patchDataset.numObservations());
        }

        if ( patchDataset.numSeries() != null){
            dataset.setTimeSeriesNumber(patchDataset.numSeries());
        }

        update(datasetId, dataset);
    }

    @Override
    public void deleteDatasetId(String datasetId) throws RmesException{
        Dataset dataset = getDatasetByID(datasetId);
        if (isPublished(dataset)){
             throw new RmesBadRequestException(ErrorCodes.DATASET_DELETE_ONLY_UNPUBLISHED, "Only unpublished datasets can be deleted");
        }

        if (hasDistribution(dataset)) {
            throw new RmesBadRequestException(ErrorCodes.DATASET_DELETE_ONLY_WITHOUT_DISTRIBUTION, "Only dataset without any distribution can be deleted");
        }

        if (hasDerivedDataset(dataset)) {
            throw new RmesBadRequestException(ErrorCodes.DATASET_DELETE_ONLY_WITHOUT_DERIVED_DATASET, "Only dataset without any derived dataset can be deleted");
        }

        IRI datasetIRI = RdfUtils.createIRI(getDatasetsBaseUri());
        IRI graph = getDatasetIri(datasetId);
        String datasetURI = getDatasetsBaseUri() + "/" + datasetId;
        IRI catalogRecordIRI = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + datasetId);
        IRI datasetAdmsIri = RdfUtils.createIRI(getDatasetsAdmsBaseUri() + "/" + datasetId);

        if (hasTemporalCoverage(dataset)){
            deleteTemporalWhiteNode(datasetId);
        }

        if (isDerivedFromADataset(dataset)){
            deleteQualifiedDerivationWhiteNode(datasetId);
        }
        repoGestion.deleteObject(RdfUtils.toURI(datasetURI));
        repoGestion.deleteObject(catalogRecordIRI);
        repoGestion.deleteObject(datasetAdmsIri);
        repoGestion.deleteTripletByPredicate(datasetIRI, DCAT.DATASET, graph);
    }


    private boolean isPublished(Dataset dataset) {
        return !"Unpublished".equalsIgnoreCase(dataset.getValidationState());
    }

    private boolean hasDistribution(Dataset dataset) throws RmesException {
        String datasetId = dataset.getId();
        return !getDistributions(datasetId).equals("[]");
    }

    private boolean hasDerivedDataset(Dataset dataset) throws RmesException {
        String datasetId = dataset.getId();
        JSONObject datasetDerivation =  this.repoGestion.getResponseAsObject(DatasetQueries.getDerivedDataset(datasetId, getDatasetsGraph()));
        return (datasetDerivation.has("id"));
    }

    private boolean hasTemporalCoverage(Dataset dataset) {
        return (dataset.getTemporalCoverageDataType() != null);
    }

    private void deleteTemporalWhiteNode(String id) throws RmesException {
        repoGestion.executeUpdate(DatasetQueries.deleteTempWhiteNode(id, getDatasetsGraph()));
    }

    private boolean isDerivedFromADataset(Dataset dataset) throws RmesException {
        String datasetId = dataset.getId();
        JSONObject datasetDerivedFrom =  this.repoGestion.getResponseAsObject(DatasetQueries.getDatasetDerivedFrom(datasetId, getDatasetsGraph()));
        return (!datasetDerivedFrom.optString("wasDerivedFromS").isEmpty());
    }

    private void deleteQualifiedDerivationWhiteNode(String id) throws RmesException {
        repoGestion.executeUpdate(DatasetQueries.deleteDatasetQualifiedDerivationWhiteNode(id, getDatasetsGraph()));
    }

    private void persistCatalogRecord(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDatasetsGraph());
        IRI catalogRecordIRI = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + dataset.getId());
        IRI datasetIri = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + dataset.getId());

        Model model = new LinkedHashModel();

        CatalogRecord catalogRecord = dataset.getCatalogRecord();

        RdfUtils.addTripleUri(catalogRecordIRI, FOAF.PRIMARY_TOPIC, datasetIri, model, graph);

        model.add(catalogRecordIRI, RDF.TYPE, DCAT.CATALOG_RECORD, graph);
        model.add(catalogRecordIRI, DC.CREATOR, RdfUtils.setLiteralString(catalogRecord.getCreator()), graph);

        catalogRecord.getContributor().forEach(contributor -> model.add(catalogRecordIRI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(contributor), graph));
        RdfUtils.addTripleDateTime(catalogRecordIRI, DCTERMS.CREATED, catalogRecord.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(catalogRecordIRI, DCTERMS.MODIFIED, catalogRecord.getUpdated(), model, graph);

        repoGestion.loadSimpleObject(catalogRecordIRI, model, null);

    }

    private void persistGeneralInformations(IRI datasetIri, Dataset dataset, Model model, Resource graph){
        model.add(datasetIri, DCTERMS.TITLE, RdfUtils.setLiteralString(dataset.getLabelLg1(), config.getLg1()), graph);
        model.add(datasetIri, DCTERMS.TITLE, RdfUtils.setLiteralString(dataset.getLabelLg2(), config.getLg2()), graph);
        RdfUtils.addTripleString(datasetIri, INSEE.SUBTITLE, dataset.getSubTitleLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, INSEE.SUBTITLE, dataset.getSubTitleLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleUri(datasetIri, DCTERMS.ACCRUAL_PERIODICITY, dataset.getAccrualPeriodicity(), model, graph);
        RdfUtils.addTripleUri(datasetIri, DCTERMS.ACCESS_RIGHTS, dataset.getAccessRights(), model, graph);
        RdfUtils.addTripleUri(datasetIri, INSEE.CONFIDENTIALITY_STATUS, dataset.getConfidentialityStatus(), model, graph);

        if(dataset.getCreators() != null){
            dataset.getCreators().forEach(creator -> RdfUtils.addTripleUri(datasetIri, DCTERMS.CREATOR, creator, model, graph));
        }

        RdfUtils.addTripleUri(datasetIri, DCTERMS.PUBLISHER, dataset.getPublisher(), model, graph);

        RdfUtils.addTripleString(datasetIri, DCAT.LANDING_PAGE, dataset.getLandingPageLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCAT.LANDING_PAGE, dataset.getLandingPageLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleDateTime(datasetIri, DCTERMS.MODIFIED, dataset.getUpdated(), model, graph);
        RdfUtils.addTripleDateTime(datasetIri, DCTERMS.ISSUED, dataset.getIssued(), model, graph);

    }

    private void persistInternalManagment(IRI datasetIri, Dataset dataset, Model model, Resource graph){
        RdfUtils.addTripleUri(datasetIri, INSEE.DISSEMINATIONSTATUS, dataset.getDisseminationStatus(), model, graph);
        RdfUtils.addTripleUri(datasetIri, INSEE.PROCESS_STEP, dataset.getProcessStep(), model, graph);
        RdfUtils.addTripleUri(datasetIri, INSEE.ARCHIVE_UNIT, dataset.getArchiveUnit(), model, graph);

        if(dataset.getAltIdentifier() != null){
            Resource admsGraph = RdfUtils.createIRI(getAdmsGraph());
            IRI datasetAdmsIri = RdfUtils.createIRI(getDatasetsAdmsBaseUri() + "/" + dataset.getId());
            RdfUtils.addTripleUri(datasetIri, ADMS.HAS_IDENTIFIER, datasetAdmsIri, model, graph);
            RdfUtils.addTripleUri(datasetAdmsIri, RDF.TYPE, ADMS.IDENTIFIER, model, admsGraph);
            RdfUtils.addTripleString(datasetAdmsIri, SKOS.NOTATION, dataset.getAltIdentifier(), model, admsGraph);
        }

    }

    private void persistNotes(IRI datasetIri, Dataset dataset, Model model, Resource graph){
        RdfUtils.addTripleString(datasetIri, DCTERMS.DESCRIPTION, dataset.getDescriptionLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCTERMS.DESCRIPTION, dataset.getDescriptionLg2(), config.getLg2(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCTERMS.ABSTRACT, dataset.getAbstractLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCTERMS.ABSTRACT, dataset.getAbstractLg2(), config.getLg2(), model, graph);
        RdfUtils.addTripleString(datasetIri, SKOS.SCOPE_NOTE, dataset.getCautionLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, SKOS.SCOPE_NOTE, dataset.getCautionLg2(), config.getLg2(), model, graph);
    }

    private void persistStatisticsInformations(IRI datasetIri, Dataset dataset, Model model, Resource graph){
        RdfUtils.addTripleUri(datasetIri, DCTERMS.TYPE, dataset.getType(), model, graph);

        if(dataset.getStatisticalUnit() != null){
            dataset.getStatisticalUnit().forEach(statisticalUnit -> RdfUtils.addTripleUri(datasetIri, INSEE.STATISTICAL_UNIT, statisticalUnit, model, graph));
        }
        RdfUtils.addTripleUri(datasetIri, INSEE.STRUCTURE, dataset.getDataStructure(), model, graph);
        if(dataset.getObservationNumber() != null){
            RdfUtils.addTripleInt(datasetIri, INSEE.NUM_OBSERVATIONS, dataset.getObservationNumber().toString(), model, graph);
        }
        if(dataset.getTimeSeriesNumber() != null){
            RdfUtils.addTripleInt(datasetIri, RdfUtils.createIRI("http://data.europa.eu/m8g/numSeries"), dataset.getTimeSeriesNumber().toString(), model, graph);
        }
        RdfUtils.addTripleUri(datasetIri, DCTERMS.SPATIAL, dataset.getSpacialCoverage(), model, graph);
        RdfUtils.addTripleDate(datasetIri, INSEE.SPATIAL_TEMPORAL, dataset.getSpacialTemporal(), model, graph);
        RdfUtils.addTripleUri(datasetIri, DCAT.TEMPORAL_RESOLUTION, dataset.getTemporalResolution(), model, graph);

        if(dataset.getSpacialResolutions() != null){
            dataset.getSpacialResolutions().forEach(spacialResolution -> RdfUtils.addTripleUri(datasetIri, INSEE.SPATIAL_RESOLUTION, spacialResolution, model, graph));
        }

        if(dataset.getTemporalCoverageEndDate() != null && dataset.getTemporalCoverageStartDate() != null){
            BNode node =  RdfUtils.createBlankNode();
            model.add(node, RDF.TYPE, DCTERMS.PERIOD_OF_TIME, graph);

            if(dataset.getTemporalCoverageDataType() != null && dataset.getTemporalCoverageDataType().endsWith("date")){

                if(StringUtils.hasLength(dataset.getTemporalCoverageStartDate())){
                    model.add(node, DCAT.START_DATE, RdfUtils.setLiteralDate(dataset.getTemporalCoverageStartDate()), graph);
                }
                if(StringUtils.hasLength(dataset.getTemporalCoverageEndDate())){
                    model.add(node, DCAT.END_DATE, RdfUtils.setLiteralDate(dataset.getTemporalCoverageEndDate()), graph);
                }

            } else {
                model.add(node, DCAT.START_DATE, RdfUtils.setLiteralYear(dataset.getTemporalCoverageStartDate()), graph);
                model.add(node, DCAT.END_DATE, RdfUtils.setLiteralYear(dataset.getTemporalCoverageEndDate()), graph);

            }

            RdfUtils.addTripleBNode(datasetIri, DCTERMS.TEMPORAL, node, model, graph);
        }
    }

    private void addKeywords(IRI datasetIri, Optional<List<String>> keywords, String language, Model model, Resource graph) {
        keywords.ifPresent(list -> list.forEach(keyword ->
                RdfUtils.addTripleString(datasetIri, DCAT.KEYWORD, keyword, language, model, graph)
        ));
    }


    private void persistDataset(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDatasetsGraph());

        IRI datasetIri = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + dataset.getId());

        Model model = new LinkedHashModel();

        model.add(datasetIri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(dataset.getId()), graph);
        model.add(datasetIri, RDF.TYPE, DCAT.DATASET, graph);

        this.persistGeneralInformations(datasetIri, dataset, model, graph);
        this.persistInternalManagment(datasetIri, dataset, model, graph);
        this.persistNotes(datasetIri, dataset, model, graph);
        this.persistStatisticsInformations(datasetIri, dataset, model, graph);

        RdfUtils.addTripleString(datasetIri, INSEE.VALIDATION_STATE, dataset.getValidationState(), model, graph);


        Optional.ofNullable(dataset.getWasGeneratedIRIs()).ifPresent(list -> list.forEach(iri -> RdfUtils.addTripleUri(datasetIri, PROV.WAS_GENERATED_BY, iri, model, graph)));
        Optional.ofNullable(dataset.getThemes()).ifPresent(list -> list.forEach(theme -> RdfUtils.addTripleUri(datasetIri, DCAT.THEME, theme, model, graph)));
        Optional.ofNullable(dataset.getLinkedDocuments()).ifPresent(list -> list.forEach(linkDocument -> RdfUtils.addTripleString(datasetIri, DCTERMS.RELATION, linkDocument, model, graph)));
        Optional.ofNullable(dataset.getKeywords()).ifPresent(keywords -> {
            addKeywords(datasetIri, Optional.ofNullable(keywords.lg1()), config.getLg1(), model, graph);
            addKeywords(datasetIri, Optional.ofNullable(keywords.lg2()), config.getLg2(), model, graph);
        });

        if(dataset.getKeywords() != null){
            Optional.ofNullable(dataset.getKeywords().lg1()).ifPresent(list -> list.forEach(keyword -> RdfUtils.addTripleString(datasetIri, DCAT.KEYWORD, keyword, config.getLg1(), model, graph)));
            Optional.ofNullable(dataset.getKeywords().lg2()).ifPresent(list -> list.forEach(keyword -> RdfUtils.addTripleString(datasetIri, DCAT.KEYWORD, keyword, config.getLg2(), model, graph)));
        }

        JSONUtils.stream(new JSONArray(this.getDistributions(dataset.getId())))
                .filter(distribution -> distribution.has("id"))
                .map(distribution -> {
                    String id = distribution.getString("id");
                    return RdfUtils.createIRI(getDistributionBaseUri() + "/" + id);
                })
                .forEach(distributionIRI -> RdfUtils.addTripleUri(datasetIri, DCAT.HAS_DISTRIBUTION, distributionIRI, model, graph));

        repoGestion.loadSimpleObject(datasetIri, model, null);
    }

    private String persist(Dataset dataset) throws RmesException {
        this.persistCatalogRecord(dataset);
        this.persistDataset(dataset);
        return dataset.getId();
    }

    private void validate(Dataset dataset) throws RmesException {
        if (dataset.getLabelLg1() == null) {
            throw new RmesBadRequestException("The property labelLg1 is required");
        }
        if (dataset.getLabelLg2() == null) {
            throw new RmesBadRequestException("The property labelLg2 is required");
        }
        if (dataset.getCatalogRecord().getCreator() == null) {
            throw new RmesBadRequestException("The property creator is required");
        }
        if (dataset.getCatalogRecord().getContributor() == null || dataset.getCatalogRecord().getContributor().isEmpty()) {
            throw new RmesBadRequestException("The property contributor is required");
        }
        if (dataset.getDisseminationStatus() == null) {
            throw new RmesBadRequestException("The property disseminationStatus is required");
        }
        if (dataset.getAltIdentifier() != null && !ALT_IDENTIFIER_PATTERN.matcher(dataset.getAltIdentifier()).matches()) {
            throw new RmesBadRequestException("The property altIdentifier contains forbidden characters");
        }

        if(!this.seriesUtils.isSeriesAndOperationsExist(dataset.getWasGeneratedIRIs())){
            throw new RmesBadRequestException("Some series or operations do not exist");
        }
    }
}