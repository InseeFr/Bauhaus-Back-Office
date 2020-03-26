package fr.insee.rmes.persistance.service.sesame.code_list;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.sparqlQueries.code_list.CodeListQueries;

@Service
public class CodeListServiceImpl implements CodeListService {

	final static Logger logger = LogManager.getLogger(CodeListServiceImpl.class);


	@Override
	public String getCodeList(String notation) throws RmesException{
		JSONObject codeList = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeListLabelByNotation(notation));
		codeList.put("notation",notation);
		JSONArray items = RepositoryGestion.getResponseAsArray(CodeListQueries.getCodeListItemsByNotation(notation));
		if (items.length() != 0){
			codeList.put("codes", items);
		}
		return QueryUtils.correctEmptyGroupConcat(codeList.toString());
	}


	@Override
	public String getCode(String notationCodeList, String notationCode) throws RmesException{
		JSONObject code = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeByNotation(notationCodeList,notationCode));
		code.put("code", notationCode);
		code.put("notationCodeList", notationCodeList);
		return QueryUtils.correctEmptyGroupConcat(code.toString());
	}

	@Override
	public String getCodeUri(String notationCodeList, String notationCode) throws RmesException{
			return CodeListUtils.getCodeUri(notationCodeList, notationCode);
	}


}
