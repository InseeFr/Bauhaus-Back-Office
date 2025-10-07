package fr.insee.rmes.bauhaus_services;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.config.swagger.model.code_list.CodeList;
import fr.insee.rmes.config.swagger.model.code_list.Page;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.model.codeslists.PartialCodesList;

import java.util.List;

public interface CodeListService {

    String getCodeListJson(String codeListUri) throws RmesException;

    String getCode(String notation, String id) throws RmesException;

    String getCodeUri(String notationCodeList, String notationCode) throws RmesException;

    List<PartialCodesList> getAllCodesLists(boolean partial) throws RmesException, JsonProcessingException;

    String getCodesListByIRI(String iri) throws RmesException;

    CodeList getDetailedCodesList(String notation) throws RmesException;

    List<CodeList> getDetailedCodesListForSearch(boolean partial) throws RmesException, JsonProcessingException;

    String setCodesList(String body, boolean partial) throws RmesException;

    String setCodesList(String id, String body, boolean partial) throws RmesException;

    String getPartialCodeListByParent(String parentIRI) throws RmesException;

    void deleteCodeList(String notation, boolean partial) throws RmesException;

    void publishCodeList(String id, boolean b) throws RmesException;

    Page getCodesForCodeList(String notation, List<String> search, int page, Integer perPage, String sort) throws RmesException;

    String getCodesJson(String notation, int page, Integer perPage) throws RmesException;

    String updateCodeFromCodeList(String notation, String code, String body) throws RmesException;

    String addCodeFromCodeList(String notation, String body) throws RmesException;

    String deleteCodeFromCodeList(String notation, String code) throws RmesException;

    String getDetailedPartialCodesList(String notation) throws RmesException;
}
