package fr.insee.rmes.persistance.service;

import java.util.List;

import javax.ws.rs.core.Response;

import fr.insee.rmes.persistance.service.sesame.operations.SimpleObjectForList;

public interface OperationsService {

	String getSeries() throws Exception;

	String getDataForVarBook(String idOperation) throws Exception;

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	List<SimpleObjectForList> getOperations() throws Exception;

}
