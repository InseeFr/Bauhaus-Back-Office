package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionsUtils;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.concepts.*;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.port.serverside.concepts.CollectionRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.infrastructure.utils.DiacriticSorter;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ConceptsImpl  extends RdfService implements ConceptsService {

	private static final String THE_CONCEPT = "The concept ";

	static final Logger logger = LoggerFactory.getLogger(ConceptsImpl.class);

	private final ConceptsUtils conceptsUtils;

	private final CollectionsUtils collectionsUtils;

	private final ConceptsExportBuilder conceptsExport;

	private final CollectionExportBuilder collectionExport;
	private final CollectionRepository collectionRepository;

	private final int maxLength;

    public ConceptsImpl(
			ConceptsUtils conceptsUtils,
			CollectionsUtils collectionsUtils,
			ConceptsExportBuilder conceptsExport,
			CollectionExportBuilder collectionExport,
			CollectionRepository collectionRepository,
			@Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength) {
        this.conceptsUtils = conceptsUtils;
        this.collectionsUtils = collectionsUtils;
        this.conceptsExport = conceptsExport;
        this.collectionExport = collectionExport;
		this.collectionRepository = collectionRepository;
        this.maxLength = maxLength;
    }


    @Override
	public List<PartialConcept> getConcepts()  throws RmesException {
		logger.info("Starting to get concepts list");

		var concepts = repoGestion.getResponseAsArray(ConceptsQueries.conceptsQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(concepts,
				PartialConcept[].class,
				PartialConcept::label);

	}

	@Override
	public List<ConceptForAdvancedSearch> getConceptsSearch()  throws RmesException{
		logger.info("Starting to get concepts list for advanced search");
		var concepts = repoGestion.getResponseAsArray(ConceptsQueries.conceptsSearchQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(concepts,
				ConceptForAdvancedSearch[].class,
				ConceptForAdvancedSearch::label);
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
	public void deleteConcept(String id) throws RmesException {
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
			throw new RmesBadRequestException(ErrorCodes.CONCEPT_DELETION_SEVERAL_GRAPHS,
					THE_CONCEPT+id+" cannot be deleted because it is used in several graphs.",
					details);
			
		}
		/* Check concept has no link */
		String listConcepts=getRelatedConcepts(id);
		if(!listConcepts.equals("[]")) { 
			JSONObject details = new JSONObject();
			details.put("idConcept", id);
			details.put("linkedConcepts", listConcepts);
			throw new RmesBadRequestException(
					ErrorCodes.CONCEPT_DELETION_LINKED,
					THE_CONCEPT+id+" cannot be deleted because it is linked to other concepts.",
					details);
		}
		/* deletion */
		HttpStatus result= conceptsUtils.deleteConcept(id);
		if (result!= HttpStatus.OK) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unexpected return message: ",result.toString());
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
	 *
	 * @return
	 * @throws RmesException
	 */
	@Override
	public String createCollection(Collection collection) throws RmesException {
		collection.setId(idGenerator.generateNextId());
		return collectionRepository.save(collection);
	}
	//	
	/**
	 * Modify collection
	 *
	 * @return
	 * @throws RmesException
	 * @throws RmesUnauthorizedException
	 * @throws Exception
	 */
	@Override
	public void updateCollection(String id, Collection collection) throws RmesException {
		collectionRepository.save(collection);
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
	public void exportZipConcept(String ids, String acceptHeader, HttpServletResponse response, Language lg, String type, boolean withConcepts) throws RmesException {
		Map<String, Map<String, String>> collections = new HashMap<>();
		Map<String, Map<String, InputStream>> collectionsConcepts = new HashMap<>();

		CollectionForExport collection = new CollectionForExport();
		collection.setId("custom");
		collection.setPrefLabelLg1("Liste de Concepts");
		collection.setPrefLabelLg2("Concepts List");


		List<String> conceptsIds = Arrays.asList(ids.split("_AND_"));
		List<MembersLg> members = new ArrayList<>();
		Map<String, InputStream> concepts = getConceptsExportIS(conceptsIds, members);
		collection.setMembersLg(members);


		Map<String, String> xmlContent = convertCollectionInXml(collection);
		String fileName = getFileNameForExport(collection, lg);
		collections.put(fileName, xmlContent);

		if(withConcepts){
			collectionsConcepts.put(fileName, concepts);
		}

		if("odt".equalsIgnoreCase(type)){
			collectionExport.exportMultipleCollectionsAsZipOdt(collections, true, true, true, response, lg, collectionsConcepts, withConcepts);

		} else {
			collectionExport.exportMultipleCollectionsAsZipOds(collections, true, true, true, response, collectionsConcepts, withConcepts);
		}
	}

	public String getFileNameForExport(CollectionForExport collection, Language lg){
		if (lg == Language.lg2){
			return FilesUtils.generateFinalFileNameWithoutExtension(collection.getId() + "-" + collection.getPrefLabelLg2(), this.maxLength);
		}
		return FilesUtils.generateFinalFileNameWithoutExtension(collection.getId() + "-" + collection.getPrefLabelLg1(), this.maxLength);
	}

	private String getFileNameForExport(ConceptForExport concept) {
		return FilesUtils.generateFinalFileNameWithoutExtension(concept.getId() + "-" + concept.getPrefLabelLg1(), maxLength);
	}

	private MembersLg convertConceptIntoMembers(ConceptForExport concept){
		MembersLg member = new MembersLg();
		member.setId(concept.getId());
		member.setCreator(concept.getCreator());
		member.setPrefLabelLg1(concept.getPrefLabelLg1());
		member.setPrefLabelLg2(concept.getPrefLabelLg2());
		member.setIsValidated(concept.getIsValidated());
		member.setCreated(concept.getCreated());
		member.setModified(concept.getModified());
		member.setDefLongueLg1(concept.getDefinitionLg1());
		member.setDefLongueLg2(concept.getDefinitionLg2());

		member.setDefCourteLg1(concept.getScopeNoteLg1());
		member.setDefCourteLg2(concept.getScopeNoteLg2());

		member.setEditorialNoteLg1(concept.getEditorialNoteLg1());
		member.setEditorialNoteLg2(concept.getEditorialNoteLg2());
		return member;
	}

	@Override
	public Map<String, InputStream> getConceptsExportIS(List<String> ids, List<MembersLg> members) {
		Map<String,InputStream> ret = new HashMap<>();
		ids.parallelStream().forEach(id -> {
			try {
				ConceptForExport concept = conceptsExport.getConceptData(id);
				Map<String, String> xmlContent = convertConceptInXml(concept);
				String fileName = conceptsUtils.getConceptExportFileName(concept);
				ret.put(fileName, conceptsExport.exportAsInputStream(fileName,xmlContent,true,true,true));

				if(members != null){
					members.add(convertConceptIntoMembers(concept));
				}
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
		String fileName = FilesUtils.generateFinalFileNameWithoutExtension(collection.getId() + "-" + collection.getPrefLabelLg1(), maxLength);
		return collectionExport.exportAsResponse(fileName,xmlContent,true,true,true);
	}

}
