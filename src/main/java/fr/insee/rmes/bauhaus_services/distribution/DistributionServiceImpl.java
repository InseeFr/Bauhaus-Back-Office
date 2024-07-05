package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.model.dataset.PatchDistribution;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;
import static fr.insee.rmes.exceptions.ErrorCodes.DISTRIUBTION_PATCH_INCORRECT_BODY;



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

    protected IRI getDatasetIri(String datasetId){
        IRI iri = RdfUtils.createIRI(getDatasetsBaseUri() + "/" + datasetId);
        return iri;
    }
    @Override
    public String getDistributions() throws RmesException {
        return this.repoGestion.getResponseAsArray(DistributionQueries.getDistributions(getDistributionGraph())).toString();
    }

    @Override
    public String getDistributionByID(String id) throws RmesException {
        JSONObject distribution = repoGestion.getResponseAsObject(DistributionQueries.getDistribution(id, getDistributionGraph()));

        if (distribution.isEmpty()){
            throw new RmesNotFoundException("This distribution does not exist");
        }

        return distribution.toString();
    }

    @Override
    public String create(String body) throws RmesException {
        Distribution distribution = Deserializer.deserializeBody(body, Distribution.class);
        String idnewt = IdGenerator.generateNextId(repoGestion.getResponseAsObject(DistributionQueries.lastDatasetId(getDistributionGraph())), "d");
        distribution.setId(idnewt);
        distribution.setValidationState(ValidationStatus.UNPUBLISHED.toString());

        this.validate(distribution);

        distribution.setCreated(DateUtils.getCurrentDate());
        distribution.setUpdated(distribution.getCreated());

        return this.persist(distribution, true);
    }

    @Override
    public String update(String id, String body) throws RmesException {
        Distribution distribution = Deserializer.deserializeBody(body, Distribution.class);
        distribution.setId(id);
        distribution.setValidationState(ValidationStatus.MODIFIED.toString());

        this.validate(distribution);

        distribution.setUpdated(DateUtils.getCurrentDate());

        return this.persist(distribution, false);
    }

    private String update(String distributionId, Distribution distribution) throws RmesException {
        distribution.setId(distributionId);

        return this.persist(distribution, false);
    }

    @Override
    public String publishDistribution(String id) throws RmesException {
        Model model = new LinkedHashModel();
        IRI iri = getDatasetIri(id);

        publicationUtils.publishResource(iri, Set.of());
        model.add(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.createIRI(getDistributionGraph()));
        model.remove(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.createIRI(getDistributionGraph()));
        model.remove(iri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.createIRI(getDistributionGraph()));

        repoGestion.objectValidation(iri, model);

        return id;
    }

    @Override
    public void deleteDistributionId(String distributionId) throws RmesException{
        String distributionString = getDistributionByID(distributionId);
        Distribution distribution = Deserializer.deserializeBody(distributionString, Distribution.class);
        if (isPublished(distribution)){
            throw new RmesBadRequestException(ErrorCodes.DISTRIBUTION_DELETE_ONLY_UNPUBLISHED, "Only unpublished distributions can be deleted");
        }
        IRI distributionIRI = RdfUtils.createIRI(getDistributionBaseUri());
        Resource graph = getDatasetIri(distributionId);
        String distributionURI = getDistributionBaseUri() + "/" + distributionId;
        repoGestion.deleteObject(RdfUtils.createIRI(distributionURI));
        repoGestion.deleteTripletByPredicate(distributionIRI,DCAT.DISTRIBUTION,graph);
    }
    private boolean isPublished(Distribution distribution) {
        return "Validated".equalsIgnoreCase(distribution.getValidationState());
    }

    private String persist(Distribution distribution, boolean creation) throws RmesException {
        Resource graph = RdfUtils.createIRI(getDistributionGraph());

        IRI distributionIRI = getDatasetIri(distribution.getId());

        Model model = new LinkedHashModel();

        if(!creation){
            JSONObject previousValue = new JSONObject(this.getDistributionByID(distribution.getId()));
            if(previousValue.has("idDataset")){
                IRI iriDataset = getDatasetIri(previousValue.getString("idDataset"));
                repoGestion.deleteTripletByPredicateAndValue(iriDataset, DCAT.HAS_DISTRIBUTION, graph, null, distributionIRI);
            }
        }

        RdfUtils.addTripleUri(getDatasetIri(distribution.getIdDataset()), DCAT.HAS_DISTRIBUTION, distributionIRI, model, graph);

        model.add(distributionIRI, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(distribution.getId()), graph);
        model.add(distributionIRI, RDF.TYPE, DCAT.DISTRIBUTION, graph);
        model.add(distributionIRI, DCTERMS.TITLE, RdfUtils.setLiteralString(distribution.getLabelLg1(), config.getLg1()), graph);
        model.add(distributionIRI, DCTERMS.TITLE, RdfUtils.setLiteralString(distribution.getLabelLg2(), config.getLg2()), graph);
        RdfUtils.addTripleString(distributionIRI, DCTERMS.DESCRIPTION, distribution.getDescriptionLg1(), config.getLg1(), model, graph);
        RdfUtils.addTripleString(distributionIRI, DCTERMS.DESCRIPTION, distribution.getDescriptionLg2(), config.getLg2(), model, graph);

        RdfUtils.addTripleDateTime(distributionIRI, DCTERMS.CREATED, distribution.getCreated(), model, graph);
        RdfUtils.addTripleDateTime(distributionIRI, DCTERMS.MODIFIED, distribution.getUpdated(), model, graph);

        RdfUtils.addTripleString(distributionIRI, DCTERMS.FORMAT, distribution.getFormat(), model, graph);
        RdfUtils.addTripleString(distributionIRI, DCAT.BYTE_SIZE, distribution.getByteSize(), model, graph);
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

    @Override
    public void patchDistribution(String distributionId, PatchDistribution patchDistribution) throws RmesException {
        String distributionByID = getDistributionByID(distributionId);
        Distribution distribution = Deserializer.deserializeBody(distributionByID, Distribution.class);
        if  (patchDistribution.getUpdated() == null && patchDistribution.getByteSize() == null && patchDistribution.getUrl() == null){
            throw new RmesBadRequestException(DISTRIUBTION_PATCH_INCORRECT_BODY,"One of these attributes is required : updated, byteSize or url");
        }
        if (patchDistribution.getUpdated() != null){
            distribution.setUpdated(patchDistribution.getUpdated());
        }
        if (patchDistribution.getByteSize() != null){
            distribution.setByteSize(patchDistribution.getByteSize());
        }
        if (patchDistribution.getUrl() != null){
            distribution.setUrl(patchDistribution.getUrl());
        }
        update(distributionId, distribution);
    }

}