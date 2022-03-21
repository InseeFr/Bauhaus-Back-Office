package fr.insee.rmes.bauhaus_services;

import java.io.File;

import org.springframework.http.ResponseEntity;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;

public interface OperationsService {

	/******************************************************************************************
	 * FAMILIES
	 * *******************************************************************************************/

	String getFamilies() throws RmesException;

	String getFamiliesForSearch() throws RmesException;

	String getFamilyByID(String id) throws RmesException ;
	
	void setFamily(String id, String body) throws RmesException;

	String setFamilyValidation(String body) throws RmesException;

	String createFamily(String body) throws RmesException;

	String getSeriesWithReport(String id) throws RmesException;

	/******************************************************************************************
	 * SERIES
	 * *******************************************************************************************/

	String getSeries() throws RmesException;

	String getSeriesForSearch() throws RmesException;
	
	Series getSeriesByID(String id) throws RmesException;

	String getSeriesJsonByID(String id) throws RmesException;

	IdLabelTwoLangs getSeriesLabelByID(String id) throws RmesException;

	String getSeriesWithSims() throws RmesException;

	String getSeriesWithStamp(String stamp) throws RmesException;
	
	void setSeries(String id, String body) throws RmesException;

	String createSeries(String body) throws RmesException;

	String setSeriesValidation(String body) throws RmesException;

	String getSeriesForSearchWithStamp(String stamp) throws RmesException;
	
	
	/******************************************************************************************
	 * OPERATIONS
	 * *******************************************************************************************/


	ResponseEntity<Object> getCodeBookExport(String ddiFile, File dicoVar, String acceptHeader) throws RmesException;

	String getOperations() throws RmesException ;

	String getOperationJsonByID(String id) throws RmesException;
	
	Operation getOperationById(String id) throws RmesException ;

	String getOperationsWithoutReport(String id) throws RmesException;
	
	String getOperationsWithReport(String id) throws RmesException;

	void setOperation(String id, String body) throws RmesException;

	String createOperation(String body) throws RmesException;

	String setOperationValidation(String body) throws RmesException;
	
	
	/******************************************************************************************
	 * INDICATORS
	 * *******************************************************************************************/


	String getIndicators() throws RmesException;

	String getIndicatorsWithSims() throws RmesException;

	String getIndicatorsForSearch() throws RmesException;

	String getIndicatorJsonByID(String id) throws RmesException;

	Indicator getIndicatorById(String id) throws RmesException;

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @throws RmesException
	 */
	void setIndicator(String id, String body) throws RmesException;
	
	/**
	 * PUBLISH
	 * @param body
	 * @throws RmesException
	 */

	String setIndicatorValidation(String body) throws RmesException;

	/**
	 * CREATE
	 * @param body
	 * @return
	 * @throws RmesException
	 */
	String setIndicator(String body) throws RmesException;


}
