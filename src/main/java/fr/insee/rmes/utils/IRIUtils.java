package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
public record IRIUtils() {

    public IRI findIRI(String seriesId) {
        return RdfUtils.objectIRI(ObjectType.SERIES, seriesId);
    }

    public IRI findStructureIRI(String structureId) {
        return RdfUtils.objectIRI(ObjectType.STRUCTURE, structureId);
    }

    public IRI findDatasetIRI(String datasetId) {
        return RdfUtils.objectIRI(ObjectType.DATASET, datasetId);
    }

    public IRI findDistributionIRI(String distributionId) {
        return RdfUtils.objectIRI(ObjectType.DISTRIBUTION, distributionId);
    }
}
