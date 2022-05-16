package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.utils.EncodingType;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.utils.XsltUtils;

@Component
public class DocumentationExport {

	
	@Autowired
	ExportUtils exportUtils;
	
	@Autowired
	SeriesUtils seriesUtils;
	
	@Autowired
	OperationsUtils operationsUtils;
	
	@Autowired
	IndicatorsUtils indicatorsUtils;
	
	@Autowired
	ParentUtils parentUtils;
	
	@Autowired
	CodeListService codeListServiceImpl;
	
	@Autowired
	OrganizationsService organizationsServiceImpl;
	
	@Autowired
	DocumentationsUtils documentationsUtils;
	
	String xslFile = "/xslTransformerFiles/sims2fodt.xsl";
	String xmlPatternRmes = "/xslTransformerFiles/simsRmes/rmesPatternContent.xml";
	String zipRmes = "/xslTransformerFiles/simsRmes/toZipForRmes.zip";
	
	String xmlPatternLabel = "/xslTransformerFiles/simsLabel/labelPatternContent.xml";
	String zipLabel = "/xslTransformerFiles/simsLabel/toZipForLabel.zip";
	
	public ResponseEntity<?> exportAsResponse(Map<String, String> xmlContent, String targetType, boolean includeEmptyFields, boolean lg1,
			boolean lg2, String goal) throws RmesException {
		//Add params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, targetType);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		if (Constants.GOAL_RMES.equals(goal)) {
			return exportUtils.exportAsResponse("export.odt", xmlContent,xslFile,xmlPatternRmes,zipRmes, "documentation");

		}
		if (Constants.GOAL_COMITE_LABEL.equals(goal)) {
			return exportUtils.exportAsResponse("export.odt", xmlContent,xslFile,xmlPatternLabel,zipLabel, "documentation");
		}
			
		return ResponseEntity.internalServerError().body("Goal to export is not found");
	}
	
	public ResponseEntity<Object> exportXmlFiles(Map<String, String> xmlContent, String targetType, boolean includeEmptyFields, boolean lg1,
			boolean lg2) throws RmesException {
		//Add params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, targetType);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);

		return exportUtils.exportFilesAsResponse(xmlContent);

	}
	

	public ResponseEntity<?> exportMetadataReport(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2, String goal) throws RmesException {
		Map<String,String> xmlContent = new HashMap<>();
		String targetType = getXmlContent(id, xmlContent);
		String msdXML = buildShellSims();
		xmlContent.put("msdFile", msdXML);
		return exportAsResponse(xmlContent,targetType,includeEmptyMas,lg1,lg2,goal);
	}
	

	public ResponseEntity<Object> exportMetadataReportFiles(String id, Boolean includeEmptyMas, Boolean lg1, Boolean lg2) throws RmesException {
		Map<String,String> xmlContent = new HashMap<>();
		String targetType = getXmlContent(id, xmlContent);
		String msdXML = buildShellSims();
		xmlContent.put("msdFile", msdXML);
		return exportXmlFiles(xmlContent,targetType,includeEmptyMas,lg1,lg2);
	}

	public String getXmlContent(String id, Map<String, String> xmlContent) throws RmesException {
		String emptyXML=XMLUtils.produceEmptyXML();
		Operation operation;
		Series series;
		String operationXML;
		String seriesXML = emptyXML;
		String indicatorXML;

		String[] target = parentUtils.getDocumentationTargetTypeAndId(id);
		String targetType = target[0];
		String idDatabase = target[1];

		List<String>neededCodeLists=new ArrayList<>();

		if (targetType.equals(Constants.OPERATION_UP)) {
			operation=operationsUtils.getOperationById(idDatabase);
			operationXML = XMLUtils.produceXMLResponse(operation);
			neededCodeLists.addAll(XMLUtils.getTagValues(operationXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(operationXML,Constants.ACCRUAL_PERIODICITY_LIST));
			String idSeries=operation.getSeries().getId();
			series=seriesUtils.getSeriesById(idSeries,EncodingType.XML);
			seriesXML = XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		} else {operationXML = emptyXML;}


		if (targetType.equals(Constants.INDICATOR_UP)) {
			indicatorXML=XMLUtils.produceXMLResponse(
					indicatorsUtils.getIndicatorById(idDatabase,true));
			neededCodeLists.addAll(XMLUtils.getTagValues(indicatorXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(indicatorXML,Constants.ACCRUAL_PERIODICITY_LIST));
			String idSeries=XMLUtils.getTagValues(
					XMLUtils.getTagValues(
							indicatorXML,
							Constants.WASGENERATEDBY).iterator().next(),
					Constants.ID).iterator().next();
			series=seriesUtils.getSeriesById(idSeries,EncodingType.XML);
			seriesXML = XMLUtils.produceXMLResponse(series);
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		} else {indicatorXML = emptyXML;}


		if (targetType.equals(Constants.SERIES_UP)) {
			seriesXML=XMLUtils.produceXMLResponse(
					seriesUtils.getSeriesById(idDatabase,EncodingType.XML));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.TYPELIST));
			neededCodeLists.addAll(XMLUtils.getTagValues(seriesXML,Constants.ACCRUAL_PERIODICITY_LIST));
		}

		String organizationsXML = XMLUtils.produceXMLResponse(organizationsServiceImpl.getOrganizations());

		String simsXML=XMLUtils.produceResponse(documentationsUtils.getFullSimsForXml(id), "application/xml");
		neededCodeLists.addAll(XMLUtils.getTagValues(simsXML,Constants.CODELIST));

		neededCodeLists=neededCodeLists.stream().distinct().collect(Collectors.toList());

		String codeListsXML="";
		codeListsXML=codeListsXML.concat(Constants.XML_OPEN_CODELIST_TAG);

		for(String code : neededCodeLists) {
			codeListsXML=codeListsXML.concat(XMLUtils.produceXMLResponse(codeListServiceImpl.getCodeList(code)));
		}
		codeListsXML=codeListsXML.concat(Constants.XML_END_CODELIST_TAG);


		xmlContent.put("simsFile",  simsXML);
		xmlContent.put("seriesFile",  seriesXML);
		xmlContent.put("operationFile",  operationXML);
		xmlContent.put("indicatorFile",  indicatorXML);
		xmlContent.put("codeListsFile",  codeListsXML);
		xmlContent.put("organizationsFile",  organizationsXML);
		return targetType;
	}
	

	private String buildShellSims() throws RmesException {
		MSD msd= documentationsUtils.getMSD();
		return XMLUtils.produceXMLResponse(msd);
	}


}
