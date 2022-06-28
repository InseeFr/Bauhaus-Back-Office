package fr.insee.rmes.bauhaus_services.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.operations.families.FamiliesUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.export.XDocReport;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.persistance.sparql_queries.operations.families.OpFamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.utils.EncodingType;
import fr.insee.rmes.utils.ExportUtils;

@Service
public class OperationsImpl  extends RdfService implements OperationsService {

	private static final String ATTACHMENT = "attachment";

	static final Logger logger = LogManager.getLogger(OperationsImpl.class);

	
	@Value("classpath:bauhaus-sims.json")
	org.springframework.core.io.Resource simsDefaultValue;

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

	/***************************************************************************************************
	 * SERIES
	 * 
	 *****************************************************************************************************/


	@Override
	public String getSeries() throws RmesException  {
		logger.info("Starting to get operation series list");
		String resQuery = repoGestion.getResponseAsArray(OpSeriesQueries.seriesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getSeriesForSearch() throws RmesException  {
		return seriesUtils.getSeriesForSearch(null);
	}

	@Override
	public String getSeriesWithSims() throws RmesException  {
		logger.info("Starting to get series list with sims");
		JSONArray seriesArray = repoGestion.getResponseAsArray(OpSeriesQueries.seriesWithSimsQuery());
		return QueryUtils.correctEmptyGroupConcat(seriesArray.toString());
	}

	@Override
	public String getSeriesWithStamp(String stamp) throws RmesException  {
		logger.info("Starting to get series list with sims");
		JSONArray series = repoGestion.getResponseAsArray(OpSeriesQueries.seriesWithStampQuery(stamp, this.stampsRestrictionsService.isAdmin()));
		List<JSONObject> seriesList = new ArrayList<>();
		for (int i = 0; i < series.length(); i++) {
			seriesList.add(series.getJSONObject(i));
		}
		seriesList.sort(( o1,  o2) -> {
				String key1 = Normalizer.normalize(o1.getString(Constants.LABEL), Normalizer.Form.NFD);
				String key2 = Normalizer.normalize(o2.getString(Constants.LABEL), Normalizer.Form.NFD);
				return key1.compareTo(key2);
			});
		return QueryUtils.correctEmptyGroupConcat(seriesList.toString());
	}

	@Override
	public String getSeriesForSearchWithStamp(String stamp) throws RmesException {
		return seriesUtils.getSeriesForSearch(stamp);
	}

	@Override
	public Series getSeriesByID(String id) throws RmesException {
		return seriesUtils.getSeriesById(id,EncodingType.MARKDOWN);
	}

	@Override
	public IdLabelTwoLangs getSeriesLabelByID(String id) throws RmesException {
		return seriesUtils.getSeriesLabelById(id);
	}

	/**
	 * Return the series in a JSONObject encoding in markdown
	 */
	@Override
	public String getSeriesJsonByID(String id) throws RmesException {
		JSONObject series = seriesUtils.getSeriesJsonById(id, EncodingType.MARKDOWN);
		return series.toString();
	}

	@Override
	public void setSeries(String id, String body) throws RmesException {
		seriesUtils.setSeries(id,body);
	}

	@Override
	public String getOperationsWithoutReport(String idSeries) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(OperationsQueries.operationsWithoutSimsQuery(idSeries));
		if (resQuery.length()==1 &&resQuery.getJSONObject(0).length()==0) {resQuery.remove(0);}
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}

	@Override
	public String getOperationsWithReport(String idSeries) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(OperationsQueries.operationsWithSimsQuery(idSeries));
		if (resQuery.length()==1 &&resQuery.getJSONObject(0).length()==0) {resQuery.remove(0);}
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}

	@Override
	public String createSeries(String body) throws RmesException {
		// TODO: check if there is already a series with the same name ?
		return seriesUtils.createSeries(body);
	}

	@Override
	public String setSeriesValidation(String id) throws RmesException{
		return seriesUtils.setSeriesValidation(id);
	}

	/***************************************************************************************************
	 * OPERATIONS
	 * 
	 *****************************************************************************************************/


