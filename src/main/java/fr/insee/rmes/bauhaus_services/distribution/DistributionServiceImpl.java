package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.Distribution;
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
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DistributionServiceImpl extends RdfService implements DistributionService {

    @Value("${fr.insee.rmes.bauhaus.datasets.graph}")
    private String datasetsGraphSuffix;

    @Value("${fr.insee.rmes.bauhaus.datasets.baseURI}")
    private String datasetsBaseUriSuffix;

    @Value("${fr.insee.rmes.bauhaus.baseGraph}")
    private String baseGraph;

    @Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseURI}")
    private String baseUriGestion;

    @Value("${fr.insee.rmes.bauhaus.distribution.baseURI}")
    private String distributionsBaseUriSuffix;

    private String getDistributionGraph(){
        return baseGraph + datasetsGraphSuffix;
    }

    private String getDistributionBaseUri(){
        return baseUriGestion + distributionsBaseUriSuffix;
    }

    private String getDatasetsBaseUri(){
        return baseUriGestion + datasetsBaseUriSuffix;
    }

    @Override
    public String getDistributions() throws RmesException {
        return this.repoGestion.getResponseAsArray(DistributionQueries.getDistributions(getDistributionGraph())).toString();
    }

    @Override
    public String getDistributionByID(String id) throws RmesException {
        return this.repoGestion.getResponseAsObject(DistributionQueries.getDistribution(id, getDistributionGraph())).toString();
    }

    @Override
    public String create(String body) throws RmesException {
        Distribution distribution = Deserializer.deserializeBody(body, Distribution.class);
        distribution.setId(IdGenerator.generateNextId(repoGestion.getResponseAsObject(DistributionQueries.lastDatasetId(getDistributionGraph())), "d"));

        this.validate(distribution);

        distribution.setCreated(DateUtils.getCurrentDate());
        distribution.setUpdated(distribution.getCreated());

        return this.persist(distribution);
    }

    @Override
    public String update(String id, String body) throws RmesException {
        Distribution distribution = Deserializer.deserializeBody(body, Distribution.class);
        distribution.setId(id);

        this.validate(distribution);

        distribution.setUpdated(DateUtils.getCurrentDate());

        return this.persist(distribution);
    }

    @Override
    public String publishDistribution(String id) throws RmesException {
        Model model = new LinkedHashModel();
        IRI iri = RdfUtils.createIRI(getDistributionBaseUri() + "/" + id);

        publicationUtils.publishResource(iri, Set.of());
        model.add(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.createIRI(getDistributionGraph()));
        repoGestion.objectValidation(iri, model);

        return id;
    }


    private String persist(Distribution distribution) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDistributionGraph());

        IRI distributionIRI = RdfUtils.createIRI(getDistributionBaseUri() + "/" + distribution.getId());

        Model model = new LinkedHashModel();

        JSONObject previousValue = new JSONObject(this.getDistributionByID(distribution.getId()));
        if(previousValue.has("idDataset")){
            IRI iriDataset = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + previousValue.getString("idDataset"));
            repoGestion.deleteTripletByPredicateAndValue(iriDataset, DCAT.DISTRIBUTION, graph, null, distributionIRI);
        }

        RdfUtils.addTripleUri(RdfUtils.createIRI(getDatasetsBaseUri() + "/" + distribution.getIdDataset()), DCAT.DISTRIBUTION, distributionIRI, model, graph);

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