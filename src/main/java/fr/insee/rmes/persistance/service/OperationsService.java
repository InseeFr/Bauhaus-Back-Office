package fr.insee.rmes.persistance.service;

import javax.ws.rs.core.Response;

public interface OperationsService {

	String getSeries() throws Exception;
	/**
	 * FAMILIES
	 */

	String getFamilies();

	String getFamilyByID(String id);



	String getSeriesByID(String id);

	String getSeriesLinksByID(String id);

	String getSeriesNotesByID(String id);



	/**
	 * OPERATIONS
	 */

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

	String getOperations() throws Exception;

	String getOperationByID(String id);




}
