package fr.insee.rmes.persistance.service.sesame.codeList;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class CodeListServiceImpl implements CodeListService {

	final static Logger logger = LogManager.getLogger(CodeListServiceImpl.class);


	@Override
	public String getCodeList(String notation) {
		JSONObject codeList = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeListLabelByNotation(notation));
		codeList.put("notation",notation);
		JSONArray items = RepositoryGestion.getResponseAsArray(CodeListQueries.getCodeListItemsByNotation(notation));
		if (items.length() != 0){
			codeList.put("codes", items);
		}
		return QueryUtils.correctEmptyGroupConcat(codeList.toString());
	}


	@Override
	public String getCode(String notationCodeList, String notationCode) {
		JSONObject code = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeByNotation(notationCodeList,notationCode));
		code.put("code", notationCode);
		code.put("notationCodeList", notationCodeList);
		return QueryUtils.correctEmptyGroupConcat(code.toString());
	}

	@Override
	public String getCodeUri(String notationCodeList, String notationCode) {
		if (StringUtils.isEmpty(notationCodeList) ||StringUtils.isEmpty(notationCode)) {return null;}
		JSONObject code = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeUriByNotation(notationCodeList,notationCode));
		return QueryUtils.correctEmptyGroupConcat(code.getString("uri"));
	}


}
