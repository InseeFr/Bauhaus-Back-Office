package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperationSeriesStampChecker implements  ObjectStampChecker {

    private final RepositoryGestion repositoryGestion;

    public OperationSeriesStampChecker(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<String> getStamps(String id) {

        IRI seriesIRI = RdfUtils.objectIRI(ObjectType.SERIES, id);
        try {
            JSONArray contributors = this.repositoryGestion.getResponseAsArray(OpSeriesQueries.getCreatorsBySeriesUri(seriesIRI.toString()));
            List<String> stamps = new ArrayList<>();
            for (int i = 0; i < contributors.length(); i++) {
                JSONObject obj = contributors.getJSONObject(i);
                if (obj.has("creators")) {
                    stamps.add(obj.getString("creators"));
                }
            }
            return stamps;
        } catch (RmesException e) {
            return Collections.emptyList();
        }
    }
}