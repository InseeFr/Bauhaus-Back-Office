package fr.insee.rmes.modules.datasets.datasets.infrastructure;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetQueries {

    private final Environment env;

    public DatasetQueries(Environment env) {
        this.env = env;
    }

    public String getDatasetContributors(IRI iri) throws RmesException {
        String datasetsGraph = this.env.getProperty("fr.insee.rmes.bauhaus.baseGraph")
                + this.env.getProperty("fr.insee.rmes.bauhaus.datasets.graph");
        Map<String, Object> params =
                Map.of("GRAPH", SparqlLiterals.iri(datasetsGraph), "IRI", SparqlLiterals.iri(iri.stringValue()));
        return FreeMarkerUtils.buildRequest(
                "fr/insee/rmes/modules/datasets/datasets/infrastructure/", "getDatasetContributors.ftlh", params);
    }
}
