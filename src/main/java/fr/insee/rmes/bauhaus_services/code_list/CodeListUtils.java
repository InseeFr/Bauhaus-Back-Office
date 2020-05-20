package fr.insee.rmes.bauhaus_services.code_list;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.rdfUtils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdfUtils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;

@Component
public class CodeListUtils  extends RdfService {

	
	public String getCodeUri(String notationCodeList, String notationCode) throws RmesException{
		if (StringUtils.isEmpty(notationCodeList) ||StringUtils.isEmpty(notationCode)) {return null;}
		JSONObject code = repoGestion.getResponseAsObject(CodeListQueries.getCodeUriByNotation(notationCodeList,notationCode));
		return QueryUtils.correctEmptyGroupConcat(code.getString("uri"));
	}
}
