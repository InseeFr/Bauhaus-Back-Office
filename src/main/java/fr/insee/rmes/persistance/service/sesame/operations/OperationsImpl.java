package fr.insee.rmes.persistance.service.sesame.operations;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.export.Jasper;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationsUtils;
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

	static final Logger logger = LogManager.getLogger(OperationsImpl.class);

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
	
	@Autowired
	DocumentationsUtils documentationsUtils;

	/***************************************************************************************************
	 * SERIES
	 * 
	 *****************************************************************************************************/


	@Override
	public String getSeries() throws RmesException  {
		logger.info("Starting to get operation series list");
		String resQuery = RepositoryGestion.getResponseAsArray(SeriesQueries.seriesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getSeriesByID(String id) throws RmesException {
		JSONObject series = seriesUtils.getSeriesById(id);
		return series.toString();
	}

	@Override
	public void setSeries(String id, String body) throws RmesException {
		seriesUtils.setSeries(id,body);
	}

	/***************************************************************************************************
	 * OPERATIONS
	 * 
	 *****************************************************************************************************/


	@Override
	public String getOperations() throws RmesException  {
		logger.info("Starting to get operations list");
		String resQuery = RepositoryGestion.getResponseAsArray(OperationsQueries.operationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public Response getVarBookExport(String id, String acceptHeader) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException  {
		String xmlForJasper = varBookExport.getData(id);
		InputStream is = jasper.exportVariableBook(xmlForJasper, acceptHeader);
		String fileName = "Dico" + id + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader).header("Content-Disposition", content).build();
	}

	@Override
	public String getOperationByID(String id) throws RmesException {
		JSONObject operation = operationsUtils.getOperationById(id);
		return operation.toString();
	}

	@Override
	public void setOperation(String id, String body) throws RmesException {
		operationsUtils.setOperation(id,body);
	}

	/***************************************************************************************************
	 * FAMILIES
	 * @throws RmesException 
	 *****************************************************************************************************/

	@Override
	public String getFamilies() throws RmesException {
		logger.info("Starting to get families list");
		String resQuery = RepositoryGestion.getResponseAsArray(FamiliesQueries.familiesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getFamilyByID(String id) throws RmesException {
		JSONObject family = familiesUtils.getFamilyById(id);
		return family.toString();
	}

	@Override
	public void setFamily(String id, String body) throws RmesException {
		familiesUtils.setFamily(id,body);
	}


	/***************************************************************************************************
	 * INDICATORS
	 * @throws RmesException 
	 *****************************************************************************************************/


	@Override
	public String getIndicators() throws RmesException {
		logger.info("Starting to get indicators list");
		String resQuery = RepositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getIndicatorByID(String id) throws RmesException {
		JSONObject indic = indicatorsUtils.getIndicatorById(id);
		return indic.toString();
	}

	@Override
	public void setIndicator(String id, String body) throws RmesException {
		indicatorsUtils.setIndicator(id,body);
	}

	/**
	 * Create indicator
	 * @throws RmesException 
	 */
	@Override
	public String setIndicator(String body) throws RmesException {
		return indicatorsUtils.setIndicator(body);
	}

	
	/***************************************************************************************************
	 * DOCUMENTATION
	 * @throws RmesException 
	 *****************************************************************************************************/

	@Override
	public String getMSD() throws RmesException {
		String resQuery = RepositoryGestion.getResponseAsArray(DocumentationsQueries.msdQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getMetadataAttribute(String id) throws RmesException {
		JSONObject attribute = documentationsUtils.getMetadataAttributeById(id);
		return attribute.toString();
	}
	
	@Override
	public String getMetadataAttributes() throws RmesException {
		String attributes = documentationsUtils.getMetadataAttributes().toString();
		return QueryUtils.correctEmptyGroupConcat(attributes);
	}

	@Override
	public String getMetadataReport(String id) throws RmesException {
		JSONObject documentation = documentationsUtils.getDocumentationByIdSims(id);
		return documentation.toString();
	}

	/**
	 * CREATE
	 */
	@Override
	public String setMetadataReport(String body, boolean isCreation) throws RmesException {
		return documentationsUtils.setMetadataReport(body, isCreation);
	}


}
