package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

public interface OperationsService {

	/**
	 * FAMILIES
	 */

	String getFamilies();

	String getFamilyByID(String id);



	/**
	 * SERIES
	 */

	String getSeries() throws Exception;

	String getSeriesByID(String id);

	String getSeriesLinksByID(String id);



	/**
	 * OPERATIONS
	 */

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	String getOperations() throws Exception;

	String getOperationByID(String id);




}
