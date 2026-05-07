package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.infrastructure.graphdb.OrganizationQueries;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class OrganizationUtils  extends RdfService {

	private final FamOpeSerIndUtils famOpeSerUtils;

	private final OrganizationQueries organizationQueries;

	public OrganizationUtils(RepositoryGestion repoGestion, IdGenerator idGenerator,
							 RepositoryPublication repositoryPublication, Config config,
							 PublicationUtils publicationUtils,
							 FamOpeSerIndUtils famOpeSerUtils, OrganizationQueries organizationQueries) {
		super(repoGestion, idGenerator, repositoryPublication, config, publicationUtils);
		this.famOpeSerUtils = famOpeSerUtils;
		this.organizationQueries = organizationQueries;
	}
	
	public String getUri(String code) throws RmesException{
		if (StringUtils.isEmpty(code) ) {return null;}
		JSONObject orga = repoGestion.getResponseAsObject(organizationQueries.getUriById(code));
		return QueryUtils.correctEmptyGroupConcat(orga.getString(Constants.URI));
	}

	public IdLabelTwoLangs buildOrganizationFromJson(JSONObject organizationJson) {
		return famOpeSerUtils.buildIdLabelTwoLangsFromJson(organizationJson);	
	}
	
	public JSONObject getOrganizationJson(String organizationIdentifier) throws RmesException {
		JSONObject orga = repoGestion.getResponseAsObject(organizationQueries.organizationQuery(organizationIdentifier));
		orga.put(Constants.ID, organizationIdentifier);
		return orga;

	}
	
}
