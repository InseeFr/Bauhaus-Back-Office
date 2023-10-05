package fr.insee.rmes.bauhaus_services.datasets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.DateUtils;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DatasetServiceImpl extends RdfService implements DatasetService {
    private static final String IO_EXCEPTION = "IOException";

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

        this.validate(dataset);

        dataset.setUpdated(DateUtils.getCurrentDate());

        if(dataset.getIdSerie() != null){
            dataset.setIdSerie(RdfUtils.seriesIRI(dataset.getIdSerie()).toString());
        }

        return this.persist(dataset);
    }

    @Override
    public String create(String body) throws RmesException {
        Dataset dataset = deserializeBody(body);
        dataset.setId(generateNextId());

        this.validate(dataset);

        dataset.setCreated(DateUtils.getCurrentDate());
        dataset.setUpdated(dataset.getCreated());

        if(dataset.getIdSerie() != null){
            dataset.setIdSerie(RdfUtils.seriesIRI(dataset.getIdSerie()).toString());
        }

        return this.persist(dataset);
    }

    private Dataset deserializeBody(String body) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Dataset dataset = null;
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
        if (json.length() == 0) {
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
        RdfUtils.addTripleString(datasetIri, DCAT.THEME, dataset.getTheme(), model, graph);

        repoGestion.loadSimpleObject(datasetIri, model, null);

        return dataset.getId();
    }

    private void validate(Dataset dataset) throws RmesException {
        if (dataset.getId() == null) {
            throw new RmesBadRequestException("The property identifiant is required");
        }
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
    }
}
