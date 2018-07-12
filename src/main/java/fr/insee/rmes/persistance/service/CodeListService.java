package fr.insee.rmes.persistance.service;

public interface CodeListService {


	String getCodeList(String codeListUri);

	String getCode(String notation, String id);

}
