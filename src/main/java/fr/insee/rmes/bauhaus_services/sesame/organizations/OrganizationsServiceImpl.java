package fr.insee.rmes.bauhaus_services.sesame.organizations;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.sesame.utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.sesame.utils.SesameService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.organizations.OrganizationQueries;

@Service
public class OrganizationsServiceImpl  extends SesameService implements OrganizationsService {

	static final Logger logger = LogManager.getLogger(OrganizationsServiceImpl.class);


	@Override
	public String getOrganization(String organizationIdentifier) throws RmesException {
		JSONObject orga = repoGestion.getResponseAsObject(OrganizationQueries.organizationQuery(organizationIdentifier));
		orga.put(Constants.ID, organizationIdentifier);
		return QueryUtils.correctEmptyGroupConcat(orga.toString());
	}

	
	@Override
	public String getOrganizationUriById(String organizationIdentifier) throws RmesException {
		if (StringUtils.isEmpty(organizationIdentifier)) {return null;}
		JSONObject orga = repoGestion.getResponseAsObject(OrganizationQueries.getUriById(organizationIdentifier));
		return QueryUtils.correctEmptyGroupConcat(orga.getString("uri"));
	}

	@Override
	public String getOrganizations() throws RmesException {
		logger.info("Starting to get organizations list");
		String resQuery = repoGestion.getResponseAsArray(OrganizationQueries.organizationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}


}
