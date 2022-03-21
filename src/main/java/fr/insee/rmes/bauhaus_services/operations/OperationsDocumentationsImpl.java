package fr.insee.rmes.bauhaus_services.operations;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationExport;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.MetadataStructureDefUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.external_services.export.XDocReport;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Service
public class OperationsDocumentationsImpl  extends RdfService implements OperationsDocumentationsService {

	static final Logger logger = LogManager.getLogger(OperationsDocumentationsImpl.class);

	@Value("classpath:bauhaus-sims.json")
	org.springframework.core.io.Resource simsDefaultValue;

	@Autowired
	XDocReport xdr;

	@Autowired
	OperationsUtils operationsUtils;

	@Autowired
	DocumentationsUtils documentationsUtils;
	
	@Autowired
	DocumentationExport documentationsExport;

	@Autowired
	MetadataStructureDefUtils msdUtils;
	
	@Autowired
	ParentUtils ownersUtils;


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
		return documentationsUtils.getMSD();
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
		return ownersUtils.getDocumentationOwnersByIdSims(id);
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
	public HttpStatus deleteMetadataReport(String id) throws RmesException {
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
	public ResponseEntity<Object> exportMetadataReport(String id, boolean includeEmptyMas, boolean lg1, boolean lg2) throws RmesException  {
		if(!(lg1) && !(lg2)) throw new RmesNotAcceptableException(
				ErrorCodes.SIMS_EXPORT_WITHOUT_LANGUAGE, 
				"at least one language must be selected for export",
				"in export of sims: "+id); 
		return documentationsExport.exportMetadataReport(id,includeEmptyMas, lg1, lg2,Constants.GOAL_RMES);

	}

	@Override
	public ResponseEntity<Object> exportMetadataReportForLabel(String id) throws RmesException  {
			return documentationsExport.exportMetadataReport(id,true, true, false, Constants.GOAL_COMITE_LABEL);
	}

	@Override
	public ResponseEntity<Object> exportMetadataReportTempFiles(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2) throws RmesException {
		return documentationsExport.exportMetadataReportFiles(id,includeEmptyMas, lg1, lg2);
	}



}
