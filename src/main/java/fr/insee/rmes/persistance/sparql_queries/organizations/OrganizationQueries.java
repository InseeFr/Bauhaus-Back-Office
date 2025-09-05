package fr.insee.rmes.persistance.sparql_queries.organizations;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;

public class OrganizationQueries extends GenericQueries{

	public static String organizationQuery(String identifier) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ORGANIZATIONS_GRAPH", config.getOrganizationsGraph());
		params.put("ORGANIZATIONS_INSEE_GRAPH", config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("IDENTIFIER", identifier);
		return FreeMarkerUtils.buildRequest("organizations/", "getOrganization.ftlh", params);
	}

	public static String organizationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ORGANIZATIONS_GRAPH", config.getOrganizationsGraph());
		params.put("ORGANIZATIONS_INSEE_GRAPH", config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest("organizations/", "getOrganizations.ftlh", params);
	}

	public static String organizationsTwoLangsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ORGANIZATIONS_GRAPH", config.getOrganizationsGraph());
		params.put("ORGANIZATIONS_INSEE_GRAPH", config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest("organizations/", "getOrganizationsTwoLangs.ftlh", params);
	}

	public static String getUriById(String identifier) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ORGANIZATIONS_GRAPH", config.getOrganizationsGraph());
		params.put("ORGANIZATIONS_INSEE_GRAPH", config.getOrgInseeGraph());
		params.put("IDENTIFIER", identifier);
		return FreeMarkerUtils.buildRequest("organizations/", "getUriById.ftlh", params);
	}

	  private OrganizationQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
