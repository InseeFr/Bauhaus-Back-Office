package fr.insee.rmes.persistance.service;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import fr.insee.rmes.exceptions.RmesException;

public interface OperationsService {

	/**
	 * FAMILIES
	 * @throws RmesException 
	 */

	String getFamilies() throws RmesException;

	String getFamilyByID(String id) throws RmesException ;
	
	void setFamily(String id, String body) throws RmesException;



	/**
	 * SERIES
	 * @throws RmesException 
	 */

	String getSeries() throws RmesException;

	String getSeriesByID(String id) throws RmesException;

	void setSeries(String id, String body) throws RmesException;


	/**
	 * OPERATIONS
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 

	 */

	Response getVarBookExport(String id, String acceptHeader) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException;

	String getOperations() throws RmesException ;

	String getOperationByID(String id) throws RmesException;

	void setOperation(String id, String body) throws RmesException;
	
	
	/**
	 * INDICATORS

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
