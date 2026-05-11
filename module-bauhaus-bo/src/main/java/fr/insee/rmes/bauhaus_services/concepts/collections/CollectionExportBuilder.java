package fr.insee.rmes.bauhaus_services.concepts.collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.CollectionForExportOld;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XsltUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class CollectionExportBuilder extends RdfService {

	private final ExportUtils exportUtils;

	private final ConceptCollectionsQueries conceptCollectionsQueries;

	public CollectionExportBuilder(RepositoryGestion repoGestion, IdGenerator idGenerator,
								   RepositoryPublication repositoryPublication,
								   PublicationUtils publicationUtils,
								   ExportUtils exportUtils, ConceptCollectionsQueries conceptCollectionsQueries) {
		super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
		this.exportUtils = exportUtils;
		this.conceptCollectionsQueries = conceptCollectionsQueries;
	}

	private static final String XSL_FILE = "/xslTransformerFiles/rmes2odt.xsl";
	private static final String XML_PATERN = "/xslTransformerFiles/collection/collectionPatternContent.xml";
	private static final String ZIP = "/xslTransformerFiles/collection/toZipForCollection.zip";
	private static final String ZIP_OLD = "/xslTransformerFiles/collection/toZipForCollectionOld.zip";
	private static final String XML_PATTERN_FR = "/xslTransformerFiles/collection/collectionFrPatternContent.xml";
	private static final String XML_PATTERN_EN = "/xslTransformerFiles/collection/collectionEnPatternContent.xml";
	private static final String XML_PATTERN_ODS = "/xslTransformerFiles/collection/collectionOdsPatternContent.xml";
	private static final String ZIP_ODS = "/xslTransformerFiles/collection/toZipForCollectionOds.zip";

	final Collator instance = Collator.getInstance();

	private static final String CONTENT_TYPE = "Content-Type";


	public CollectionForExport getCollectionData(String id) throws RmesException {
		CollectionForExport collection;
		JSONObject json = repoGestion.getResponseAsObject(conceptCollectionsQueries.collectionQuery(id));
		JSONArray members = repoGestion.getResponseAsArray(conceptCollectionsQueries.collectionConceptsQuery(id));

		List<JSONObject> orderMembers = new ArrayList<>();
		for (int i = 0; i < members.length(); i++) {
			orderMembers.add(members.getJSONObject(i));
		}

		instance.setStrength(Collator.NO_DECOMPOSITION);

		Collections.sort( orderMembers, new Comparator<>() {
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
			collection.setCreated(DateUtils.toDate(collection.getCreated()));
			collection.setModified(DateUtils.toDate(collection.getModified()));
			collection.setIsValidated(ExportUtils.toValidationStatus(collection.getIsValidated(),true));

		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}

		return collection;
	}


	public ResponseEntity<Resource> exportAsResponse(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		return exportUtils.exportAsODT(fileName, xmlContent,XSL_FILE,XML_PATERN,ZIP_OLD, Constants.COLLECTION);
	}


	public ResponseEntity<Resource> exportAsResponseODT(String fileName, Map<String, String> xmlContent, boolean includeEmptyFields, Language lg) throws RmesException {
		String parametersXML = XsltUtils.buildParams(true, true, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		String xmlPattern = lg == Language.lg1 ? XML_PATTERN_FR : XML_PATTERN_EN;
		return exportUtils.exportAsODT(fileName, xmlContent, XSL_FILE, xmlPattern, ZIP, Constants.COLLECTION);
	}

	public ResponseEntity<Resource> exportAsResponseODS(String fileName, Map<String, String> xmlContent, boolean lg1, boolean lg2, boolean includeEmptyFields) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		xmlContent.put(Constants.PARAMETERS_FILE, parametersXML);
		return exportUtils.exportAsODS(fileName, xmlContent,XSL_FILE,XML_PATTERN_ODS,ZIP_ODS, Constants.COLLECTION);
	}

	public void exportMultipleCollectionsAsZipOdt(Map<String, Map<String, String>> collections, boolean lg1, boolean lg2, boolean includeEmptyFields, HttpServletResponse response, Language lg, Map<String, Map<String, InputStream>> concepts, boolean withConcepts) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		collections.values().stream().forEach(collection -> collection.put(Constants.PARAMETERS_FILE, parametersXML));
		String xmlPattern = lg == Language.lg1 ? XML_PATTERN_FR : XML_PATTERN_EN;
		exportMultipleResourceAsZip(collections,XSL_FILE,xmlPattern,ZIP, response, FilesUtils.ODT_EXTENSION, concepts, withConcepts);
	}

	public void exportMultipleCollectionsAsZipOds(Map<String, Map<String, String>> collections, boolean lg1, boolean lg2, boolean includeEmptyFields, HttpServletResponse response, Map<String, Map<String, InputStream>> concepts, boolean withConcepts) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		collections.values().stream().forEach(collection -> collection.put(Constants.PARAMETERS_FILE, parametersXML));
		exportMultipleResourceAsZip(collections,XSL_FILE,XML_PATTERN_ODS, ZIP_ODS, response, FilesUtils.ODS_EXTENSION, concepts, withConcepts);
	}

	public byte[] buildOdtZipBytes(Map<String, Map<String, String>> collections, boolean lg1, boolean lg2, boolean includeEmptyFields, Language lg, Map<String, Map<String, InputStream>> concepts, boolean withConcepts) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		collections.values().forEach(c -> c.put(Constants.PARAMETERS_FILE, parametersXML));
		String xmlPattern = lg == Language.lg1 ? XML_PATTERN_FR : XML_PATTERN_EN;
		return buildMultipleResourceZipBytes(collections, XSL_FILE, xmlPattern, ZIP, FilesUtils.ODT_EXTENSION, concepts, withConcepts);
	}

	public byte[] buildOdsZipBytes(Map<String, Map<String, String>> collections, boolean lg1, boolean lg2, boolean includeEmptyFields, Map<String, Map<String, InputStream>> concepts, boolean withConcepts) throws RmesException {
		String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.COLLECTION);
		collections.values().forEach(c -> c.put(Constants.PARAMETERS_FILE, parametersXML));
		return buildMultipleResourceZipBytes(collections, XSL_FILE, XML_PATTERN_ODS, ZIP_ODS, FilesUtils.ODS_EXTENSION, concepts, withConcepts);
	}

	private void exportMultipleResourceAsZip(Map<String, Map<String, String>> resources, String xslFile, String xmlPattern, String zip, HttpServletResponse response, String extension, Map<String, Map<String, InputStream>> concepts, boolean withConcepts) throws RmesException {

		String zipFileName = computeZipFileName(resources);

		response.addHeader(HttpHeaders.ACCEPT, "*/*");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
		response.addHeader(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Access-Control-Allow-Origin, Access-Control-Allow-Credentials");

		try {
			writeMultipleResourceAsZip(resources, xslFile, xmlPattern, zip, response.getOutputStream(), extension, concepts, withConcepts, zipFileName);
		} catch (IOException e1) {
			throw new RmesException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "IOException on " + zipFileName, e1.getMessage());
		}
	}

	public byte[] buildMultipleResourceZipBytes(Map<String, Map<String, String>> resources, String xslFile, String xmlPattern, String zip, String extension, Map<String, Map<String, InputStream>> concepts, boolean withConcepts) throws RmesException {
		String zipFileName = computeZipFileName(resources);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			writeMultipleResourceAsZip(resources, xslFile, xmlPattern, zip, baos, extension, concepts, withConcepts, zipFileName);
		} catch (IOException e) {
			throw new RmesException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "IOException on " + zipFileName, e.getMessage());
		}
		return baos.toByteArray();
	}

	public String computeZipFileName(Map<String, Map<String, String>> resources) {
		if (resources.size() == 1) {
			return resources.keySet().iterator().next() + FilesUtils.ZIP_EXTENSION;
		}
		return "collections" + FilesUtils.ZIP_EXTENSION;
	}

	private void writeMultipleResourceAsZip(Map<String, Map<String, String>> resources, String xslFile, String xmlPattern, String zip, OutputStream output, String extension, Map<String, Map<String, InputStream>> concepts, boolean withConcepts, String zipFileName) throws IOException, RmesException {
		try (ZipOutputStream zos = new ZipOutputStream(output)) {
			if (withConcepts && concepts != null) {
				addCollectionsConcepts(concepts, zos);
			} else {
				for (String key : resources.keySet()) {
					InputStream input = exportUtils.exportAsInputStream(key.replace(extension, ""), resources.get(key), xslFile, xmlPattern, zip, Constants.COLLECTION, FilesUtils.ODS_EXTENSION);
					if (input == null) {
						throw new RmesException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Can't generate codebook", "Stream is null on " + zipFileName);
					}
					this.addZipEntry("", key + extension, input, zos);
				}
			}
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
				this.addZipEntry(collectionName + "/", key + FilesUtils.ODT_EXTENSION, concepts.get(key), zipOutputStreamStream);
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
		CollectionForExportOld collection;
		JSONObject json = repoGestion.getResponseAsObject(conceptCollectionsQueries.collectionQuery(id));
		JSONArray members = repoGestion.getResponseAsArray(conceptCollectionsQueries.collectionMembersQuery(id));

		List<JSONObject> orderMembers = new ArrayList<>();
		for (int i = 0; i < members.length(); i++) {
			orderMembers.add(members.getJSONObject(i));
		}

		instance.setStrength(Collator.NO_DECOMPOSITION);

		Collections.sort( orderMembers, new Comparator<>() {
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
			collection.setCreated(DateUtils.toDate(collection.getCreated()));
			collection.setModified(DateUtils.toDate(collection.getModified()));
			collection.setIsValidated(ExportUtils.toValidationStatus(collection.getIsValidated(),true));

		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}

		return collection;
	}

}
