package fr.insee.rmes.bauhaus_services;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;

public interface OperationsDocumentationsService {


	/******************************************************************************************
	 * DOCUMENTATION
	 * *******************************************************************************************/

	
	MSD getMSD() throws RmesException;

	String getMetadataAttribute(String id) throws RmesException;

	String getMetadataAttributes() throws RmesException;
	
	//SIMS
	String getMetadataReport(String id) throws RmesException;

	Documentation getFullSimsForXml(String id) throws RmesException;

	String getFullSimsForJson(String id) throws RmesException;
	
	String createMetadataReport(String body) throws RmesException;

	String setMetadataReport(String id, String body) throws RmesException;

	String publishMetadataReport(String id) throws RmesException;
	
	String getMetadataReportOwner(String id) throws RmesException;

	String getMSDJson() throws RmesException;

	String getMetadataReportDefaultValue() throws IOException;

	HttpStatus deleteMetadataReport(String id) throws RmesException;
	
	/** export **/
	ResponseEntity<Object> exportMetadataReport(String id, boolean includeEmptyMas, boolean lg1, boolean lg2) throws RmesException;

	ResponseEntity<Object> exportMetadataReportForLabel(String id) throws RmesException;

	ResponseEntity<Object> exportMetadataReportTempFiles(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2) throws RmesException;


}
