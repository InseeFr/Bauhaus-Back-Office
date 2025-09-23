package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.datasets.DatasetQueries;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetDatasetStampChecker implements  ObjectStampChecker {

    private final RepositoryGestion repositoryGestion;
    private final Environment env;

    public DatasetDatasetStampChecker(Environment env, RepositoryGestion repositoryGestion) {
        this.env = env;
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<String> getStamps(String id) {

        IRI catalogRecordIRI = RdfUtils.createIRI(this.env.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.baseURI") + this.env.getProperty("fr.insee.rmes.bauhaus.datasets.record.baseURI") + "/" + id);
        String datasetGraph = this.env.getProperty("fr.insee.rmes.bauhaus.baseGraph") + this.env.getProperty("fr.insee.rmes.bauhaus.datasets.graph");
        try {
            JSONArray contributors = this.repositoryGestion.getResponseAsArray(DatasetQueries.getDatasetContributors(catalogRecordIRI, datasetGraph));
            List<String> stamps = new ArrayList<>();
            for (int i = 0; i < contributors.length(); i++) {
                JSONObject obj = contributors.getJSONObject(i);
                if (obj.has("contributor")) {
                    stamps.add(obj.getString("contributor"));
                }
            }
            return stamps;
        } catch (RmesException e) {
            return Collections.emptyList();
        }
    }
}