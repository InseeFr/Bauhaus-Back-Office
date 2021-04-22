package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;

public interface CodeListService {


	String getCodeListJson(String codeListUri) throws RmesException;

	String getCode(String notation, String id) throws RmesException;

	String getCodeUri(String notationCodeList, String notationCode) throws RmesException;

	String getAllCodesLists() throws RmesException;

	String geCodesListByIRI(String IRI) throws RmesException;

}
