package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.code_list.CodeList;
import fr.insee.rmes.exceptions.RmesException;

import java.util.List;

public interface CodeListService {

    String getCodeListJson(String codeListUri) throws RmesException;

    String getCode(String notation, String id) throws RmesException;

    String getCodeUri(String notationCodeList, String notationCode) throws RmesException;

    String getAllCodesLists(boolean partial) throws RmesException;

    String getCodesListByIRI(String iri) throws RmesException;

    String getDetailedCodesList(String notation, boolean partial) throws RmesException;

    String getDetailedCodesListForSearch(boolean partial) throws RmesException;

    String setCodesList(String body, boolean partial) throws RmesException;

    String setCodesList(String id, String body, boolean partial) throws RmesException;

    CodeList getCodeListAndCodesForExport(String code) throws RmesException;

    String getPartialCodeListByParent(String parentIRI) throws RmesException;

    void deleteCodeList(String notation, boolean partial) throws RmesException;

    String publishCodeList(String id, boolean b) throws RmesException;

    String getCodesForCodeList(String notation, List<String> search, int page, Integer perPage, String sort) throws RmesException;

    String getCodesJson(String notation, int page, Integer perPage) throws RmesException;

    String updateCodeFromCodeList(String notation, String code, String body) throws RmesException;

    String addCodeFromCodeList(String notation, String body) throws RmesException;

    String deleteCodeFromCodeList(String notation, String code) throws RmesException;
}
