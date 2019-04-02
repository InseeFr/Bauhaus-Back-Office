package fr.insee.rmes.persistance.service.sesame.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.export.ExportUtils;
import fr.insee.rmes.persistance.export.Jasper;
import fr.insee.rmes.persistance.export.XDocReport;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.MetadataStructureDefUtils;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.famOpeSerUtils;
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
	XDocReport xdr;

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
	
	@Autowired
	MetadataStructureDefUtils msdUtils;

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
	
	@Override
	public String getOperationsWithoutReport(String idSeries) throws RmesException {
		String resQuery = RepositoryGestion.getResponseAsArray(OperationsQueries.operationsWithoutSimsQuery(idSeries)).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String createSeries(String body) throws RmesException {
		// TODO: check if there is already a series with the same name ?
				
		String id = seriesUtils.createSeries(body);
		return id;
		
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

	@Deprecated
	@Override
	public Response getVarBookExport(String id, String acceptHeader) throws RmesException  {
		String xmlForJasper = varBookExport.getData(id);
		InputStream is = jasper.exportVariableBook(xmlForJasper, acceptHeader);
		String fileName = "Dico" + id + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader).header("Content-Disposition", content).build();
	}
	
	@Override
	public Response getCodeBookExport(String ddiFile, File dicoVar,  String acceptHeader) throws Exception  {
		OutputStream os;
		if (acceptHeader.equals(MediaType.APPLICATION_OCTET_STREAM)) {
			os = xdr.exportVariableBookInPdf(ddiFile,"DicoVar.odt");
		}else {
			os = xdr.exportVariableBookInOdt(ddiFile,dicoVar);
		}
		
		InputStream is = transformFileOutputStreamInInputStream(os);
		String fileName = "Codebook"+ExportUtils.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader).header("Content-Disposition", content).build();
	}

	private InputStream transformFileOutputStreamInInputStream(OutputStream os)
			throws NoSuchFieldException, IllegalAccessException, FileNotFoundException {
		Field pathField = FileOutputStream.class.getDeclaredField("path");
		pathField.setAccessible(true);
		String path = (String) pathField.get(os);
		return new FileInputStream(path);
	}
	

	@Override
	public String getOperationByID(String id) throws RmesException {
		JSONObject operation = operationsUtils.getOperationById(id);
		return operation.toString();
	}

	/**
	 * UPDATE
	 */
	@Override
	public void setOperation(String id, String body) throws RmesException {
		operationsUtils.setOperation(id,body);
	}

	/**
	 * CREATE
	 */
	@Override
	public String createOperation(String body) throws RmesException {
		return operationsUtils.setOperation(body);				
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
		JSONObject attribute = msdUtils.getMetadataAttributeById(id);
		return attribute.toString();
	}
	
	@Override
	public String getMetadataAttributes() throws RmesException {
		String attributes = msdUtils.getMetadataAttributes().toString();
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
	public String createMetadataReport(String body) throws RmesException {
		return documentationsUtils.setMetadataReport(null, body, true);
	}


	/**
	 * UPDATE
	 */
	@Override
	public String setMetadataReport(String id, String body) throws RmesException {
		return documentationsUtils.setMetadataReport(id, body, false);
	}
	
}
