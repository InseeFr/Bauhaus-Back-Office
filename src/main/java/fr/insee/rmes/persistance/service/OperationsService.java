package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

public interface OperationsService {

	/**
	 * FAMILIES
	 */

	String getFamilies();

	String getFamilyByID(String id);
	
	void setFamily(String id, String body);



	/**
	 * SERIES
	 */

	String getSeries() throws Exception;

	String getSeriesByID(String id);




	/**
	 * OPERATIONS
	 */

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	String getOperations() throws Exception;

	String getOperationByID(String id);
	
	
	/**
	 * INDICATORS
	 */

	String getIndicators();

	String getIndicatorByID(String id);





}
