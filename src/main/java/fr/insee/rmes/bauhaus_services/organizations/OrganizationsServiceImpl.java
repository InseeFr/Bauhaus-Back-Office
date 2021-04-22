package fr.insee.rmes.bauhaus_services.organizations;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.organizations.Organization;
import fr.insee.rmes.persistance.sparql_queries.organizations.OrganizationQueries;

@Service
public class OrganizationsServiceImpl  extends RdfService implements OrganizationsService {

	@Autowired
	OrganizationUtils organizationUtils;

	@Autowired
	FamOpeSerIndUtils famOpeSerUtils;

	static final Logger logger = LogManager.getLogger(OrganizationsServiceImpl.class);
	
	@Override
	public String getOrganizationJsonString(String organizationIdentifier) throws RmesException {
		JSONObject orgaJson = organizationUtils.getOrganizationJson(organizationIdentifier);
		return QueryUtils.correctEmptyGroupConcat(orgaJson.toString());
	}

	@Override
	public IdLabelTwoLangs getOrganization(String organizationIdentifier) throws RmesException {
		return organizationUtils.buildOrganizationFromJson(organizationUtils.getOrganizationJson(organizationIdentifier));
	}
	

	@Override
	public String getOrganizationUriById(String organizationIdentifier) throws RmesException {
		if (StringUtils.isEmpty(organizationIdentifier)) {return null;}
		JSONObject orga = repoGestion.getResponseAsObject(OrganizationQueries.getUriById(organizationIdentifier));
		return QueryUtils.correctEmptyGroupConcat(orga.getString(Constants.URI));
	}

	@Override
	public String getOrganizationsJson() throws RmesException {
		logger.info("Starting to get organizations list");
		String resQuery = repoGestion.getResponseAsArray(OrganizationQueries.organizationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public List<Organization> getOrganizations() throws RmesException {
		JSONArray orgsJson = repoGestion.getResponseAsArray(OrganizationQueries.organizationsTwoLangsQuery());
		List<Object> objects = 	famOpeSerUtils.buildObjectListFromJson(
				orgsJson,
				Organization.getClassOperationsLink());
		List<Organization> result = new ArrayList<>();
		for (Object o:objects){
			result.add((Organization) o);		
		}
		return result;
	}
	
}
