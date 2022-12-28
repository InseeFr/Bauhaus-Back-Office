package fr.insee.rmes.bauhaus_services.concepts.collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.CollectionForExportOld;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.XsltUtils;
import fr.insee.rmes.webservice.ConceptsCollectionsResources;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class CollectionExportBuilder extends RdfService {
	
	@Autowired
	ExportUtils exportUtils;
	
	String xslFile = "/xslTransformerFiles/rmes2odt.xsl";
	String xmlPattern = "/xslTransformerFiles/collection/collectionPatternContent.xml";
	String zip = "/xslTransformerFiles/collection/toZipForCollection.zip";
	String zipold = "/xslTransformerFiles/collection/toZipForCollectionOld.zip";
	final Collator instance = Collator.getInstance();

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String ATTACHMENT = "attachment";
	private static final String ODT_EXTENSION = ".odt";
	private static final String ODS_EXTENSION = ".ods";
	private static final String ZIP_EXTENSION = ".zip";


	String xmlPatternFR = "/xslTransformerFiles/collection/collectionFrPatternContent.xml";
	String xmlPatternEN = "/xslTransformerFiles/collection/collectionEnPatternContent.xml";
	String xmlPatternODS = "/xslTransformerFiles/collection/collectionOdsPatternContent.xml";
	String zipODS = "/xslTransformerFiles/collection/toZipForCollectionOds.zip";
	
	

	public CollectionForExport getCollectionData(String id) throws RmesException {
		CollectionForExport collection = null;
		JSONObject json = repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id));
		JSONArray members = repoGestion.getResponseAsArray(CollectionsQueries.collectionConceptsQuery(id));

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
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		return exportUtils.exportAsResponse(fileName, xmlContent,xslFile,xmlPattern,zipold, Constants.COLLECTION);
	}


	public ResponseEntity<Resource> exportAsResponseODT(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields, ConceptsCollectionsResources.Language lg) throws RmesException {
		String parametersXML = XsltUtils.buildParams(true, true, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		String xmlPattern = lg == ConceptsCollectionsResources.Language.lg1 ? xmlPatternFR : xmlPatternEN;
		return exportUtils.exportAsResponse(fileName, xmlContent, xslFile, xmlPattern, zip, Constants.COLLECTION);
	}

	public ResponseEntity<Resource> exportAsResponseODS(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		return exportUtils.exportAsResponseODS(fileName, xmlContent,xslFile,xmlPatternODS,zipODS, Constants.COLLECTION);
	}

	public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		return exportUtils.exportAsInputStream(fileName, xmlContent,xslFile,xmlPattern,zip, Constants.COLLECTION);
	}

	public void exportMultipleCollectionsAsZipOdt(Map<String, Map<String, String>> collections, boolean lg1, boolean lg2, boolean includeEmptyFields, HttpServletResponse response, ConceptsCollectionsResources.Language lg, Map<String, Map<String, InputStream>> concepts) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		collections.values().stream().forEach(collection -> collection.put(Constants.PARAMETERS_FILE, parametersXML));
		String xmlPattern = lg == ConceptsCollectionsResources.Language.lg1 ? xmlPatternFR : xmlPatternEN;
		exportMultipleResourceAsZip(collections,xslFile,xmlPattern,zip, response, ODT_EXTENSION, concepts);
	}

	public void exportMultipleCollectionsAsZipOds(Map<String, Map<String, String>> collections, boolean lg1, boolean lg2, boolean includeEmptyFields, HttpServletResponse response, Map<String, Map<String, InputStream>> concepts) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		collections.values().stream().forEach(collection -> collection.put(Constants.PARAMETERS_FILE, parametersXML));
		exportMultipleResourceAsZip(collections,xslFile,xmlPatternODS, zip, response, ODS_EXTENSION, concepts);
	}

	private void exportMultipleResourceAsZip(Map<String, Map<String, String>> resources, String xslFile, String xmlPattern, String zip, HttpServletResponse response, String extension, Map<String, Map<String, InputStream>> concepts) throws RmesException {

		String zipFileName = "collections" + ZIP_EXTENSION;

		/**
		 * If we want to create an archive with only one collection, we name the archive after it.
		 */
		if(resources.size() == 1){
			zipFileName = resources.keySet().iterator().next() + ZIP_EXTENSION;
		}

		response.addHeader(HttpHeaders.ACCEPT, "*/*");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
		response.addHeader(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Access-Control-Allow-Origin, Access-Control-Allow-Credentials");

		try (ZipOutputStream zipOutputStreamStream = new ZipOutputStream(response.getOutputStream())) {
			Iterator<String> resourceIterator = resources.keySet().iterator();
			while (resourceIterator.hasNext()) {
				String key = resourceIterator.next();
				InputStream input = exportUtils.exportAsInputStream(key.replace(extension, ""), resources.get(key), xslFile, xmlPattern, zip, Constants.COLLECTION);
				if (input == null)
					throw new RmesException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Can't generate codebook", "Stream is null");

				this.addZipEntry("", key + extension, input, zipOutputStreamStream);
			}
			if(concepts != null){
				addCollectionsConcepts(concepts, zipOutputStreamStream);
			}
		} catch (IOException e1) {
			throw new RmesException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "IOException on " + zipFileName, e1.getMessage());
		}
	}

	private void addCollectionsConcepts(Map<String, Map<String, InputStream>> collectionsConcepts, ZipOutputStream zipOutputStreamStream) throws IOException {
		Iterator<String> collectionIterator = collectionsConcepts.keySet().iterator();
		while (collectionIterator.hasNext()) {
			String collectionName = collectionIterator.next();
			Map<String, InputStream> concepts = collectionsConcepts.get(collectionName);

			Iterator<String> conceptsIterator = concepts.keySet().iterator();
			while (conceptsIterator.hasNext()) {
				String key = conceptsIterator.next();
				this.addZipEntry(collectionName + "/", key + ODT_EXTENSION, concepts.get(key), zipOutputStreamStream);
			}
		}
	}

	private void addZipEntry(String folder, String filename, InputStream input, ZipOutputStream zos) throws IOException {
		ZipEntry entry = new ZipEntry(folder + filename);
		zos.putNextEntry(entry);
		input.transferTo(zos);
		zos.closeEntry();
	}

	public CollectionForExportOld getCollectionDataOld(String id) throws RmesException {
		CollectionForExportOld collection = null;
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
			collection = mapper.readValue(json.toString(), CollectionForExportOld.class);
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

}
