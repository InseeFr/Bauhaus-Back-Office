package fr.insee.rmes.persistance.service.sesame.operations;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.persistance.export.Jasper;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.operations.families.FamiliesQueries;
import fr.insee.rmes.persistance.service.sesame.operations.families.FamiliesUtils;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsUtils;
import fr.insee.rmes.persistance.service.sesame.operations.operations.VarBookExportBuilder;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesQueries;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Service
public class OperationsImpl implements OperationsService {

	final static Logger logger = LogManager.getLogger(OperationsImpl.class);

	@Autowired
	Jasper jasper;

	@Autowired
	VarBookExportBuilder varBookExport;

	@Autowired
	OperationsUtils operationsUtils;

	@Autowired
	FamiliesUtils familiesUtils;

	@Override
	public String getSeries() throws Exception {
		logger.info("Starting to get operation series list");
		String resQuery = RepositoryGestion.getResponseAsArray(SeriesQueries.seriesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getSeriesByID(String id) {
		JSONObject series = RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		addSeriesAltLabel(id, series);
		addSeriesOperations(id, series);
		return series.toString();
	}

	private void addSeriesAltLabel(String idSeries, JSONObject series) {
		JSONArray altLabelLg1 = RepositoryGestion.getResponseAsArray(SeriesQueries.altLabel(idSeries, Config.LG1));
		JSONArray altLabelLg2 = RepositoryGestion.getResponseAsArray(SeriesQueries.altLabel(idSeries, Config.LG2));
		if (altLabelLg1.length() != 0) {
			series.put("altLabelLg1", JSONUtils.extractFieldToArray(altLabelLg1, "altLabel"));
		}
		if (altLabelLg2.length() != 0) {
			series.put("altLabelLg2", JSONUtils.extractFieldToArray(altLabelLg2, "altLabel"));
		}
	}

	private void addSeriesOperations(String idSeries, JSONObject series) {
		JSONArray operations = RepositoryGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}


	@Override
	public String getSeriesLinksByID(String id) {
		return RepositoryGestion.getResponseAsArray(SeriesQueries.seriesLinks(id)).toString();
	}

	@Override
	public String getSeriesNotesByID(String id) {
		return RepositoryGestion.getResponseAsObject(SeriesQueries.seriesNotesQuery(id)).toString();
	}


	/***************************************************************************************************
	 * OPERATIONS
	 *****************************************************************************************************/


	@Override
	public String getOperations() throws Exception {
		logger.info("Starting to get operations list");
		String resQuery = RepositoryGestion.getResponseAsArray(OperationsQueries.operationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public Response getVarBookExport(String id, String acceptHeader) throws Exception {
		String xmlForJasper = varBookExport.getData(id);
		InputStream is = jasper.exportVariableBook(xmlForJasper, acceptHeader);
		String fileName = "Dico" + id + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader).header("Content-Disposition", content).build();
	}

	@Override
	public String getOperationByID(String id) {
		JSONObject operation = operationsUtils.getOperationById(id);
		return operation.toString();
	}


	/***************************************************************************************************
	 * FAMILIES
	 *****************************************************************************************************/

	@Override
	public String getFamilies() {
		logger.info("Starting to get families list");
		String resQuery = RepositoryGestion.getResponseAsArray(FamiliesQueries.familiesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getFamilyByID(String id) {
		JSONObject family = familiesUtils.getFamilyById(id);
		return family.toString();
	}

}
