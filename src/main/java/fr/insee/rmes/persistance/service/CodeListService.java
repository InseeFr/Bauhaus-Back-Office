package fr.insee.rmes.persistance.service;

import fr.insee.rmes.exceptions.RmesException;

public interface CodeListService {


	String getCodeList(String codeListUri) throws RmesException;

	String getCode(String notation, String id) throws RmesException;

	String getCodeUri(String notationCodeList, String notationCode) throws RmesException;

}
