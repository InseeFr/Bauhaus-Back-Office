package fr.insee.rmes.bauhaus_services.distribution;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.utils.DateUtils;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DistributionServiceImpl extends RdfService implements DistributionService {
    private static final String IO_EXCEPTION = "IOException";


    @Override
    public String getDistributions() throws RmesException {
        return this.repoGestion.getResponseAsArray(DistributionQueries.getDistributions()).toString();
    }

    @Override
    public String getDistributionByID(String id) throws RmesException {
        return this.repoGestion.getResponseAsObject(DistributionQueries.getDistribution(id)).toString();
    }

    private Distribution deserializeBody(String body) throws RmesException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Distribution dataset;
        try {
            dataset = mapper.readValue(body, Distribution.class);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), IO_EXCEPTION);
        }
        return dataset;
    }

    @Override
    public String create(String body) throws RmesException {
        Distribution distribution = deserializeBody(body);
        distribution.setId(generateNextId());

        this.validate(distribution);

        distribution.setCreated(DateUtils.getCurrentDate());
        distribution.setUpdated(distribution.getCreated());

        return this.persist(distribution);
    }

    @Override
    public String update(String id, String body) throws RmesException {
        Distribution distribution = deserializeBody(body);
        distribution.setId(id);

        this.validate(distribution);

        distribution.setUpdated(DateUtils.getCurrentDate());

        return this.persist(distribution);
    }

    private String generateNextId() throws RmesException {
        String prefix = "d";
        JSONObject json = repoGestion.getResponseAsObject(DistributionQueries.lastDatasetId());
        if (json.isEmpty()) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (id.equals(Constants.UNDEFINED)) {
            return prefix + "1000";
        }
        return prefix + (Integer.parseInt(id) + 1);
    }

    private String persist(Distribution distribution) throws RmesException {
        Resource graph = RdfUtils.datasetGraph();

        IRI distributionIRI = RdfUtils.distributionIRI(distribution.getId());

        Model model = new LinkedHashModel();

        JSONObject previousValue = new JSONObject(this.getDistributionByID(distribution.getId()));
        if(previousValue.has("idDataset")){
            IRI iriDataset = RdfUtils.datasetIRI(previousValue.getString("idDataset"));
            repoGestion.deleteTripletByPredicateAndValue(iriDataset, DCAT.DISTRIBUTION, graph, null, distributionIRI);
        }

        RdfUtils.addTripleUri(RdfUtils.datasetIRI(distribution.getIdDataset()), DCAT.DISTRIBUTION, distributionIRI, model, graph);

        model.add(distributionIRI, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(distribution.getId()), graph);
        model.add(distributionIRI, RDF.TYPE, DCAT.DISTRIBUTION, graph);
        model.add(distributionIRI, DCTERMS.TITLE, RdfUtils.setLiteralString(distribution.getLabelLg1(), config.getLg1()), graph);
        model.add(distributionIRI, DCTERMS.TITLE, RdfUtils.setLiteralString(distribution.getLabelLg2(), config.getLg2()), graph);
        RdfUtils.addTripleString(distributionIRI, DCTERMS.DESCRIPTION, distribution.getDescriptionLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(distributionIRI, DCTERMS.DESCRIPTION, distribution.getDescriptionLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleDateTime(distributionIRI, DCTERMS.CREATED, distribution.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(distributionIRI, DCTERMS.MODIFIED, distribution.getUpdated(), model, graph);

        RdfUtils.addTripleString(distributionIRI, DCTERMS.FORMAT, distribution.getFormat(), model, graph);
        RdfUtils.addTripleString(distributionIRI, DCAT.BYTE_SIZE, distribution.getTaille(), model, graph);
        RdfUtils.addTripleString(distributionIRI, DCAT.DOWNLOAD_URL, distribution.getUrl(), model, graph);

        repoGestion.loadSimpleObject(distributionIRI, model, null);

        return distribution.getId();
    }

    private void validate(Distribution distribution) throws RmesException {
        if (distribution.getIdDataset() == null) {
            throw new RmesBadRequestException("The property idDataset is required");
        }
        if (distribution.getLabelLg1() == null) {
            throw new RmesBadRequestException("The property labelLg1 is required");
        }
        if (distribution.getLabelLg2() == null) {
            throw new RmesBadRequestException("The property labelLg2 is required");
        }
    }
}
