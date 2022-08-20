package fr.insee.rmes.bauhaus_services.concepts.collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.*;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.XsltUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CollectionExportBuilder extends RdfService {
	
	@Autowired
	ExportUtils exportUtils;
	
	String xslFile = "/xslTransformerFiles/rmes2odt.xsl";
	String xmlPattern = "/xslTransformerFiles/collection/collectionPatternContent.xml";
	String zip = "/xslTransformerFiles/collection/toZipForCollection.zip";



	public CollectionForExport getCollectionData(String id) throws RmesException {
		CollectionForExport collection = null;
		JSONObject json = repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id));
		JSONArray members = repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id));

		// Deserialization in the `CollectionForExport` class
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		try {
			collection = mapper.readValue(json.toString(), CollectionForExport.class);
			collection.addMembers(members);
			// format specific data
			collection.setCreated(ExportUtils.toDate(collection.getCreated()));
			collection.setModified(ExportUtils.toDate(collection.getModified()));
			collection.setIsValidated(ExportUtils.toValidationStatus(collection.getIsValidated(),true));

		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}

		return collection;
	}

	public CollectionForExportODSFinal getCollectionConceptsData(String id) throws RmesException {
		CollectionForExportODS collection = null;
		CollectionForExportODSFinal sortie;

		JSONObject json = repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id));

		HashMap<String, Object> params = new HashMap<>();
		params.put("CONCEPT_GRAPH", config.getConceptsGraph());
		params.put("STRUCTURES_COMPONENTS_GRAPH", config.getStructuresComponentsGraph());
		params.put("COLLECTION_ID", id);
		JSONArray members = repoGestion.getResponseAsArray(buildRequest("getCollectionConcepts.ftlh", params));

		// Deserialization in the `CollectionForExport` class
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		ObjectMapper mapper2 = new ObjectMapper();

		try {

			collection = mapper.readValue(json.toString(), CollectionForExportODS.class);
			Conceptfield[] membersConcept = mapper2.readValue(members.toString(), Conceptfield[].class);

			List<Conceptfield> membersLg1 = new ArrayList<>();
			List<Conceptfield> membersLg2 = new ArrayList<>();

			for (Conceptfield byPreflabel : membersConcept) {

				Conceptfield conceptLg1 = new Conceptfield(byPreflabel.getId(),byPreflabel.getPrefLabelLg1(),byPreflabel.getCreator(),
						byPreflabel.getContributor(),byPreflabel.getDisseminationStatus(),byPreflabel.getAdditionalMaterial(),byPreflabel.getCreated(),
						byPreflabel.getModified(),byPreflabel.getValid(),byPreflabel.getConceptVersion(),byPreflabel.getIsValidated(),byPreflabel.getDefcourteLg1()
				);

				conceptLg1.setCreated(ExportUtils.toDate(conceptLg1.getCreated()));
				conceptLg1.setModified(ExportUtils.toDate(conceptLg1.getModified()));
				conceptLg1.setIsValidated(ExportUtils.toValidationStatus(conceptLg1.getIsValidated(), true));
				conceptLg1.setDisseminationStatus((ExportUtils.toLabel(conceptLg1.getDisseminationStatus())));

				Conceptfield conceptLg2 = new Conceptfield.UserBuilder(byPreflabel.getId()).setPrefLabelLg2(byPreflabel.getPrefLabelLg2())
						.setContributor(byPreflabel.getContributor()).setDisseminationStatus(byPreflabel.getDisseminationStatus())
						.setAdditionalMaterial(byPreflabel.getAdditionalMaterial()).setCreated(byPreflabel.getCreated())
						.setModified(byPreflabel.getModified()).setValid(byPreflabel.getValid()).setConceptVersion(byPreflabel.getConceptVersion())
						.setIsValidated(byPreflabel.getIsValidated()).setDefcourteLg2(byPreflabel.getDefcourteLg2())
						.build();

				conceptLg2.setCreated(ExportUtils.toDate(conceptLg2.getCreated()));
				conceptLg2.setModified(ExportUtils.toDate(conceptLg2.getModified()));
				conceptLg2.setIsValidated(ExportUtils.toValidationStatus(conceptLg2.getIsValidated(), true));
				conceptLg2.setDisseminationStatus(ExportUtils.toLabel(conceptLg1.getDisseminationStatus()));

				if (byPreflabel.getId() != null) {
					membersLg1.add(conceptLg1);
					membersLg2.add(conceptLg2);

				}
			}
			// format specific data

			collection.setCreated(ExportUtils.toDate(collection.getCreated()));
			collection.setModified(ExportUtils.toDate(collection.getModified()));
			collection.setIsValidated(ExportUtils.toValidationStatus(collection.getIsValidated(), true));
			sortie = new CollectionForExportODSFinal(collection.getId(),collection.getPrefLabelLg1(),collection.getPrefLabelLg2(),collection.getCreator(),
					collection.getContributor(),collection.getDisseminationStatus(),collection.getAdditionalMaterial(),collection.getCreated(),
					collection.getModified(),collection.getValid(),collection.getConceptVersion(),collection.getIsValidated(),collection.getDescriptionLg1(),collection.getDescriptionLg2(), membersLg1, membersLg2);


		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}


		return sortie;

	}


	private static String buildRequest(String fileName, HashMap<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("collections/", fileName, params);
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
