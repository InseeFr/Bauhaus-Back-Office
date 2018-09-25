package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

import fr.insee.rmes.exceptions.RmesException;

public interface OperationsService {

	/**
	 * FAMILIES
	 * @throws RmesException 
	 */

	String getFamilies() throws RmesException;

	String getFamilyByID(String id) throws Exception;
	
	void setFamily(String id, String body) throws RmesException;



	/**
	 * SERIES
	 */

	String getSeries() throws Exception;

	String getSeriesByID(String id) throws RmesException;

	void setSeries(String id, String body) throws RmesException;


	/**
	 * OPERATIONS
	 */

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	String getOperations() throws Exception;

	String getOperationByID(String id) throws RmesException;

	void setOperation(String id, String body) throws RmesException;
	
	
	/**
	 * INDICATORS
	 * @throws RmesException 
	 */

	String getIndicators() throws RmesException;

	String getIndicatorByID(String id) throws RmesException;

	void setIndicator(String id, String body) throws RmesException;

	String setIndicator(String body) throws RmesException;



	/**
	 * DOCUMENTATION
	 * @throws RmesException 
	 */
	String getMSD() throws RmesException;

	String getMetadataAttribute(String id) throws RmesException;

	String getMetadataReport(String id) throws RmesException;

	String getMetadataAttributes() throws RmesException;



}
