package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.service.sesame.code_list.CodeListUtils;
import fr.insee.rmes.persistance.service.sesame.ontologies.DCMITYPE;
import fr.insee.rmes.persistance.service.sesame.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.persistance.service.sesame.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.persistance.service.sesame.organizations.OrganizationUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentationsUtils {
	
	private static final String ID = "id";
	final static Logger logger = LogManager.getLogger(DocumentationsUtils.class);

	@Autowired
	MetadataStructureDefUtils msdUtils;
	
	@Autowired
	DocumentsUtils docUtils;

	/**
	 * GETTER
	 * @param idSims
	 * @return
	 * @throws RmesException
	 */
	public JSONObject getDocumentationByIdSims(String idSims) throws RmesException{
		//Get general informations
		JSONObject doc = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getDocumentationTitleQuery(idSims));
		if (doc.length()==0) {throw new RmesNotFoundException("Not found", "");}
		doc.put(ID, idSims);
		
		//Get all rubrics
		JSONArray docRubrics = RepositoryGestion.getResponseAsArray(DocumentationsQueries.getDocumentationRubricsQuery(idSims));
		if (docRubrics.length() != 0) {
			 for (int i = 0; i < docRubrics.length(); i++) {
		         JSONObject rubric = docRubrics.getJSONObject(i);
		         if (rubric.has("hasDoc") && rubric.getBoolean("hasDoc")) {
		        	 JSONArray listDoc = docUtils.getListDocumentLink(idSims, rubric.getString("idAttribute"));
		        	 rubric.put("documents", listDoc);
		         }
		         rubric.remove("hasDoc");
		     }
		}
		doc.put("rubrics", docRubrics);
		return doc;
	}
	
	
	/**
	 * CREATE or UPDATE
	 * @param body
	 * @return
	 * @throws RmesException 
	 */
	public String setMetadataReport(String id, String body, boolean create) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Documentation sims = new Documentation();
		try {
			sims = mapper.readValue(body, Documentation.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_METHOD_FAILURE, e.getMessage(), "IOException")	;	
		}
		//Check idOperation/idSerie/IdIndicator and Init or check id sims
		String idTarget = sims.getIdTarget();
		if (create) {
			sims.setId(prepareCreation(idTarget));
		}else {
			checkIdsBeforeUpdate(id, sims.getId(), idTarget);
		}
		URI targetUri = getTarget(sims);

		//Update rubrics
		saveRdfMetadataReport(sims, targetUri);
		logger.info("Create or update sims : " + sims.getId() + " - " + sims.getLabelLg1());
		return sims.getId();
	}

	
	/**
	 * get Operation URI to create or update SIMS
	 * @param idTarget
	 * @return
	 * @throws RmesException
	 */
	private URI getTarget(Documentation sims) throws RmesException {
		URI target = null;
		
		if (StringUtils.isNotEmpty(sims.getIdOperation()) && FamOpeSerUtils.checkIfObjectExists(ObjectType.OPERATION,sims.getIdTarget())){
			target= SesameUtils.objectIRI(ObjectType.OPERATION, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdSeries()) && FamOpeSerUtils.checkIfObjectExists(ObjectType.SERIES,sims.getIdTarget())){
			target= SesameUtils.objectIRI(ObjectType.SERIES, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdIndicator()) && IndicatorsUtils.checkIfIndicatorExists(sims.getIdTarget())) {
			target= SesameUtils.objectIRI(ObjectType.INDICATOR, sims.getIdTarget());
		}
		if (target==null) {
			logger.error("Create or Update sims cancelled - no target");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Operation/Series/Indicator doesn't exist", "id Operation/Series/Indicator doesn't match with an existing Operation/Series/Indicator")	;	
		}
		return target;
	}
	
	/**
	 * Check the existing id is the same that the id to set
	 * Update only
	 * @param idRequest
	 * @param idSims
	 * @param idTarget
	 * @throws RmesException
	 */
	private void checkIdsBeforeUpdate(String idRequest, String idSims, String idTarget) throws RmesException {
		//Check idSims
		if (idRequest==null || idSims == null || !idRequest.equals(idSims)) {
			logger.error("Can't update a documentation if idSims or id don't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idSims can't be null, and must be the same in request", "idSims in param : "+idRequest+" /id in body : "+idSims)	;	
		}
		//Check id Operation/Serie/Indicator
		if (idTarget==null) {
			logger.error("Can't update a documentation if id Operation/Serie/Indicator doesn't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "id Operation/Serie/Indicator can't be null", "id Operation/Serie/Indicator or id is null")	;	
		}
		JSONObject existingIdTarget =  RepositoryGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));
		String idDatabase = null;
		if (existingIdTarget != null ) {
			idDatabase = (String) existingIdTarget.get("idOperation");
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get("idSeries");
			}
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get("idIndicator");
			}
		}
		if (existingIdTarget == null || idDatabase == null) {
			logger.error("Can't find Operation/Serie/Indicator linked to the documentation");
			throw new RmesNotFoundException("Operation/Serie/Indicator not found", "Maybe this is a creation")	;	
		}
		if (!idTarget.equals(idDatabase)) {
			logger.error("id Operation/Serie/Indicator and idSims don't match");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "id Operation/Serie/Indicator and idSims don't match", "Documentation linked to Operation/Serie/Indicator : " + existingIdTarget)	;	
		}
	}

	/**
	 * check idTarget is not null and has no sims yet
	 * create only
	 * @param idTarget
	 * @return
	 * @throws RmesException
	 */
	private String prepareCreation(String idTarget) throws RmesException {
		if (idTarget==null) {
			logger.error("Can't create a documentation if operation/serie/indicator doesn't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "id operation/serie/indicator can't be null", "id is null")	;	
		}
		JSONObject existingIdSims = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getSimsByTarget(idTarget));
		if (existingIdSims != null && existingIdSims.has("idSims")) {
			logger.error("Documentation already exists");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Operation/Series/Indicator already has a documentation", "Maybe this is an update")	;	
		}
		return createSimsID();
	}

	/**
	 * Load in database the metadataReport to create or update
	 * @param sims
	 * @param target
	 * @throws RmesException
	 */
	private void saveRdfMetadataReport(Documentation sims, URI target) throws RmesException {
		Model model = new LinkedHashModel();
		URI simsUri = SesameUtils.objectIRI(ObjectType.DOCUMENTATION,sims.getId());
		Resource graph = SesameUtils.simsGraph(sims.getId());
		/*Const*/
		model.add(simsUri, RDF.TYPE, SDMX_MM.METADATA_REPORT, graph);
		model.add(simsUri, SDMX_MM.TARGET, target, graph);
		
		/*Optional*/
		SesameUtils.addTripleString(simsUri, RDFS.LABEL, sims.getLabelLg1(), Config.LG1, model, graph);
		SesameUtils.addTripleString(simsUri, RDFS.LABEL, sims.getLabelLg2(), Config.LG2, model, graph);
		
		addRubricsToModel(model, sims.getId(), graph, sims.getRubrics());
		
		RepositoryGestion.replaceGraph(graph, model, null);
	}

	/**
	 * Add all rubrics to the specified metadata report
	 * @param model
	 * @param simsId
	 * @param graph
	 * @param rubrics
	 * @throws RmesException
	 */
	private void addRubricsToModel(Model model, String simsId, Resource graph, List<DocumentationRubric> rubrics) throws RmesException {
		Map<String, String> attributesUriList = msdUtils.getMetadataAttributesUri();
		URI simsUri = SesameUtils.objectIRI(ObjectType.DOCUMENTATION,simsId);
		
		for (DocumentationRubric rubric : rubrics) {
			RangeType type = getRangeType(rubric);
			URI predicateUri;
			URI attributeUri;
			try {
				String predicate = attributesUriList.get(rubric.getIdAttribute());
				predicateUri = SesameUtils.toURI(predicate);
				attributeUri = getAttributeUri(simsId,predicate);
			}catch (Exception e) {
				throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idAttribute not found", rubric.getIdAttribute());
			}
			SesameUtils.addTripleUri(attributeUri,SDMX_MM.METADATA_REPORT_PREDICATE,simsUri, model, graph);
			addRubricByRangeType(model, graph, rubric, type, predicateUri, attributeUri);
		}
	}

	/**
	 * Add one rubric to the model
	 * @param model
	 * @param graph
	 * @param rubric
	 * @param type
	 * @param predicateUri
	 * @param attributeUri
	 * @throws RmesException
	 */
	private void addRubricByRangeType(Model model, Resource graph, DocumentationRubric rubric, RangeType type,
			URI predicateUri, URI attributeUri) throws RmesException {
		switch (type) {
			case DATE:
				SesameUtils.addTripleDate(attributeUri,predicateUri, rubric.getValue(), model, graph);
				break;
			case CODELIST :
				String codeUri = CodeListUtils.getCodeUri(rubric.getCodeList(), rubric.getValue());
				if (codeUri != null) { 
					SesameUtils.addTripleUri(attributeUri,predicateUri , SesameUtils.toURI(codeUri), model, graph);
				}
				break; 
			case RICHTEXT :
				if (rubric.isEmpty()) break;
				URI textUri =SesameUtils.toURI( attributeUri.stringValue().concat("/texte"));
				SesameUtils.addTripleUri(attributeUri,predicateUri , textUri, model, graph);
				SesameUtils.addTripleUri(textUri,RDF.TYPE , DCMITYPE.TEXT, model, graph);
				if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
					SesameUtils.addTripleStringMdToXhtml(textUri, RDF.VALUE, rubric.getLabelLg1(),Config.LG1, model, graph);
				}
				if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
					SesameUtils.addTripleStringMdToXhtml(textUri,RDF.VALUE, rubric.getLabelLg2(),Config.LG2, model, graph);
				}
				docUtils.addDocumentsToRubric(model, graph, rubric, textUri);
				break; 
			case ORGANIZATION :
				String orgaUri = OrganizationUtils.getUri(rubric.getValue());
				if (orgaUri != null) { 
					SesameUtils.addTripleUri(attributeUri,predicateUri , SesameUtils.toURI(orgaUri), model, graph);
				}
				break; 
			case STRING :
				if (rubric.isEmpty()) break;
				SesameUtils.addTripleUri(attributeUri,RDF.TYPE,SDMX_MM.REPORTED_ATTRIBUTE, model, graph);
				if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
					SesameUtils.addTripleString(attributeUri,predicateUri , rubric.getLabelLg1(),Config.LG1, model, graph);
				}	
				if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
					SesameUtils.addTripleString(attributeUri,predicateUri , rubric.getLabelLg2(),Config.LG2, model, graph);
				}
				break; 
			default:
				break;
		}
	}


	private RangeType getRangeType(DocumentationRubric rubric) throws RmesException {
		if (rubric.getRangeType() == null) throw new RmesException(HttpStatus.SC_BAD_REQUEST, "At least one rubric doesn't have rangeType", "Rubric :"+rubric.getIdAttribute());
		RangeType type = RangeType.getEnumByJsonType(rubric.getRangeType());
		return type;
	}	
	
	/**
	 * Get attribute uri for a metadata report and the associated attribute definition
	 * @param simsId
	 * @param predicate
	 * @return
	 */
	private URI getAttributeUri(String simsId, String predicate) {
		String newUri = predicate.replace("/simsv2fr/attribut/", "/attribut/"+simsId+"/");
		return SesameUtils.toURI(newUri);
	}


	/**
	 * Generate a new ID
	 * Prefer to call prepareCreation instead
	 * @return
	 * @throws RmesException
	 */
	private String createSimsID() throws RmesException {
		logger.info("Generate documentation id");
		JSONObject json = RepositoryGestion.getResponseAsObject(DocumentationsQueries.lastID());
		logger.debug("JSON for documentation id : " + json);
		if (json.length()==0) {return "1000";}
		String id = json.getString("idSims");
		if (id.equals("undefined")) {return "1000";}
		int newId = Integer.parseInt(id)+1;
		return String.valueOf(newId);
	}

}
