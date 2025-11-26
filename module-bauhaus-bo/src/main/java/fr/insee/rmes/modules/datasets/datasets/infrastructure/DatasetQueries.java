package fr.insee.rmes.modules.datasets.datasets.infrastructure;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class DatasetQueries {

    private final Environment env;

    public DatasetQueries(Environment env) {
        this.env = env;
    }

    public String getDatasetContributors(IRI iri) throws RmesException {
        String datasetsGraph = this.env.getProperty("fr.insee.rmes.bauhaus.baseGraph") + this.env.getProperty("fr.insee.rmes.bauhaus.datasets.graph");
        Map<String, Object> params = Map.of("GRAPH", datasetsGraph, "IRI", iri);
        return FreeMarkerUtils.buildRequest("fr/insee/rmes/modules/datasets/datasets/infrastructure/", "getDatasetContributors.ftlh", params);
    }
}
