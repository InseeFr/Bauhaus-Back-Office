package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;

public interface CodeListService {


	String getCodeListJson(String codeListUri) throws RmesException;

	String getCode(String notation, String id) throws RmesException;

	String getCodeUri(String notationCodeList, String notationCode) throws RmesException;

	String getAllCodesLists(boolean partial) throws RmesException;

	String geCodesListByIRI(String iri) throws RmesException;

	String getDetailedCodesList(String notation, boolean partial) throws RmesException;

    String getDetailedCodesListForSearch(boolean partial) throws RmesException;

    String setCodesList(String body, boolean partial) throws RmesException;

	String setCodesList(String id, String body, boolean partial) throws RmesException;

	void deleteCodeList(String notation, boolean partial) throws RmesException;

}
