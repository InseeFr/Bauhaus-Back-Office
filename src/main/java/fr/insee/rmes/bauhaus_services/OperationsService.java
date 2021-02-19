package fr.insee.rmes.bauhaus_services;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;

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


	Response getCodeBookExport(String ddiFile, File dicoVar, String acceptHeader) throws RmesException;

	String getOperations() throws RmesException ;

	String getOperationJsonByID(String id) throws RmesException;
	
	Operation getOperationById(String id) throws RmesException ;

	String getOperationsWithoutReport(String id) throws RmesException;

	void setOperation(String id, String body) throws RmesException;

	String createOperation(String body) throws RmesException;

	String setOperationValidation(String body) throws RmesException;
	
	/******************************************************************************************
	 * INDICATORS
	 * *******************************************************************************************/


	String getIndicators() throws RmesException;

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



	/******************************************************************************************
	 * DOCUMENTATION
	 * *******************************************************************************************/

	
	MSD getMSD() throws RmesException;

	String getMetadataAttribute(String id) throws RmesException;

	String getMetadataAttributes() throws RmesException;
	
	//SIMS
	String getMetadataReport(String id) throws RmesException;

	Documentation getFullSims(String id) throws RmesException;

	String createMetadataReport(String body) throws RmesException;

	String setMetadataReport(String id, String body) throws RmesException;

	String publishMetadataReport(String id) throws RmesException;
	
	Response exportMetadataReport(String id, Boolean includeEmptyMas, Boolean english) throws RmesException;

	Response exportTestMetadataReport() throws RmesException;
	
	String getMetadataReportOwner(String id) throws RmesException;

	String getMSDJson() throws RmesException;

	String getMetadataReportDefaultValue() throws IOException;

	Status deleteMetadataReport(String id) throws RmesException;

}
