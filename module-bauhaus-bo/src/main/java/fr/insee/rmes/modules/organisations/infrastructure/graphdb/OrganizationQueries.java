package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;

import java.util.HashMap;
import java.util.List;

public class OrganizationQueries extends GenericQueries{

	public static final String ORGANIZATIONS_GRAPH = "ORGANIZATIONS_GRAPH";
	public static final String ORGANIZATIONS_INSEE_GRAPH = "ORGANIZATIONS_INSEE_GRAPH";
	public static final String ORGANIZATIONS_FOLDER = "fr/insee/rmes/modules/organisations/infrastructure/";

    public static String generateCompactOrganisationQuery(String identifier) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
        params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
        params.put("LG1", config.getLg1());
        params.put("IDENTIFIER", identifier);
        return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "compactOrganisationQuery.ftlh", params);
    }

    public static String generateCompactOrganisationsQuery(List<String> identifiers) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
        params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
        params.put("LG1", config.getLg1());
        params.put("IDENTIFIERS", identifiers);
        return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "compactOrganisationQuery.ftlh", params);
    }

    public static String checkIfOrganisationExistsQuery(String iri) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
        params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
        params.put("IRI", iri);
        return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "organisationExistsQuery.ftlh", params);
    }

	public static String organizationQuery(String identifier) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("IDENTIFIER", identifier);
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganization.ftlh", params);
	}

	public static String organizationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganizations.ftlh", params);
	}

	public static String organizationsTwoLangsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getOrganizationsTwoLangs.ftlh", params);
	}

	public static String getUriById(String identifier) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
		params.put(ORGANIZATIONS_INSEE_GRAPH, config.getOrgInseeGraph());
		params.put("IDENTIFIER", identifier);
		return FreeMarkerUtils.buildRequest(ORGANIZATIONS_FOLDER, "getUriById.ftlh", params);
	}

	  private OrganizationQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
