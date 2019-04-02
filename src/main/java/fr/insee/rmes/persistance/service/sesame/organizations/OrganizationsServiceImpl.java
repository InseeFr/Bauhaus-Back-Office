package fr.insee.rmes.persistance.service.sesame.organizations;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.OrganizationsService;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class OrganizationsServiceImpl implements OrganizationsService {

	final static Logger logger = LogManager.getLogger(OrganizationsServiceImpl.class);


	@Override
	public String getOrganization(String organizationIdentifier) throws RmesException {
		JSONObject orga = RepositoryGestion.getResponseAsObject(OrganizationQueries.organizationQuery(organizationIdentifier));
		orga.put("id", organizationIdentifier);
		return QueryUtils.correctEmptyGroupConcat(orga.toString());
	}

	
	@Override
	public String getOrganizationUriById(String organizationIdentifier) throws RmesException {
		if (StringUtils.isEmpty(organizationIdentifier)) {return null;}
		JSONObject orga = RepositoryGestion.getResponseAsObject(OrganizationQueries.getUriById(organizationIdentifier));
		return QueryUtils.correctEmptyGroupConcat(orga.getString("uri"));
	}

	@Override
	public String getOrganizations() throws RmesException {
		logger.info("Starting to get organizations list");
		String resQuery = RepositoryGestion.getResponseAsArray(OrganizationQueries.organizationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}


}
