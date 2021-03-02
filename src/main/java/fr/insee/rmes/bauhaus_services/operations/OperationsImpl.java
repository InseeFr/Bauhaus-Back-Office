package fr.insee.rmes.bauhaus_services.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.MetadataStructureDefUtils;
import fr.insee.rmes.bauhaus_services.operations.families.FamiliesUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.VarBookExportBuilder;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.external_services.export.Jasper;
import fr.insee.rmes.external_services.export.XDocReport;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.families.FamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.SeriesQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Service
public class OperationsImpl  extends RdfService implements OperationsService {

	private static final String ATTACHMENT = "attachment";

	private static final String CONTENT_DISPOSITION = "Content-Disposition";

	static final Logger logger = LogManager.getLogger(OperationsImpl.class);

	@Value("classpath:bauhaus-sims.json")
	org.springframework.core.io.Resource simsDefaultValue;

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
		String resQuery = repoGestion.getResponseAsArray(SeriesQueries.seriesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getSeriesForSearch() throws RmesException  {
		return seriesUtils.getSeriesForSearch(null);
	}

	@Override
	public String getSeriesWithSims() throws RmesException  {
		logger.info("Starting to get series list with sims");
		JSONArray seriesArray = repoGestion.getResponseAsArray(SeriesQueries.seriesWithSimsQuery());
		return QueryUtils.correctEmptyGroupConcat(seriesArray.toString());
	}

	@Override
	public String getSeriesWithStamp(String stamp) throws RmesException  {
		logger.info("Starting to get series list with sims");
		String resQuery = repoGestion.getResponseAsArray(SeriesQueries.seriesWithStampQuery(stamp)).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}
	
	@Override
	public String getSeriesForSearchWithStamp(String stamp) throws RmesException {
		return seriesUtils.getSeriesForSearch(stamp);
	}
	
	@Override
	public Series getSeriesByID(String id) throws RmesException {
		return seriesUtils.getSeriesById(id);
	}

	@Override
	public IdLabelTwoLangs getSeriesLabelByID(String id) throws RmesException {
		return seriesUtils.getSeriesLabelById(id);
	}

	@Override
	public String getSeriesJsonByID(String id) throws RmesException {
		JSONObject series = seriesUtils.getSeriesJsonById(id);
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
	public Response getCodeBookExport(String ddiFile, File dicoVar,  String acceptHeader) throws RmesException  {
		OutputStream os;
		if (acceptHeader.equals(MediaType.APPLICATION_OCTET_STREAM)) {
			os = xdr.exportVariableBookInPdf(ddiFile,"DicoVar.odt");
		} else {
			os = xdr.exportVariableBookInOdt(ddiFile,dicoVar);
		}

		InputStream is = transformFileOutputStreamInInputStream(os);
		String fileName = "Codebook"+ExportUtils.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type(ATTACHMENT).fileName(fileName).build();
		return Response.ok(is, acceptHeader).header(CONTENT_DISPOSITION, content).build();
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
		String resQuery = repoGestion.getResponseAsArray(FamiliesQueries.familiesQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getFamiliesForSearch() throws RmesException {
		logger.info("Starting to get families list for search");
		String resQuery = repoGestion.getResponseAsArray(FamiliesQueries.familiesSearchQuery()).toString();
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
		return indicatorsUtils.getIndicatorById(id);
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


	/***************************************************************************************************
	 * DOCUMENTATION
	 * @throws RmesException 
	 *****************************************************************************************************/

	@Override
	public String getMSDJson() throws RmesException {
		String resQuery = repoGestion.getResponseAsArray(DocumentationsQueries.msdQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getMetadataReportDefaultValue() throws IOException {
		return StreamUtils.copyToString(this.simsDefaultValue.getInputStream(), Charset.defaultCharset());
	}

	@Override
	public MSD getMSD() throws RmesException {
		return operationsUtils.getMSD();
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
		XhtmlToMarkdownUtils.convertJSONObject(documentation);
		return documentation.toString();
	}

	@Override
	public Documentation getFullSimsForXml(String id) throws RmesException {
		return  documentationsUtils.getFullSimsForXml(id);
	}

	@Override
	public String getFullSimsForJson(String id) throws RmesException {
		return  documentationsUtils.getFullSimsForJson(id).toString();
	}
	
	@Override
	public String getMetadataReportOwner(String id) throws RmesException {
		return documentationsUtils.getDocumentationOwnersByIdSims(id);
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

	/**
	 * DELETE
	 */
	@Override
	public Status deleteMetadataReport(String id) throws RmesException {
		return documentationsUtils.deleteMetadataReport(id);
	}

	/**
	 * PUBLISH
	 */
	@Override
	public String publishMetadataReport(String id) throws RmesException {
		return documentationsUtils.publishMetadataReport(id);
	}

	/**
	 * EXPORT
	 */
	@Override
	public Response exportMetadataReport(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2) throws RmesException  {

		if(!(lg1) && !(lg2)) throw new RmesNotAcceptableException(
				ErrorCodes.SIMS_EXPORT_WITHOUT_LANGUAGE, 
				"at least one language must be selected for export",
				"in export of sims: "+id); 
		File output;
		InputStream is;
		try {
			output = documentationsUtils.exportMetadataReport(id,includeEmptyMas, lg1, lg2);
			is = new FileInputStream(output);
		} catch (Exception e1) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e1.getMessage(), "Error export");
		}
		String fileName = output.getName();
		ContentDisposition content = ContentDisposition.type(ATTACHMENT).fileName(fileName).build();
		return Response.ok(is, "application/vnd.oasis.opendocument.text").header(CONTENT_DISPOSITION, content).build();
	}

	public Response exportMetadataReportOld(String id) throws RmesException  {
		File output;
		InputStream is;
		try {
			output = documentationsUtils.exportMetadataReportOld(id);
			is = new FileInputStream(output);
		} catch (Exception e1) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e1.getMessage(), "Error export");
		}
		String fileName = output.getName();
		ContentDisposition content = ContentDisposition.type(ATTACHMENT).fileName(fileName).build();
		return Response.ok(is, MediaType.APPLICATION_OCTET_STREAM).header(CONTENT_DISPOSITION, content).build();
	}
	
	
	@Override
	public Response exportTestMetadataReport() throws RmesException  {
		File output;
		InputStream is;
		try {
			output = documentationsUtils.exportTestMetadataReport();
			is = new FileInputStream(output);
		} catch (Exception e1) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e1.getMessage(), "Error export");
		}
		String fileName = output.getName();
		ContentDisposition content = ContentDisposition.type(ATTACHMENT).fileName(fileName).build();
		return Response.ok(is, MediaType.APPLICATION_OCTET_STREAM).header(CONTENT_DISPOSITION, content).build();
	}


	

}
