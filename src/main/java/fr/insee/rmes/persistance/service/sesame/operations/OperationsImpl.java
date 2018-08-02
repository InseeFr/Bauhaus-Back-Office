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
import fr.insee.rmes.persistance.service.sesame.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsUtils;
import fr.insee.rmes.persistance.service.sesame.operations.operations.VarBookExportBuilder;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesQueries;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesUtils;
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
	SeriesUtils seriesUtils;

	@Autowired
	OperationsUtils operationsUtils;

	@Autowired
	FamiliesUtils familiesUtils;

	@Autowired
	IndicatorsUtils indicatorsUtils;

	/***************************************************************************************************
	 * SERIES
	 *****************************************************************************************************/


	@Override
	public String getSeries() throws Exception {
		logger.info("Starting to get operation series list");
		String resQuery = RepositoryGestion.getResponseAsArray(SeriesQueries.seriesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getSeriesByID(String id) {
		JSONObject series = seriesUtils.getSeriesById(id);
		return series.toString();
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

	@Override
	public void setFamily(String id, String body) {
		familiesUtils.setFamily(id,body);
	}


	/***************************************************************************************************
	 * INDICATORS
	 *****************************************************************************************************/


	@Override
	public String getIndicators() {
		logger.info("Starting to get indicators list");
		String resQuery = RepositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getIndicatorByID(String id) {
		JSONObject indic = indicatorsUtils.getIndicatorById(id);
		return indic.toString();
	}




}
