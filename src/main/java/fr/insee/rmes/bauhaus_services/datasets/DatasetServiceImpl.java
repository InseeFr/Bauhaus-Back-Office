package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.CatalogRecord;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.IdGenerator;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {

    @Autowired
    SeriesUtils seriesUtils;

    @Value("${fr.insee.rmes.bauhaus.datasets.graph}")
    private String datasetsGraphSuffix;

    @Value("${fr.insee.rmes.bauhaus.operations.graph}")
    private String operationsGraphSuffix;

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



    private String getDatasetsGraph(){
        return baseGraph + datasetsGraphSuffix;
    }

    private String getOperationsGraph(){return  baseGraph + operationsGraphSuffix; }

    private String getDistributionBaseUri(){
        return baseUriGestion + distributionsBaseUriSuffix;
    }

    private String getDatasetsBaseUri(){
        return baseUriGestion + datasetsBaseUriSuffix;
    }

    private String getCatalogRecordBaseUri(){
        return baseUriGestion + datasetsRecordBaseUriSuffix;
    }

    @Override
    public String getDatasets() throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getDatasets(getDatasetsGraph())).toString();
    }

    @Override
    public String getDatasetByID(String id) throws RmesException {
        JSONArray datasetWithThemes =  this.repoGestion.getResponseAsArray(DatasetQueries.getDataset(id, getDatasetsGraph(),getOperationsGraph()));

        if(datasetWithThemes.isEmpty()){
            throw new RmesBadRequestException("This dataset does not exist");
        }

        JSONObject dataset = datasetWithThemes.getJSONObject(0);
        List<String> themes = new ArrayList<>();
        for(int i = 0; i < datasetWithThemes.length(); i++){
            JSONObject tempDataset = datasetWithThemes.getJSONObject(i);
            if(tempDataset.has("theme")){
                themes.add(tempDataset.getString("theme"));
            }
        }
        dataset.put("themes", themes);
        dataset.remove("theme");

        JSONArray creatorsArray = this.repoGestion.getResponseAsArray(DatasetQueries.getDatasetCreators(id, getDatasetsGraph()));
        List<String> creators = new ArrayList<>();
        creatorsArray.iterator().forEachRemaining((creator) -> creators.add(((JSONObject) creator).getString("creator")));
        dataset.put("creators", creators);

        JSONArray spacialResolutionsArray = this.repoGestion.getResponseAsArray(DatasetQueries.getDatasetSpacialResolutions(id, getDatasetsGraph()));
        List<String> spacialResolutions = new ArrayList<>();
        spacialResolutionsArray.iterator().forEachRemaining((spacialResolution) -> spacialResolutions.add(((JSONObject) spacialResolution).getString("spacialResolution")));
        dataset.put("spacialResolutions", spacialResolutions);

        JSONArray statisticalUnitArray = this.repoGestion.getResponseAsArray(DatasetQueries.getDatasetStatisticalUnits(id, getDatasetsGraph()));
        List<String> statisticalUnit = new ArrayList<>();
        statisticalUnitArray.iterator().forEachRemaining((unit) -> statisticalUnit.add(((JSONObject) unit).getString("statisticalUnit")));
        dataset.put("statisticalUnit", statisticalUnit);

        JSONObject catalogRecord = new JSONObject();
        if(dataset.has("catalogRecordCreator")){
            catalogRecord.put("creator", dataset.getString("catalogRecordCreator"));
            dataset.remove("catalogRecordCreator");
        }
        if(dataset.has("catalogRecordContributor")){
            catalogRecord.put("contributor", dataset.getString("catalogRecordContributor"));
            dataset.remove("catalogRecordContributor");
        }
        if(dataset.has("catalogRecordCreated")){
            catalogRecord.put("created", dataset.getString("catalogRecordCreated"));
            dataset.remove("catalogRecordCreated");
        }
        if(dataset.has("catalogRecordUpdated")){
            catalogRecord.put("updated", dataset.getString("catalogRecordUpdated"));
            dataset.remove("catalogRecordUpdated");
        }
        dataset.put("catalogRecord", catalogRecord);
        return dataset.toString();
    }

    @Override
    public String update(String datasetId, String body) throws RmesException {

        Dataset dataset = Deserializer.deserializeBody(body, Dataset.class);
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
    public String patchDataset(String datasetId, String observationNumber) throws RmesException {
        String datasetByID = getDatasetByID(datasetId);
        JSONObject jsonDataset = new JSONObject(datasetByID);
        JSONObject jsonObservationNumber = new JSONObject(observationNumber);
        Integer observationNumberInt = (Integer) jsonObservationNumber.get("observationNumber");
        if ( observationNumberInt > 0){
            jsonDataset.put("observationNumber",observationNumberInt);
        }
        JSONObject catalogRecord = (JSONObject) jsonDataset.get("catalogRecord");
        catalogRecord.put("updated",DateUtils.getCurrentDate());
        return update(datasetId,jsonDataset.toString());
    }

    private void persistCatalogRecord(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDatasetsGraph());
        IRI catalogRecordIRI = RdfUtils.createIRI(getCatalogRecordBaseUri() + "/" + dataset.getId());
        IRI datasetIri = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + dataset.getId());

        Model model = new LinkedHashModel();

        CatalogRecord record = dataset.getCatalogRecord();

        RdfUtils.addTripleUri(catalogRecordIRI, FOAF.PRIMARY_TOPIC, datasetIri, model, graph);
        model.add(catalogRecordIRI, DC.CREATOR, RdfUtils.setLiteralString(record.getCreator()), graph);
        model.add(catalogRecordIRI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(record.getContributor()), graph);
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
            dataset.getCreators().forEach(creator -> model.add(datasetIri, DCTERMS.CREATOR, RdfUtils.setLiteralString(creator), graph));
        }

        RdfUtils.addTripleString(datasetIri, DCTERMS.PUBLISHER, dataset.getPublisher(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCAT.LANDING_PAGE, dataset.getLandingPageLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCAT.LANDING_PAGE, dataset.getLandingPageLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleDateTime(datasetIri, DCTERMS.MODIFIED, dataset.getUpdated(), model, graph);
        RdfUtils.addTripleDateTime(datasetIri, DCTERMS.ISSUED, dataset.getIssued(), model, graph);

    }

    private void persistInternalManagment(IRI datasetIri, Dataset dataset, Model model, Resource graph){
        RdfUtils.addTripleUri(datasetIri, INSEE.DISSEMINATIONSTATUS, dataset.getDisseminationStatus(), model, graph);
        RdfUtils.addTripleUri(datasetIri, INSEE.PROCESS_STEP, dataset.getProcessStep(), model, graph);
        RdfUtils.addTripleString(datasetIri, INSEE.ARCHIVE_UNIT, dataset.getArchiveUnit(), model, graph);

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
        RdfUtils.addTripleUri(datasetIri, DCAT.TEMPORAL_RESOLUTION, dataset.getTemporalResolution(), model, graph);

        if(dataset.getSpacialResolutions() != null){
            dataset.getSpacialResolutions().forEach(spacialResolution -> RdfUtils.addTripleUri(datasetIri, INSEE.SPATIAL_RESOLUTION, spacialResolution, model, graph));
        }

        if(dataset.getTemporalCoverageEndDate() != null && dataset.getTemporalCoverageStartDate() != null){
            BNode node =  RdfUtils.createBlankNode();
            model.add(node, RDF.TYPE, DCTERMS.PERIOD_OF_TIME, graph);

            if(dataset.getTemporalCoverageDataType().endsWith("date")){
                model.add(node, DCAT.START_DATE, RdfUtils.setLiteralDate(dataset.getTemporalCoverageStartDate()), graph);
                model.add(node, DCAT.END_DATE, RdfUtils.setLiteralDate(dataset.getTemporalCoverageEndDate()), graph);
            } else {
                model.add(node, DCAT.START_DATE, RdfUtils.setLiteralYear(dataset.getTemporalCoverageStartDate()), graph);
                model.add(node, DCAT.END_DATE, RdfUtils.setLiteralYear(dataset.getTemporalCoverageEndDate()), graph);

            }

            RdfUtils.addTripleBNode(datasetIri, DCTERMS.TEMPORAL, node, model, graph);
        }
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
                RdfUtils.addTripleUri(datasetIri, DCAT.DISTRIBUTION, distributionIRI, model, graph);
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
        if (dataset.getCatalogRecord().getContributor() == null) {
            throw new RmesBadRequestException("The property contributor is required");
        }
        if (dataset.getDisseminationStatus() == null) {
            throw new RmesBadRequestException("The property disseminationStatus is required");
        }
        if(!this.seriesUtils.isSeriesExist(dataset.getIdSerie())){
            throw new RmesBadRequestException("The series does not exist");
        }
    }
}
