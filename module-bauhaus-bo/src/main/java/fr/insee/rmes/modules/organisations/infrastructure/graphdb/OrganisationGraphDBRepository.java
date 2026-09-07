package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationRepository;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.json.JSONUtils;
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
    private static final String ORGANIZATIONS_INSEE_GRAPH_PARAM = "ORGANIZATIONS_INSEE_GRAPH";
    private static final String ORGANISATIONS_PATH = "organisations/";
    private static final String STAMP_FIELD = "stamp";
    private static final String LABEL_FIELD = "label";
    private static final String KEY_FIELD = "key";

    private final RepositoryGestion repositoryGestion;
    private final String organizationsGraph;
    private final String organizationsRootGraph;
    private final BauhausLanguagesProperties languages;

    public OrganisationGraphDBRepository(
            RepositoryGestion repositoryGestion,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.organisations.graph}") String organisationsGraph,
            @Value("${fr.insee.rmes.bauhaus.insee.graph}") String inseeGraph,
            BauhausLanguagesProperties languages) {
        this.repositoryGestion = repositoryGestion;
        // Existing methods (getOrganisations, getOrganisation) target the insee sub-graph.
        this.organizationsGraph = baseGraph + inseeGraph;
        // The map-resolution path needs the outer graph too, since some IRIs (sub-units) live there.
        this.organizationsRootGraph = baseGraph + organisationsGraph;
        this.languages = languages;
    }

    @Override
    public List<OrganisationOption> getOrganisations() throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH_PARAM, SparqlLiterals.iri(organizationsGraph));
        params.put("LANG", SparqlLiterals.literal(languages.lg1()));

        String query = FreeMarkerUtils.buildRequest(ORGANISATIONS_PATH, "getOrganisations.ftlh", params);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        List<OrganisationOption> organisations = new ArrayList<>();
        JSONUtils.stream(results).forEach(org -> {
            String stamp = org.getString(STAMP_FIELD);
            String label = org.getString(LABEL_FIELD);
            organisations.add(new OrganisationOption(stamp, label));
        });

        return organisations;
    }

    @Override
    public OrganisationOption getOrganisation(String identifier) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH_PARAM, SparqlLiterals.iri(organizationsGraph));
        params.put("LANG", SparqlLiterals.literal(languages.lg1()));
        params.put("IDENTIFIER", SparqlLiterals.literal(identifier));

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

        List<String> iris = new ArrayList<>();
        List<String> literals = new ArrayList<>();
        for (String id : identifiers) {
            if (id == null) {
                continue;
            }
            if (id.startsWith("http://") || id.startsWith("https://")) {
                iris.add(id);
            } else {
                literals.add(id);
            }
        }
        if (iris.isEmpty() && literals.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH_PARAM, SparqlLiterals.iri(organizationsRootGraph));
        params.put(ORGANIZATIONS_INSEE_GRAPH_PARAM, SparqlLiterals.iri(organizationsGraph));
        params.put("LANG", SparqlLiterals.literal(languages.lg1()));
        params.put("IRI_IDENTIFIERS", iris.stream().map(SparqlLiterals::iri).toList());
        params.put("LITERAL_IDENTIFIERS", literals.stream().map(SparqlLiterals::literal).toList());

        String query = FreeMarkerUtils.buildRequest(ORGANISATIONS_PATH, "getOrganisationsMap.ftlh", params);
        JSONArray results = repositoryGestion.getResponseAsArray(query);

        Map<String, OrganisationOption> organisationsMap = new HashMap<>();
        JSONUtils.stream(results).forEach(org -> {
            String label = org.getString(LABEL_FIELD);
            String stamp = org.has(STAMP_FIELD) ? org.getString(STAMP_FIELD) : null;
            String key = org.has(KEY_FIELD) ? org.getString(KEY_FIELD) : stamp;
            if (key != null) {
                organisationsMap.put(key, new OrganisationOption(stamp, label));
            }
        });

        return organisationsMap;
    }
}
