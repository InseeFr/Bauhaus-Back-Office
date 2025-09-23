package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetDistributionStampChecker implements  ObjectStampChecker {

    private final RepositoryGestion repositoryGestion;

    public DatasetDistributionStampChecker(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<String> getStamps(String id) {

        IRI distributionIRI = RdfUtils.objectIRI(ObjectType.DISTRIBUTION, id);
        try {
            JSONArray contributors = this.repositoryGestion.getResponseAsArray(DistributionQueries.getContributorsByDistributionUri(distributionIRI.toString()));
            List<String> stamps = new ArrayList<>();
            for (int i = 0; i < contributors.length(); i++) {
                JSONObject obj = contributors.getJSONObject(i);
                if (obj.has("contributors")) {
                    stamps.add(obj.getString("contributors"));
                }
            }
            return stamps;
        } catch (RmesException e) {
            return Collections.emptyList();
        }
    }
}