	@Override
	public String getOperations() throws RmesException  {
		logger.info("Starting to get operations list");
		String resQuery = repoGestion.getResponseAsArray(OperationsQueries.operationsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public ResponseEntity<Resource> getCodeBookExport(String ddiFile, File dicoVar,  String accept) throws RmesException {		
		//Prepare file
		OutputStream os = xdr.exportVariableBookInOdt(ddiFile,dicoVar);
		InputStream is = transformFileOutputStreamInInputStream(os);
		if (is == null) throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't generate codebook","Stream is null");
		ByteArrayResource resource = null;
		try {
			resource = new ByteArrayResource(IOUtils.toByteArray(is));
			is.close();
		} catch (IOException e) {
			logger.error("Failed to getBytes of resource");
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}

		//Prepare response headers
		String fileName = "Codebook"+ ExportUtils.getExtension(accept);
		ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName).build();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(HttpHeaders.ACCEPT,  "*/*");
		responseHeaders.setContentDisposition(content);
		responseHeaders.add("Content-Type","application/vnd.oasis.opendocument.text" );

		return ResponseEntity.ok()
		         .headers(responseHeaders)
		         .contentLength(resource.contentLength())
		         .contentType(MediaType.APPLICATION_OCTET_STREAM)
		         .body(resource);
		
	}

	private InputStream transformFileOutputStreamInInputStream(OutputStream os) {
		Field pathField;
		String path = null;
		FileInputStream fis =null;
		try {
			pathField = FileOutputStream.class.getDeclaredField("path");
			pathField.setAccessible(true);
			path = (String) pathField.get(os);
			fis= new FileInputStream(path);

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | FileNotFoundException  e) {
			logger.error(e.getMessage(),e);
		}
		return(fis);
	}

	@Override
	public Operation getOperationById(String id) throws RmesException {
		return operationsUtils.getOperationById(id);
	}

	@Override
	public String getOperationJsonByID(String id) throws RmesException {
		JSONObject operation = operationsUtils.getOperationJsonById(id);
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

	@Override
	public String setOperationValidation(String id) throws RmesException{
		return operationsUtils.setOperationValidation(id);
	}

	/***************************************************************************************************
	 * FAMILIES
	 * @throws RmesException 
	 *****************************************************************************************************/

	@Override
	public String getFamilies() throws RmesException {
		logger.info("Starting to get families list");
		String resQuery = repoGestion.getResponseAsArray(OpFamiliesQueries.familiesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getFamiliesForSearch() throws RmesException {
		logger.info("Starting to get families list for search");
		String resQuery = repoGestion.getResponseAsArray(OpFamiliesQueries.familiesSearchQuery()).toString();
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

	@Override
	public String setFamilyValidation(String id) throws RmesException{
		return familiesUtils.setFamilyValidation(id);
	}

	/**
	 * CREATE
	 */
	@Override
	public String createFamily(String body) throws RmesException {
		return familiesUtils.createFamily(body);
	}

	public String getSeriesWithReport(String idFamily) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(OperationsQueries.seriesWithSimsQuery(idFamily));
		if (resQuery.length()==1 &&resQuery.getJSONObject(0).length()==0) {resQuery.remove(0);}
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}


	/***************************************************************************************************
	 * INDICATORS
	 * @throws RmesException 
	 *****************************************************************************************************/


	@Override
	public String getIndicators() throws RmesException {
		logger.info("Starting to get indicators list");
		String resQuery = repoGestion.getResponseAsArray(IndicatorsQueries.indicatorsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getIndicatorsWithSims() throws RmesException {
		logger.info("Starting to get indicators list with sims");
		String resQuery = repoGestion.getResponseAsArray(IndicatorsQueries.indicatorsWithSimsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getIndicatorsForSearch() throws RmesException {
		return indicatorsUtils.getIndicatorsForSearch();
	}

	@Override
	public String getIndicatorJsonByID(String id) throws RmesException {
		JSONObject indic = indicatorsUtils.getIndicatorJsonById(id);
		return indic.toString();
	}

	@Override
	public Indicator getIndicatorById(String id) throws RmesException {
		return indicatorsUtils.getIndicatorById(id,false);
	}

	@Override
	public void setIndicator(String id, String body) throws RmesException {
		indicatorsUtils.setIndicator(id,body);
	}

	/**
	 * Publish indicator
	 * @throws RmesException 
	 */
	@Override
	public String setIndicatorValidation(String id) throws RmesException{
		return indicatorsUtils.setIndicatorValidation(id);
	}

	/**
	 * Create indicator
	 * @throws RmesException 
	 */
	@Override
	public String setIndicator(String body) throws RmesException {
		return indicatorsUtils.setIndicator(body);
	}

}
