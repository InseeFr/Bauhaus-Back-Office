package fr.insee.rmes.bauhaus_services.concepts.concepts;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.XsltUtils;

@Component
public class ConceptsExportBuilder extends RdfService {

	private static final String CONCEPT_VERSION = "conceptVersion";
	@Autowired
	ConceptsUtils conceptsUtils;

	@Autowired
	ExportUtils exportUtils;
	
	String xslFile = "/xslTransformerFiles/rmes2odt.xsl";
	String xmlPattern = "/xslTransformerFiles/concept/conceptPatternContent.xml";
	String zip = "/xslTransformerFiles/concept/toZipForConcept.zip";
	
	private void transformAltLabelListInString(JSONObject general) {
		if (general.has(Constants.ALT_LABEL_LG1)) {
			general.put(Constants.ALT_LABEL_LG1,
					JSONUtils.jsonArrayOfStringToString(general.getJSONArray(Constants.ALT_LABEL_LG1)));
		} else {
			general.remove(Constants.ALT_LABEL_LG1);
		}
		if (general.has(Constants.ALT_LABEL_LG2)) {
			general.put(Constants.ALT_LABEL_LG2,
					JSONUtils.jsonArrayOfStringToString(general.getJSONArray(Constants.ALT_LABEL_LG2)));
		} else {
			general.remove(Constants.ALT_LABEL_LG2);
		}
	}

	public ConceptForExport getConceptData(String id) throws RmesException {
		ConceptForExport concept = null;
		JSONObject general = conceptsUtils.getConceptById(id);
		transformAltLabelListInString(general);

		JSONArray links = repoGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id));
		JSONObject notes = repoGestion.getResponseAsObject(
				ConceptsQueries.conceptNotesQuery(id, Integer.parseInt(general.getString(CONCEPT_VERSION))));

		// Deserialization in the `ConceptForExport` class
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		try {
			concept = mapper.readValue(general.toString(), ConceptForExport.class);
			concept.addLinks(links);
			concept.addNotes(notes);

			// format specific data
			concept.setIsValidated(ExportUtils.toValidationStatus(concept.getIsValidated(),false));
			concept.setDisseminationStatus(ExportUtils.toLabel(concept.getDisseminationStatus()));
			concept.setCreated(ExportUtils.toDate(concept.getCreated()));
			concept.setModified(ExportUtils.toDate(concept.getModified()));
			concept.setValid(ExportUtils.toDate(concept.getValid()));

		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}
		return concept;

	}

	public ResponseEntity<Object>  exportAsResponse(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		// Add two params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.CONCEPT);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		
		return exportUtils.exportAsResponse(fileName, xmlContent,xslFile,xmlPattern,zip, Constants.CONCEPT);
	}

	public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		// Add two params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.CONCEPT);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		
		return exportUtils.exportAsInputStream(fileName, xmlContent,xslFile,xmlPattern,zip, Constants.CONCEPT);
	}
	
}
