package fr.insee.rmes.bauhaus_services.datasets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.utils.DateUtils;
import org.apache.http.HttpStatus;
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

import java.io.IOException;

@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {
    private static final String IO_EXCEPTION = "IOException";

    @Autowired
    SeriesUtils seriesUtils;

    @Override
    public String getDatasets() throws RmesException {
        return this.repoGestion.getResponseAsArray(DatasetQueries.getDatasets()).toString();
    }

    @Override
    public String getDatasetByID(String id) throws RmesException {
        return this.repoGestion.getResponseAsObject(DatasetQueries.getDataset(id)).toString();
    }

    @Override
    public String update(String datasetId, String body) throws RmesException {

        Dataset dataset = deserializeBody(body);
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
        Dataset dataset = deserializeBody(body);
        dataset.setId(generateNextId());
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
        return this.repoGestion.getResponseAsArray(DatasetQueries.getThemes()).toString();
    }

    private Dataset deserializeBody(String body) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Dataset dataset;
        try {
            dataset = mapper.readValue(body, Dataset.class);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }
        return dataset;
    }

    private String generateNextId() throws RmesException {
        String prefix = "jd";
        JSONObject json = repoGestion.getResponseAsObject(DatasetQueries.lastDatasetId());
        if (json.isEmpty()) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (id.equals(Constants.UNDEFINED)) {
            return prefix + "1000";
        }
        return prefix + (Integer.parseInt(id) + 1);
    }

    private String persist(Dataset dataset) throws RmesException {
        Resource graph = RdfUtils.datasetGraph();

        IRI datasetIri = RdfUtils.datasetIRI(dataset.getId());

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
        RdfUtils.addTripleUri(datasetIri, DCAT.THEME, dataset.getTheme(), model, graph);

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
