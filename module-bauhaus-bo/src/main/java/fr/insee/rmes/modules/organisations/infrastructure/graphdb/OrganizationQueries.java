package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class OrganizationQueries {

	public static final String ORGANIZATIONS_GRAPH = "ORGANIZATIONS_GRAPH";
	public static final String ORGANIZATIONS_INSEE_GRAPH = "ORGANIZATIONS_INSEE_GRAPH";
	public static final String ORGANIZATIONS_FOLDER = "fr/insee/rmes/modules/organisations/infrastructure/";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public OrganizationQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

    public String generateCompactOrganisationQuery(String identifier) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
        params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
        params.put("LG1", languages.lg1());
        params.put("IDENTIFIER", identifier);
        return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "compactOrganisationQuery.ftlh", params);
    }

    public String generateCompactOrganisationsQuery(List<String> identifiers) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
        params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
        params.put("LG1", languages.lg1());
        params.put("IDENTIFIERS", identifiers);
        return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "compactOrganisationQuery.ftlh", params);
    }

    public String checkIfOrganisationExistsQuery(String iri) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
        params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
        params.put("IRI", iri);
        return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "organisationExistsQuery.ftlh", params);
    }

	public String organizationQuery(String identifier) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		params.put("IDENTIFIER", identifier);
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganization.ftlh", params);
	}

	public String organizationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganizations.ftlh", params);
	}

	public String organizationsTwoLangsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
		params.put("LG1", languages.lg1());
		params.put("LG2", languages.lg2());
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganizationsTwoLangs.ftlh", params);
	}

	public String getUriById(String identifier) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
		params.put("IDENTIFIER", identifier);
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getUriById.ftlh", params);
	}

	public String getOrganizationIdenfier(IRI from, String id, IRI expected) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, graphs.organizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, graphs.orgInseeGraph());
		params.put("FROM_PREDICATE", from.toString());
		params.put("ID", id);
		params.put("EXPECTED_PREDICATE", expected.toString());
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganizationIdentifier.ftlh", params);
	}
}