package fr.insee.rmes.persistance.service.sesame.organizations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.persistance.service.OrganizationsService;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class OrganizationsServiceImpl implements OrganizationsService {

	final static Logger logger = LogManager.getLogger(OrganizationsServiceImpl.class);


	@Override
	public String getOrganization(String organizationIdentifier) {
		JSONObject orga = RepositoryGestion.getResponseAsObject(OrganizationQueries.organizationQuery(organizationIdentifier));
		orga.put("id", organizationIdentifier);
		return QueryUtils.correctEmptyGroupConcat(orga.toString());
	}

	
	@Override
	public String getOrganizationUriById(String organizationIdentifier) {
		if (organizationIdentifier==null) {return null;}
		JSONObject orga = RepositoryGestion.getResponseAsObject(OrganizationQueries.getUriById(organizationIdentifier));
		return QueryUtils.correctEmptyGroupConcat(orga.getString("uri"));
	}

	@Override
	public String getOrganizations() {
		logger.info("Starting to get organizations list");
		String resQuery = RepositoryGestion.getResponseAsArray(OrganizationQueries.organizationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}


}
