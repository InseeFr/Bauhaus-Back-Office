package fr.insee.rmes.bauhaus_services.organizations;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.organizations.OrganizationQueries;

@Component
public class OrganizationUtils  extends RdfService {

	
	public String getUri(String code) throws RmesException{
		if (StringUtils.isEmpty(code) ) {return null;}
		JSONObject orga = repoGestion.getResponseAsObject(OrganizationQueries.getUriById(code));
		return QueryUtils.correctEmptyGroupConcat(orga.getString("uri"));
	}
}
