package fr.insee.rmes.bauhaus_services.organizations;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeser_utils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.organizations.OrganizationQueries;

@Component
public class OrganizationUtils  extends RdfService {
	
	@Autowired
	FamOpeSerUtils famOpeSerUtils;
	
	public String getUri(String code) throws RmesException{
		if (StringUtils.isEmpty(code) ) {return null;}
		JSONObject orga = repoGestion.getResponseAsObject(OrganizationQueries.getUriById(code));
		return QueryUtils.correctEmptyGroupConcat(orga.getString(Constants.URI));
	}

	public IdLabelTwoLangs buildOrganizationFromJson(JSONObject organizationJson) {
		return famOpeSerUtils.buildIdLabelTwoLangsFromJson(organizationJson);	
	}
	
	public JSONObject getOrganizationJson(String organizationIdentifier) throws RmesException {
		JSONObject orga = repoGestion.getResponseAsObject(OrganizationQueries.organizationQuery(organizationIdentifier));
		orga.put(Constants.ID, organizationIdentifier);
		return orga;

	}
	
}
