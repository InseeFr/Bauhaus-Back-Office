package fr.insee.rmes.persistance.service;

import java.util.List;

import javax.ws.rs.core.Response;

import fr.insee.rmes.persistance.service.sesame.operations.series.SerieForList;

public interface OperationsService {

	List<SerieForList> getSeries() throws Exception;

	String getDataForVarBook(String idOperation) throws Exception;

	Response getVarBookExport(String id, String acceptHeader) throws Exception;

}
