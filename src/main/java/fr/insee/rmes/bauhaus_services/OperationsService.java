package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.*;

import java.util.List;

public interface OperationsService {

	/******************************************************************************************
	 * FAMILIES
	 * *******************************************************************************************/

	List<PartialOperationFamily> getFamilies() throws RmesException;

	String getFamiliesForSearch() throws RmesException;

	String getFamilyByID(String id) throws RmesException ;
	
	void setFamily(String id, String body) throws RmesException;

	String setFamilyValidation(String body) throws RmesException;

	String createFamily(String body) throws RmesException;

	String getSeriesWithReport(String id) throws RmesException;

	/******************************************************************************************
	 * SERIES
	 * *******************************************************************************************/

	List<PartialOperationSeries> getSeries() throws RmesException;

	String getSeriesForSearch() throws RmesException;
	
	Series getSeriesByID(String id) throws RmesException;

	String getSeriesJsonByID(String id) throws RmesException;

	String getSeriesWithSims() throws RmesException;

	String getSeriesWithStamp(String stamp) throws RmesException;
	
	void setSeries(String id, String body) throws RmesException;

	String createSeries(String body) throws RmesException;

	String setSeriesValidation(String body) throws RmesException;

	String getSeriesForSearchWithStamp(String stamp) throws RmesException;
	
	
	/******************************************************************************************
	 * OPERATIONS
	 * *******************************************************************************************/

	List<PartialOperation> getOperations() throws RmesException ;

	Operation getOperationById(String id) throws RmesException ;

	String getOperationsWithoutReport(String id) throws RmesException;
	
	String getOperationsWithReport(String id) throws RmesException;

	void setOperation(String id, String body) throws RmesException;

	String createOperation(String body) throws RmesException;

	String setOperationValidation(String body) throws RmesException;
	
	
	/******************************************************************************************
	 * INDICATORS
	 * *******************************************************************************************/


	List<PartialOperationIndicator> getIndicators() throws RmesException;

	String getIndicatorsWithSims() throws RmesException;

	String getIndicatorsForSearch() throws RmesException;

	String getIndicatorJsonByID(String id) throws RmesException;

	Indicator getIndicatorById(String id) throws RmesException;

	/**
	 * CREATE
	 */
	String setIndicator(String body) throws RmesException;

	/**
	 * UPDATE
	 */
	void setIndicator(String id, String body) throws RmesException;
	
	/**
	 * PUBLISH
	 */
	String setIndicatorValidation(String body) throws RmesException;
}
