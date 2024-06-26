package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.CatalogRecord;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.PatchDataset;
import fr.insee.rmes.persistance.ontologies.ADMS;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static fr.insee.rmes.exceptions.ErrorCodes.DATASET_PATCH_INCORRECT_BODY;


@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {

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
        IRI iri = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + datasetId);
        return iri;
    }

    private String getDatasetsAdmsBaseUri(){
        return baseUriGestion + identifiantsAlternatifsBaseUri + "/" +datasetsBaseUriSuffix;
    }

    private String getCatalogRecordBaseUri(){
        return baseUriGestion + datasetsRecordBaseUriSuffix;
    }

    static ValueFactory factory =  SimpleValueFactory.getInstance();

    @Override
    public String getDatasets() throws RmesException {
        return this.getDatasets(null);
    }

    @Override
    public String getDatasetsForDistributionCreation(String stamp) throws RmesException {
        return this.getDatasets(stamp);
    }

    @Override
    public String publishDataset(String id) throws RmesException {
        Model model = new LinkedHashModel();
        IRI iri = getDatasetIri(id);
        IRI catalogRecordIri = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + id);

        publicationUtils.publishResource(iri, Set.of("processStep", "archiveUnit", "validationState"));
        publicationUtils.publishResource(catalogRecordIri, Set.of(CREATOR, "contributor"));
        model.add(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.createIRI(getDatasetsGraph()));
        model.remove(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.createIRI(getDatasetsGraph()));
        model.remove(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.createIRI(getDatasetsGraph()));

        repoGestion.objectValidation(iri, model);

        return id;
    }

    private String getDatasets(String stamp) throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getDatasets(getDatasetsGraph(), stamp)).toString();
    }

    @Override
    public String getDatasetByID(String id) throws RmesException {
        JSONArray datasetWithThemes =  this.repoGestion.getResponseAsArray(DatasetQueries.getDataset(id, getDatasetsGraph(), getAdmsGraph()));

        if(datasetWithThemes.isEmpty()){
            throw new RmesNotFoundException("This dataset does not exist");
        }

        JSONObject dataset = datasetWithThemes.getJSONObject(0);
        List<String> themes = new ArrayList<>();
        for(int i = 0; i < datasetWithThemes.length(); i++){
            JSONObject tempDataset = datasetWithThemes.getJSONObject(i);
            if(tempDataset.has(THEME)){
                themes.add(tempDataset.getString(THEME));
            }
        }
        dataset.put("themes", themes);
        dataset.remove(THEME);

        getMultipleTripletsForObject(dataset, "creators", DatasetQueries.getDatasetCreators(id, getDatasetsGraph()), CREATOR);

        IRI catalogRecordIRI = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + id);
        getMultipleTripletsForObject(dataset, "spacialResolutions", DatasetQueries.getDatasetSpacialResolutions(id, getDatasetsGraph()), "spacialResolution");
        getMultipleTripletsForObject(dataset, "statisticalUnit", DatasetQueries.getDatasetStatisticalUnits(id, getDatasetsGraph()), "statisticalUnit");



        JSONObject catalogRecord = new JSONObject();
        getMultipleTripletsForObject(catalogRecord, "contributor", DatasetQueries.getDatasetContributors(catalogRecordIRI, getDatasetsGraph()), "contributor");

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
        return dataset.toString();
    }

    private String update(String datasetId, Dataset dataset) throws RmesException {
        dataset.setId(datasetId);

        if(ValidationStatus.VALIDATED.toString().equalsIgnoreCase(dataset.getValidationState())){
            dataset.setValidationState(ValidationStatus.MODIFIED.toString());
        }
        if(dataset.getIdSerie() != null){
            dataset.setIdSerie(RdfUtils.seriesIRI(dataset.getIdSerie()).toString());
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
        Dataset dataset = Deserializer.deserializeBody(body, Dataset.class);
        return this.update(datasetId, dataset);
    }

    @Override
    public String create(String body) throws RmesException {
        Dataset dataset = Deserializer.deserializeBody(body, Dataset.class);
        dataset.setId(IdGenerator.generateNextId(repoGestion.getResponseAsObject(DatasetQueries.lastDatasetId(getDatasetsGraph())), "jd"));
        dataset.setValidationState(ValidationStatus.UNPUBLISHED.toString());

        if(dataset.getIdSerie() != null){
            dataset.setIdSerie(RdfUtils.seriesIRI(dataset.getIdSerie()).toString());
        }

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
        String datasetByID = getDatasetByID(datasetId);
        Dataset dataset = Deserializer.deserializeBody(datasetByID, Dataset.class);
        if  (patchDataset.getUpdated() == null && patchDataset.getIssued() == null && patchDataset.getNumObservations() == null
                && patchDataset.getNumSeries() == null && patchDataset.getTemporal() == null){
            throw new RmesBadRequestException(DATASET_PATCH_INCORRECT_BODY,"One of these attributes is required : updated, issued, numObservations, numSeries, temporal");
        }

        if ( patchDataset.getIssued() != null){
            dataset.setIssued(patchDataset.getIssued());
        }

        if ( patchDataset.getUpdated() != null){
            dataset.setUpdated(patchDataset.getUpdated());
        }

        if ( patchDataset.getTemporal() != null){
            String temporalCoverageStartDate = patchDataset.getTemporal().getStartPeriod();
            String temporalCoverageEndDate = patchDataset.getTemporal().getEndPeriod();
            dataset.setTemporalCoverageStartDate(temporalCoverageStartDate);
            dataset.setTemporalCoverageStartDate(temporalCoverageEndDate);
        }

        if ( patchDataset.getNumObservations() != null && patchDataset.getNumObservations() > 0){
            dataset.setObservationNumber(patchDataset.getNumObservations());
        }

        if ( patchDataset.getNumSeries() != null){
            dataset.setTimeSeriesNumber(patchDataset.getNumSeries());
        }

        update(datasetId, dataset);
    }

    @Override
    public void deleteDatasetId(String datasetId) throws RmesException{
        String datasetString = getDatasetByID(datasetId);
        Dataset dataset = Deserializer.deserializeBody(datasetString, Dataset.class);
        if (!isUnpublished(dataset)){
            throw new RmesBadRequestException(ErrorCodes.DATASET_DELETE_ONLY_UNPUBLISHED, "Only unpublished datasets can be deleted");
        }

        if (hasDistribution(dataset)) {
            throw new RmesBadRequestException(ErrorCodes.DATASET_DELETE_ONLY_WITHOUT_DISTRIBUTION, "Only dataset without any distribution can be deleted");
        }

        IRI datasetIRI = RdfUtils.createIRI(getDatasetsBaseUri());
        IRI graph = getDatasetIri(datasetId);
        String datasetURI = getDatasetsBaseUri() + "/" + datasetId;

        repoGestion.deleteObject(RdfUtils.toURI(datasetURI));
        repoGestion.deleteTripletByPredicate(datasetIRI, DCAT.DATASET, graph);
    }

    private boolean isUnpublished(Dataset dataset) {
        return "Unpublished".equalsIgnoreCase(dataset.getValidationState());
    }

    private boolean hasDistribution(Dataset dataset) throws RmesException {
        String datasetId = dataset.getId();
        return !getDistributions(datasetId).equals("[]");
    }

    private void persistCatalogRecord(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDatasetsGraph());
        IRI catalogRecordIRI = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + dataset.getId());
        IRI datasetIri = getDatasetIri(dataset.getId());

        Model model = new LinkedHashModel();

        CatalogRecord record = dataset.getCatalogRecord();

        RdfUtils.addTripleUri(catalogRecordIRI, FOAF.PRIMARY_TOPIC, datasetIri, model, graph);

        model.add(catalogRecordIRI, RDF.TYPE, DCAT.CATALOG_RECORD, graph);
        model.add(catalogRecordIRI, DC.CREATOR, RdfUtils.setLiteralString(record.getCreator()), graph);

        record.getContributor().forEach(contributor -> model.add(catalogRecordIRI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(contributor), graph));
        RdfUtils.addTripleDateTime(catalogRecordIRI, DCTERMS.CREATED, record.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(catalogRecordIRI, DCTERMS.MODIFIED, record.getUpdated(), model, graph);

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

    private void persistDataset(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDatasetsGraph());

        IRI datasetIri = getDatasetIri(dataset.getId());

        Model model = new LinkedHashModel();

        model.add(datasetIri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(dataset.getId()), graph);
        model.add(datasetIri, RDF.TYPE, DCAT.DATASET, graph);

        this.persistGeneralInformations(datasetIri, dataset, model, graph);
        this.persistInternalManagment(datasetIri, dataset, model, graph);
        this.persistNotes(datasetIri, dataset, model, graph);
        this.persistStatisticsInformations(datasetIri, dataset, model, graph);

        RdfUtils.addTripleString(datasetIri, INSEE.VALIDATION_STATE, dataset.getValidationState(), model, graph);
        RdfUtils.addTripleUri(datasetIri, PROV.WAS_GENERATED_BY, dataset.getIdSerie(), model, graph);

        if(dataset.getThemes() != null){
            dataset.getThemes().forEach(theme -> RdfUtils.addTripleUri(datasetIri, DCAT.THEME, theme, model, graph));
        }

        JSONArray distributions = new JSONArray(this.getDistributions(dataset.getId()));

        for(int i = 0; i < distributions.length(); i++) {
            JSONObject distribution = distributions.getJSONObject(i);
            if (distribution.has("id")) {
                String id = distribution.getString("id");
                IRI distributionIRI = RdfUtils.createIRI(getDistributionBaseUri() + "/" + id);
                RdfUtils.addTripleUri(datasetIri, DCAT.HAS_DISTRIBUTION, distributionIRI, model, graph);
            }
        }

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
        if(!this.seriesUtils.isSeriesExist(dataset.getIdSerie())){
            throw new RmesBadRequestException("The series does not exist");
        }
    }
}