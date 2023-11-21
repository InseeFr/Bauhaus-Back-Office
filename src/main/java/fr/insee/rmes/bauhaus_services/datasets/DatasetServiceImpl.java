package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.DatasetsConfig;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {

    @Autowired
    SeriesUtils seriesUtils;

    @Autowired
    DatasetsConfig datasetsConfig;

    @Override
    public String getDatasets() throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getDatasets(datasetsConfig)).toString();
    }

    @Override
    public String getDatasetByID(String id) throws RmesException {
        JSONArray datasetWithThemes =  this.repoGestion.getResponseAsArray(DatasetQueries.getDataset(id, datasetsConfig));

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

        this.validate(dataset);

        dataset.setUpdated(DateUtils.getCurrentDate());

        return this.persist(dataset);
    }

    @Override
    public String create(String body) throws RmesException {
        Dataset dataset = Deserializer.deserializeBody(body, Dataset.class);
        dataset.setId(IdGenerator.generateNextId(repoGestion.getResponseAsObject(DatasetQueries.lastDatasetId(datasetsConfig)), "jd"));
        dataset.setValidationState(ValidationStatus.UNPUBLISHED.toString());

        if(dataset.getIdSerie() != null){
            dataset.setIdSerie(RdfUtils.seriesIRI(dataset.getIdSerie()).toString());
        }


        this.validate(dataset);

        dataset.setCreated(DateUtils.getCurrentDate());
        dataset.setUpdated(dataset.getCreated());

        return this.persist(dataset);
    }

    @Override
    public String getDistributions(String id) throws RmesException {
        return this.repoGestion.getResponseAsArray(DistributionQueries.getDatasetDistributions(id)).toString();
    }

    @Override
    public String getThemes() throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getThemes(datasetsConfig)).toString();
    }


    private String persist(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.createIRI(datasetsConfig.getDatasetsGraph());

        IRI datasetIri = RdfUtils.createIRI(datasetsConfig.getDatasetsBaseUri() + "/" + dataset.getId());

        Model model = new LinkedHashModel();

        model.add(datasetIri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(dataset.getId()), graph);
        model.add(datasetIri, RDF.TYPE, DCAT.DATASET, graph);
        model.add(datasetIri, DCTERMS.TITLE, RdfUtils.setLiteralString(dataset.getLabelLg1(), config.getLg1()), graph);
        model.add(datasetIri, DCTERMS.TITLE, RdfUtils.setLiteralString(dataset.getLabelLg2(), config.getLg2()), graph);
        model.add(datasetIri, DCTERMS.CREATOR, RdfUtils.setLiteralString(dataset.getCreator()), graph);
        model.add(datasetIri, DCTERMS.CONTRIBUTOR, RdfUtils.setLiteralString(dataset.getContributor()), graph);

        RdfUtils.addTripleString(datasetIri, DCTERMS.DESCRIPTION, dataset.getDescriptionLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(datasetIri, DCTERMS.DESCRIPTION, dataset.getDescriptionLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleDateTime(datasetIri, DCTERMS.CREATED, dataset.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(datasetIri, DCTERMS.MODIFIED, dataset.getUpdated(), model, graph);

        RdfUtils.addTripleUri(datasetIri, INSEE.DISSEMINATIONSTATUS, dataset.getDisseminationStatus(), model, graph);
        RdfUtils.addTripleString(datasetIri, INSEE.VALIDATION_STATE, dataset.getValidationState(), model, graph);
        RdfUtils.addTripleUri(datasetIri, PROV.WAS_GENERATED_BY, dataset.getIdSerie(), model, graph);

        dataset.getThemes().forEach(theme -> RdfUtils.addTripleUri(datasetIri, DCAT.THEME, theme, model, graph));
        ;

        JSONArray distributions = new JSONArray(this.getDistributions(dataset.getId()));

        for(int i = 0; i < distributions.length(); i++) {
            JSONObject distribution = distributions.getJSONObject(i);
            if (distribution.has("id")) {
                String id = distribution.getString("id");
                RdfUtils.addTripleUri(datasetIri, DCAT.DISTRIBUTION, RdfUtils.distributionIRI(id), model, graph);
            }
        }

        repoGestion.loadSimpleObject(datasetIri, model, null);

        return dataset.getId();
    }

    private void validate(Dataset dataset) throws RmesException {
        if (dataset.getLabelLg1() == null) {
            throw new RmesBadRequestException("The property labelLg1 is required");
        }
        if (dataset.getLabelLg2() == null) {
            throw new RmesBadRequestException("The property labelLg2 is required");
        }
        if (dataset.getCreator() == null) {
            throw new RmesBadRequestException("The property creator is required");
        }
        if (dataset.getContributor() == null) {
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
