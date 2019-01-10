package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.code_list.CodeListQueries;
import fr.insee.rmes.persistance.service.sesame.code_list.CodeListUtils;
import fr.insee.rmes.persistance.service.sesame.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.service.sesame.operations.operations.OperationsUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentationsUtils {
	
	private static final String ID = "id";
	final static Logger logger = LogManager.getLogger(DocumentationsUtils.class);


	public JSONObject getMetadataAttributeById(String id) throws RmesException{
		JSONObject mas = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getAttributeSpecificationQuery(id));
		if (mas.length()==0) {throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Attribute not found", "id doesn't exist"+id);}
		transformRangeType(mas);
		mas.put(ID, id);
		return mas;
	}
	
	public JSONObject getDocumentationByIdSims(String idSims) throws RmesException{
		JSONObject doc = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getDocumentationTitleQuery(idSims));
		if (doc.length()==0) {throw new RmesException(HttpStatus.SC_NOT_FOUND, "Not found", "");}
		doc.put(ID, idSims);
		JSONArray docRubrics = RepositoryGestion.getResponseAsArray(DocumentationsQueries.getDocumentationRubricsQuery(idSims));
		doc.put("rubrics", docRubrics);
		return doc;
	}
	

	public void transformRangeType(JSONObject mas) throws RmesException {
		if (!mas.has("range")) {throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "At least one attribute don't have range", "");}
		String rangeUri = mas.getString("range");
		RangeType type = RangeType.getEnumByRdfType(SesameUtils.toURI(rangeUri));
		mas.put("rangeType", type.getJsonType());
		mas.remove("range");

		switch (type) {
			case CODELIST:
				JSONObject codeList = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeListNotationByUri(rangeUri));
				if (codeList != null && !codeList.isNull("notation")) {
					String codeListNotation = codeList.getString("notation");
					mas.put("codeList", codeListNotation);
				}
				break;
			default:
				break;
		}

	}

	public JSONArray getMetadataAttributes() throws RmesException {
		JSONArray attributesList = RepositoryGestion.getResponseAsArray(DocumentationsQueries.getAttributesQuery());
		if (attributesList.length() != 0) {
			 for (int i = 0; i < attributesList.length(); i++) {
		         JSONObject attribute = attributesList.getJSONObject(i);
		         transformRangeType(attribute);
		     }
		}
		return attributesList;
	}
	
	public Map<String,String> getMetadataAttributesUri() throws RmesException {
		Map<String,String> attributes = new HashMap<>();
		JSONArray attributesList = RepositoryGestion.getResponseAsArray(DocumentationsQueries.getAttributesUriQuery());
		if (attributesList.length() != 0) {
			 for (int i = 0; i < attributesList.length(); i++) {
		         JSONObject attribute = attributesList.getJSONObject(i);
		         if (attribute.has(ID)&& attribute.has("uri")) {
		        	 String id = StringUtils.upperCase(attribute.getString(ID));
		        	 attributes.put(id, attribute.getString("uri"));
		         }
		     }
		}
		return attributes;
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
		if (create) {
			sims.setId(prepareCreation(sims.getIdOperation()));
		}else {
			checkIdSims(id, sims.getId());
			checkIdOperation(sims.getIdOperation(), sims.getId());
		}
		URI operation = getOperation(sims.getIdOperation());

		createRdfMetadataReport(sims, operation);
		logger.info("Create or update sims : " + sims.getId() + " - " + sims.getLabelLg1());
		return sims.getId();
	}

	private URI getOperation(String idOperation) throws RmesException {
		if (idOperation==null) {
			logger.error("Create sims cancelled - no operation");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idOperation can't be null", "idOperation is null")	;	
		}
		URI operation = OperationsUtils.getOperationUriById(idOperation);
		if (operation==null) {
			logger.error("Create sims cancelled - no operation");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Operation doesn't exist", "idOperation doesn't match with an existing operation")	;	
		}
		return operation;
	}
	
	private void checkIdSims(String idRequest, String idSims) throws RmesException {
		if (idRequest==null || idSims == null || !idRequest.equals(idSims)) {
			logger.error("Can't update a documentation if idSims or id don't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idSims can't be null, and must be the same in request", "idSims in param : "+idRequest+" /id in body : "+idSims)	;	
		}
	}

	
	private void checkIdOperation(String idOperation, String idSims) throws RmesException {
		if (idOperation==null || idSims == null) {
			logger.error("Can't update a documentation if idOperation or id don't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idOperation can't be null", "idOperation or id is null")	;	
		}
		JSONObject existingIdOperation =  RepositoryGestion.getResponseAsObject(DocumentationsQueries.getDocumentationOperationQuery(idSims));
		if (existingIdOperation == null || existingIdOperation.get("idOperation")==null) {
			logger.error("Can't find operation linked to the documentation");
			throw new RmesException(HttpStatus.SC_NOT_FOUND, "Operation not found", "Maybe this is a creation")	;	
		}
		if (!idOperation.equals(existingIdOperation.get("idOperation"))) {
			logger.error("idOperation and idSims don't match");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idOperation and idSims don't match", "Documentation linked to operation : " + existingIdOperation)	;	
		}
	}

	private String prepareCreation(String idOperation) throws RmesException {
		if (idOperation==null) {
			logger.error("Can't create a documentation if idOperation doesn't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idOperation can't be null", "idOperation is null")	;	
		}
		JSONObject existingIdSims = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getOperationDocumentationQuery(idOperation));
		if (existingIdSims != null && existingIdSims.has("idSims")) {
			logger.error("Documentation already exists");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Operation already has a documentation", "Maybe this is an update")	;	
		}
		return createID();
	}


	private void createRdfMetadataReport(Documentation sims, URI operation) throws RmesException {
		Model model = new LinkedHashModel();
		URI simsUri = SesameUtils.objectIRI(ObjectType.DOCUMENTATION,sims.getId());
		Resource graph = SesameUtils.simsGraph(sims.getId());
		/*Const*/
		model.add(simsUri, RDF.TYPE, SDMX_MM.METADATA_REPORT, graph);
		model.add(simsUri, SDMX_MM.TARGET, operation, graph);
		
		/*Optional*/
		SesameUtils.addTripleString(simsUri, RDFS.LABEL, sims.getLabelLg1(), Config.LG1, model, graph);
		SesameUtils.addTripleString(simsUri, RDFS.LABEL, sims.getLabelLg2(), Config.LG2, model, graph);
		
		List<DocumentationRubric> rubrics = sims.getRubrics();
		
		addRubricsToModel(model, simsUri, graph, rubrics);
		
		RepositoryGestion.loadSimpleObject(simsUri, model, null);
		
	}

	private void addRubricsToModel(Model model, URI simsUri, Resource graph, List<DocumentationRubric> rubrics) throws RmesException {
		Map<String, String> attributesUriList = getMetadataAttributesUri();

		for (DocumentationRubric rubric : rubrics) {
			if (rubric.getRangeType() == null) throw new RmesException(HttpStatus.SC_BAD_REQUEST, "At least one rubric doesn't have idAttribute", "Rubric :"+rubric.getIdAttribute());
			RangeType type = RangeType.getEnumByJsonType(rubric.getRangeType());
			URI predicateUri;
			try {
				predicateUri = SesameUtils.toURI(attributesUriList.get(rubric.getIdAttribute()));
			}catch (Exception e) {
				throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idAttribute not found", rubric.getIdAttribute());
			}
			switch (type) {
				case DATE:
					SesameUtils.addTripleDate(simsUri,predicateUri, rubric.getValue(), model, graph);
					break;
				case CODELIST :
					String codeUri = CodeListUtils.getCodeUri(rubric.getCodeList(), rubric.getValue());
					if (codeUri != null) { 
						SesameUtils.addTripleUri(simsUri,predicateUri , SesameUtils.toURI(codeUri), model, graph);
					}
					break; 
				case ATTRIBUTE :
					if (StringUtils.isNotEmpty(rubric.getLabelLg1()) || StringUtils.isNotEmpty(rubric.getLabelLg2())) {
						BNode bnode = new BNodeImpl(rubric.getIdAttribute()); 
						SesameUtils.addTripleBNode(bnode,RDF.VALUE, rubric.getLabelLg1(),Config.LG1, model, graph);
						SesameUtils.addTripleBNode(bnode,RDF.VALUE, rubric.getLabelLg2(),Config.LG2, model, graph);
						SesameUtils.addTripleBNode(simsUri,predicateUri, bnode, model, graph);					
					}
					break; 
				case ORGANIZATION :
					break; 
				case STRING :
					break; 
				default:
					break;
			}
		}
	}	
	

	private String createID() throws RmesException {
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
