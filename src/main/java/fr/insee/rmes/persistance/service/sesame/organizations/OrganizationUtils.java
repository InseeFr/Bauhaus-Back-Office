package fr.insee.rmes.persistance.service.sesame.organizations;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.sparql_queries.organizations.OrganizationQueries;

@Component
public class OrganizationUtils {

	
	public static String getUri(String code) throws RmesException{
		if (StringUtils.isEmpty(code) ) {return null;}
		JSONObject orga = RepositoryGestion.getResponseAsObject(OrganizationQueries.getUriById(code));
		return QueryUtils.correctEmptyGroupConcat(orga.getString("uri"));
	}
}
