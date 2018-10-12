package fr.insee.rmes.persistance.service.sesame.code_list;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class CodeListUtils {

	
	public static String getCodeUri(String notationCodeList, String notationCode) throws RmesException{
		if (StringUtils.isEmpty(notationCodeList) ||StringUtils.isEmpty(notationCode)) {return null;}
		JSONObject code = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeUriByNotation(notationCodeList,notationCode));
		return QueryUtils.correctEmptyGroupConcat(code.getString("uri"));
	}
}
