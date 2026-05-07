package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.Organization;
import fr.insee.rmes.modules.organisations.infrastructure.graphdb.OrganizationQueries;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationsServiceImpl  extends RdfService implements OrganizationsService {

	private final OrganizationUtils organizationUtils;

	private final FamOpeSerIndUtils famOpeSerUtils;

	private final OrganizationQueries organizationQueries;

	static final Logger logger = LoggerFactory.getLogger(OrganizationsServiceImpl.class);

	public OrganizationsServiceImpl(RepositoryGestion repoGestion, IdGenerator idGenerator,
									RepositoryPublication repositoryPublication, Config config,
									PublicationUtils publicationUtils,
									OrganizationUtils organizationUtils, FamOpeSerIndUtils famOpeSerUtils,
									OrganizationQueries organizationQueries) {
		super(repoGestion, idGenerator, repositoryPublication, config, publicationUtils);
		this.organizationUtils = organizationUtils;
		this.famOpeSerUtils = famOpeSerUtils;
		this.organizationQueries = organizationQueries;
	}
	
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
		JSONObject orga = repoGestion.getResponseAsObject(organizationQueries.getUriById(organizationIdentifier));
		if (!orga.has(Constants.URI)) {return null;}
		return QueryUtils.correctEmptyGroupConcat(orga.getString(Constants.URI));
	}

	@Override
	public String getOrganizationsJson() throws RmesException {
		logger.info("Starting to get organizations list");
		String resQuery = repoGestion.getResponseAsArray(organizationQueries.organizationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public List<Organization> getOrganizations() throws RmesException {
		JSONArray orgsJson = repoGestion.getResponseAsArray(organizationQueries.organizationsTwoLangsQuery());
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
