package fr.insee.rmes.bauhaus_services;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.code_list.CodeList;
import fr.insee.rmes.config.swagger.model.code_list.CodeListResponse;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONObject;

import java.util.List;

public interface CodeListService {

    String getCodeListJson(String codeListUri) throws RmesException;

    String getCode(String notation, String id) throws RmesException;

    String getCodeUri(String notationCodeList, String notationCode) throws RmesException;

    List<CodeListResponse> getAllCodesLists(boolean partial) throws RmesException, JsonProcessingException;

    String geCodesListByIRI(String iri) throws RmesException;

    CodeListResponse getDetailedCodesList(String notation) throws RmesException;

    List<CodeListResponse> getDetailedCodesListForSearch(boolean partial) throws RmesException, JsonProcessingException;

    String setCodesList(String body, boolean partial) throws RmesException;

    String setCodesList(String id, String body, boolean partial) throws RmesException;

    CodeList getCodeListAndCodesForExport(String code) throws RmesException;

    String getPartialCodeListByParent(String parentIRI) throws RmesException;

    void deleteCodeList(String notation, boolean partial) throws RmesException;

    String publishCodeList(String id, boolean b) throws RmesException;

    CodeList getCodesForCodeList(String notation, List<String> search, int page, Integer perPage, String sort) throws RmesException;

    String getCodesJson(String notation, int page, Integer perPage) throws RmesException;

    String updateCodeFromCodeList(String notation, String code, String body) throws RmesException;

    String addCodeFromCodeList(String notation, String body) throws RmesException;

    String deleteCodeFromCodeList(String notation, String code) throws RmesException;

    String getDetailedPartialCodesList(String notation) throws RmesException;
}
