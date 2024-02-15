package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionsUtils;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.CollectionForExportOld;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
import org.apache.commons.text.CaseUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConceptsImpl  extends RdfService implements ConceptsService {

	private static final String THE_CONCEPT = "The concept ";


	static final Logger logger = LoggerFactory.getLogger(ConceptsImpl.class);

	
	@Autowired 
	ConceptsUtils conceptsUtils;

	@Autowired 
	CollectionsUtils collectionsUtils;

	@Autowired 
	ConceptsExportBuilder conceptsExport;
	
	@Autowired 
	CollectionExportBuilder collectionExport;

	@Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength;


	@Override
	public String getConcepts()  throws RmesException{
		logger.info("Starting to get concepts list");
		String resQuery = repoGestion.getResponseAsArray(ConceptsQueries.conceptsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getConceptsSearch()  throws RmesException{
		logger.info("Starting to get concepts list for advanced search");
		return repoGestion.getResponseAsArray(ConceptsQueries.conceptsSearchQuery()).toString();
	}

	@Override
	public String getConceptsToValidate()  throws RmesException{
		logger.info("Starting to get provisionals concepts list");
		return repoGestion.getResponseAsArray(ConceptsQueries.conceptsToValidateQuery()).toString();
	}
	
	@Override
	public String getConceptByID(String id)  throws RmesException{
		JSONObject concept = conceptsUtils.getConceptById(id);
		return concept.toString();
	}
	
	@Override
	public String getRelatedConcepts(String id)  throws RmesException{
		String uriConcept = RdfUtils.toString(RdfUtils.objectIRI(ObjectType.CONCEPT,id));
		JSONArray resQuery = conceptsUtils.getRelatedConcepts(uriConcept);
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}


	/**
	 * @param id
	 * @return String
	 * @throws RmesException
	 */	
	@Override
	public String deleteConcept(String id) throws RmesException {
		String uriConcept = RdfUtils.toString(RdfUtils.objectIRI(ObjectType.CONCEPT,id));
		JSONArray graphArray = conceptsUtils.getGraphsWithConcept(uriConcept);

		/* check concept isn't used in several graphs */
		if (graphArray.length()>1) {
			String listGraphs="";
			/* list the graphs involved in log */
			for (int i=0; i<graphArray.length(); i++) {
				JSONObject currentGraph=(JSONObject) graphArray.get(i);
				listGraphs = listGraphs.concat(currentGraph.getString("src"));
				listGraphs = listGraphs.concat("-");
			}
			 
			JSONObject details = new JSONObject();
			details.put("idConcept", id);
			details.put("graphs", graphArray);
			throw new RmesUnauthorizedException(ErrorCodes.CONCEPT_DELETION_SEVERAL_GRAPHS,
					THE_CONCEPT+id+" cannot be deleted because it is used in several graphs.",
					details);
			
		}
		/* Check concept has no link */
		String listConcepts=getRelatedConcepts(id);
		if(!listConcepts.equals("[]")) { 
			JSONObject details = new JSONObject();
			details.put("idConcept", id);
			details.put("linkedConcepts", listConcepts);
			throw new RmesUnauthorizedException(
					ErrorCodes.CONCEPT_DELETION_LINKED,
					THE_CONCEPT+id+" cannot be deleted because it is linked to other concepts.",
					details);
		}
		/* deletion */
		HttpStatus result= conceptsUtils.deleteConcept(id);
		String successMessage=THE_CONCEPT+id+" has been deleted from graph "+RdfUtils.conceptGraph();
		if (result!= HttpStatus.OK) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unexpected return message: ",result.toString());
		} else { 
			return successMessage;
		}
	}

	@Override
	public String getConceptLinksByID(String id)  throws RmesException{
		return repoGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id)).toString();
	}

	@Override
	public String getConceptNotesByID(String id, int conceptVersion)  throws RmesException{
		return repoGestion.getResponseAsObject(ConceptsQueries.conceptNotesQuery(id, conceptVersion)).toString();
	}

	@Override
	public String getCollectionsToValidate()  throws RmesException{
		return repoGestion.getResponseAsArray(CollectionsQueries.collectionsToValidateQuery()).toString();
	}


	/**
	 * Create new concept
	 * @throws RmesException 
	 */
	@Override
	public String setConcept(String body) throws RmesException {
		return conceptsUtils.setConcept(body);
	}


	/**
	 * Modify concept
	 * @throws RmesException 
	 */
	@Override
	public void setConcept(String id, String body) throws RmesException {
		conceptsUtils.setConcept(id, body);
	}


	/**
	 * Create new collection
	 * @throws RmesException 
	 */
	@Override
	public void setCollection(String body) throws RmesException {
		collectionsUtils.setCollection(body);
	}
	//	
	/**
	 * Modify collection
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 * @throws Exception 
	 */
	@Override
	public void setCollection(String id, String body) throws RmesException {
		collectionsUtils.setCollection(id, body);
	}

	/**
	 * Validate concept(s)
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 */
	@Override
	public void setConceptsValidation(String body) throws  RmesException  {
		conceptsUtils.conceptsValidation(body);
	}

	/**
	 * Export concept(s)
	 */
	@Override
	public ResponseEntity<?> exportConcept(String id, String acceptHeader) throws RmesException {
		ConceptForExport concept;
		try {
			concept = conceptsExport.getConceptData(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}

		Map<String, String> xmlContent = convertConceptInXml(concept);
		String fileName = getFileNameForExport(concept);
		return conceptsExport.exportAsResponse(fileName,xmlContent,true,true,true);
	}

	@Override
	public void exportZipConcept(String ids, String acceptHeader, HttpServletResponse response) throws RmesException {
		Map<String, Map<String, String>> concepts = new HashMap<>();
		Arrays.asList(ids.split(",")).forEach(id -> {
			try {
				ConceptForExport concept = conceptsExport.getConceptData(id);
				Map<String, String> xmlContent = convertConceptInXml(concept);
				String fileName = getFileNameForExport(concept);
				concepts.put(fileName, xmlContent);
			} catch (RmesException e) {
				logger.error(e.getMessageAndDetails());
			}
		});
		conceptsExport.exportMultipleConceptsAsZip(concepts, true, true, true, response);
	}

	private String getFileNameForExport(ConceptForExport concept) {
		return FilesUtils.reduceFileNameSize(CaseUtils.toCamelCase(concept.getPrefLabelLg1(), false) + "-" + concept.getId(), maxLength);
	}
	
	@Override
	public Map<String, InputStream> getConceptsExportIS(List<String> ids) throws RmesException {
		Map<String,InputStream> ret = new HashMap<>();
		ids.parallelStream().forEach(id -> {
			try {
				ConceptForExport concept = conceptsExport.getConceptData(id);
				Map<String, String> xmlContent = convertConceptInXml(concept);
				String fileName = getFileNameForExport(concept);
				ret.put(fileName, conceptsExport.exportAsInputStream(fileName,xmlContent,true,true,true));
			} catch (RmesException e) {
				e.printStackTrace();
			}
		});
		return ret;
	}

	private Map<String, String> convertConceptInXml(ConceptForExport concept) {
		String conceptXml = XMLUtils.produceXMLResponse(concept);
		Map<String,String> xmlContent = new HashMap<>();
		xmlContent.put("conceptFile",  conceptXml.replace("ConceptForExport", "Concept"));
		return xmlContent;
	}
	
	private Map<String, String> convertCollectionInXml(CollectionForExport collection) {
		String collectionXml = XMLUtils.produceXMLResponse(collection);
		Map<String,String> xmlContent = new HashMap<>();
		xmlContent.put("collectionFile",  collectionXml.replace("CollectionForExport", "Collection"));
		return xmlContent;
	}
	

	/**
	 * Validate collection(s)
	 * @throws RmesException 
	 * @throws RmesUnauthorizedException 
	 * @throws Exception 
	 */
	@Override
	public void setCollectionsValidation(String body) throws  RmesException   {
		collectionsUtils.collectionsValidation(body);
	}

	@Override
	public ResponseEntity<?> getCollectionExport(String id, String acceptHeader) throws RmesException{
		CollectionForExportOld collection;
		try {
			collection = collectionExport.getCollectionDataOld(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		Map<String, String> xmlContent = convertCollectionInXml(collection);	
		String fileName = FilesUtils.reduceFileNameSize(CaseUtils.toCamelCase(collection.getPrefLabelLg1(), false) + "-" + collection.getId(), maxLength);
		return collectionExport.exportAsResponse(fileName,xmlContent,true,true,true);
	}

}
