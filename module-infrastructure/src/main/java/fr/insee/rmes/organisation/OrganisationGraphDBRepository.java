package fr.insee.rmes.organisation;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrganisationGraphDBRepository implements OrganisationRepository {

    private static final String ORGANIZATIONS_GRAPH_PARAM = "ORGANIZATIONS_GRAPH";
    private static final String ORGANISATIONS_PATH = "organisations/";
    private static final String STAMP_FIELD = "stamp";
    private static final String LABEL_FIELD = "label";

    private final RepositoryGestion repositoryGestion;
    private final String organizationsGraph;
    private final String language;

    public OrganisationGraphDBRepository(
            RepositoryGestion repositoryGestion,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.insee.graph}") String inseeGraph,
            @Value("${fr.insee.rmes.bauhaus.lg1}") String language) {
        this.repositoryGestion = repositoryGestion;
        this.organizationsGraph = baseGraph + inseeGraph;
        this.language = language;
    }

    @Override
    public List<OrganisationOption> getOrganisations() throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH_PARAM, organizationsGraph);
        params.put("LANG", language);

        String query = FreeMarkerUtils.buildRequest(ORGANISATIONS_PATH, "getOrganisations.ftlh", params);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        List<OrganisationOption> organisations = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject org = results.getJSONObject(i);
            String stamp = org.getString(STAMP_FIELD);
            String label = org.getString(LABEL_FIELD);
            organisations.add(new OrganisationOption(stamp, label));
        }

        return organisations;
    }

    @Override
    public OrganisationOption getOrganisation(String identifier) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH_PARAM, organizationsGraph);
        params.put("LANG", language);
        params.put("IDENTIFIER", identifier);

        String query = FreeMarkerUtils.buildRequest(ORGANISATIONS_PATH, "getOrganisation.ftlh", params);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        if (results.isEmpty()) {
            return null;
        }

        JSONObject org = results.getJSONObject(0);
        String stamp = org.getString(STAMP_FIELD);
        String label = org.getString(LABEL_FIELD);
        return new OrganisationOption(stamp, label);
    }

    @Override
    public Map<String, OrganisationOption> getOrganisationsMap(List<String> identifiers) throws RmesException {
        if (identifiers == null || identifiers.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH_PARAM, organizationsGraph);
        params.put("LANG", language);
        params.put("IDENTIFIERS", identifiers);

        String query = FreeMarkerUtils.buildRequest(ORGANISATIONS_PATH, "getOrganisationsMap.ftlh", params);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        Map<String, OrganisationOption> organisationsMap = new HashMap<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject org = results.getJSONObject(i);
            String stamp = org.getString(STAMP_FIELD);
            String label = org.getString(LABEL_FIELD);
            organisationsMap.put(stamp, new OrganisationOption(stamp, label));
        }

        return organisationsMap;
    }
}
