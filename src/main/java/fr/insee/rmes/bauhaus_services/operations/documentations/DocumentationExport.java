package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.XsltUtils;

@Component
public class DocumentationExport {

	@Autowired
	private DocumentationsUtils documentationsUtils;
	
	@Autowired
	ExportUtils exportUtils;
	
	String xslFile = "/xslTransformerFiles/sims2fodt.xsl";
	String xmlPatternRmes = "/xslTransformerFiles/simsRmes/rmesPatternContent.xml";
	String zipRmes = "/xslTransformerFiles/simsRmes/toZipForRmes.zip";
	
	String xmlPatternLabel = "/xslTransformerFiles/simsLabel/labelPatternContent.xml";
	String zipLabel = "/xslTransformerFiles/simsLabel/toZipForLabel.zip";
	
	public Response exportAsResponse(Map<String, String> xmlContent, String targetType, boolean includeEmptyFields, boolean lg1,
			boolean lg2, String goal) throws RmesException {
		//Add two params to xmlContents
		String msdXML = documentationsUtils.buildShellSims();
		xmlContent.put("msdFile", msdXML);
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, targetType);
		xmlContent.put("parametersFile", parametersXML);
		if (Constants.GOAL_RMES.equals(goal)) {
			return exportUtils.exportAsResponse("export.odt", xmlContent,xslFile,xmlPatternRmes,zipRmes, "documentation");

		}
		if (Constants.GOAL_COMITE_LABEL.equals(goal)) {
			return exportUtils.exportAsResponse("export.odt", xmlContent,xslFile,xmlPatternLabel,zipLabel, "documentation");
		}
			
		return null;
	}


}
