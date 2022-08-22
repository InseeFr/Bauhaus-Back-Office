package fr.insee.rmes.bauhaus_services.concepts.collections;

import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.XsltUtils;

@Component
public class CollectionExportBuilder extends RdfService {
	
	@Autowired
	ExportUtils exportUtils;
	
	String xslFile = "/xslTransformerFiles/rmes2odt.xsl";
	String xmlPattern = "/xslTransformerFiles/collection/collectionPatternContent.xml";
	String zip = "/xslTransformerFiles/collection/toZipForCollection.zip";
	final Collator instance = Collator.getInstance();

	public CollectionForExport getCollectionData(String id) throws RmesException {
		CollectionForExport collection = null;
		JSONObject json = repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id));
		JSONArray members = repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id));

		List<JSONObject> orderMembers = new ArrayList<>();
		for (int i = 0; i < members.length(); i++) {
			orderMembers.add(members.getJSONObject(i));
		}

		instance.setStrength(Collator.NO_DECOMPOSITION);

		Collections.sort( orderMembers, new Comparator<JSONObject>() {
			private static final String KEY_NAME = "prefLabelLg1";

			@Override
			public int compare(JSONObject a, JSONObject b) {
				String valA = (String) a.get(KEY_NAME);
				String valB = (String) b.get(KEY_NAME);

				return instance.compare(valA.toLowerCase(), valB.toLowerCase());
			}
		});


		JSONArray orderMembersJSONArray = new JSONArray(orderMembers);

		// Deserialization in the `CollectionForExport` class
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		try {
			collection = mapper.readValue(json.toString(), CollectionForExport.class);
			collection.addMembers(orderMembersJSONArray);

			// format specific data
			collection.setCreated(ExportUtils.toDate(collection.getCreated()));
			collection.setModified(ExportUtils.toDate(collection.getModified()));
			collection.setIsValidated(ExportUtils.toValidationStatus(collection.getIsValidated(),true));

		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}

		return collection;
	}


		

	public ResponseEntity<Resource> exportAsResponse(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		// Add two params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		
		return exportUtils.exportAsResponse(fileName, xmlContent,xslFile,xmlPattern,zip, Constants.COLLECTION);
	}

	public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		// Add two params to xmlContents
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		
		return exportUtils.exportAsInputStream(fileName, xmlContent,xslFile,xmlPattern,zip, Constants.COLLECTION);
	}

}
