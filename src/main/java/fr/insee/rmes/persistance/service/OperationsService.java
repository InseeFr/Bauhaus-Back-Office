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

	void setSeries(String id, String body);


	/**
	 * OPERATIONS
	 */

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	String getOperations() throws Exception;

	String getOperationByID(String id);

	void setOperation(String id, String body);
	
	
	/**
	 * INDICATORS
	 */

	String getIndicators();

	String getIndicatorByID(String id);

	void setIndicator(String id, String body);







}
