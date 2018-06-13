package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

public interface OperationsService {

	String getSeries() throws Exception;

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	String getOperations() throws Exception;

}
