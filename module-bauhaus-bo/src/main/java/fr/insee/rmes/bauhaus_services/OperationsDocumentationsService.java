package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public interface OperationsDocumentationsService {


	/******************************************************************************************
	 * DOCUMENTATION
	 * *******************************************************************************************/


	MSD getMSD() throws RmesException;

	String getMetadataReport(String id) throws RmesException;

	Documentation getFullSimsForXml(String id) throws RmesException;

	String getFullSimsForJson(String id) throws RmesException;

	String createMetadataReport(String body) throws RmesException;

	void setMetadataReport(String id, String body) throws RmesException;

	void publishMetadataReport(String id) throws RmesException;

	String getMetadataReportOwner(String id) throws RmesException;

	String getMSDJson() throws RmesException;

	String getMetadataReportDefaultValue() throws IOException;

	HttpStatus deleteMetadataReport(String id) throws RmesException;

}
