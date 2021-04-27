package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.bauhaus_services.code_list.LangService;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.organizations.OrganizationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.RangeType;
import fr.insee.rmes.persistance.ontologies.DCMITYPE;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.XMLUtils;

@Component
public class DocumentationsRubricsUtils extends RdfService {

	static final Logger logger = LogManager.getLogger(DocumentationsRubricsUtils.class);

	@Autowired
	private MetadataStructureDefUtils msdUtils;

	@Autowired
	private DocumentsUtils docUtils;

	@Autowired
	private OrganizationUtils organizationUtils;

	@Autowired
	private CodeListService codeListService;

	@Autowired
	private LangService langService;

	@Autowired
	private GeographyService geoService;


	/**
	 * GETTER
	 * @param idSims, jsonObject containing the sims
	 * @return void : the jsonObject is updated
	 * @throws RmesException
	 */
	public void getAllRubricsJson(String idSims, JSONObject jsonSims) throws RmesException {
		JSONArray docRubrics = repoGestion
				.getResponseAsArray(DocumentationsQueries.getDocumentationRubricsQuery(idSims, langService.getLanguage1(), langService.getLanguage2()));
		if (docRubrics.length() != 0) {
			clearRubrics(idSims, docRubrics);
			jsonSims.put("rubrics", docRubrics);
		}
		else jsonSims.put("rubrics", new JSONArray());
	}

	/**
	 * From JSON to JSON
	 * Get documents if exist, format date and format list of values for codelist
	 * @param idSims
	 * @param docRubrics
	 * @return
	 * @throws RmesException
	 */
	private JSONArray clearRubrics(String idSims, JSONArray docRubrics) throws RmesException {
		Map<String, JSONObject> tempMultipleCodeList = new HashMap<>();

		for (int i = docRubrics.length() - 1; i >= 0; i--) {
			JSONObject rubric = docRubrics.getJSONObject(i);

			// Get documents
			if (rubric.has(Constants.HAS_DOC_LG1)) {
				clearDocuments(idSims, rubric, Constants.HAS_DOC_LG1);
			}
			if (rubric.has(Constants.HAS_DOC_LG2)) {
				clearDocuments(idSims, rubric, Constants.HAS_DOC_LG2);
			}
			// Format date
			else if (rubric.get(Constants.RANGE_TYPE).equals(RangeType.DATE)) {
				rubric.put(Constants.VALUE, DateUtils.getDate(rubric.getString(Constants.VALUE)));
			}

			// Format codelist with multiple value
			else if (rubric.has("maxOccurs")) {
				putMultipleValueInList(docRubrics, tempMultipleCodeList, i, rubric);
			}

			//Format Geo features
			else if (rubric.get(Constants.RANGE_TYPE).equals(RangeType.GEOGRAPHY.name())) {
				clearGeographyRubric(rubric);
			}
		}
		if (tempMultipleCodeList.size() != 0) {
			tempMultipleCodeList.forEach((k, v) -> docRubrics.put(v));
		}
		return docRubrics;
	}

	private void clearGeographyRubric(JSONObject rubric) throws RmesException {
		String value = rubric.getString(Constants.URI);
		if (StringUtils.isNotEmpty(value)) {
			IRI geoUri = RdfUtils.createIRI(value);
			JSONObject feature = geoService.getGeoFeature(geoUri);
			feature.keys().forEachRemaining(key -> rubric.put(key,feature.get(key)));
		}
	}

	private void putMultipleValueInList(JSONArray docRubrics, Map<String, JSONObject> tempMultipleCodeList, int i,
			JSONObject rubric) {
		String newValue = rubric.getString(Constants.VALUE);
		String attribute = rubric.getString(Constants.ID_ATTRIBUTE);

		if (tempMultipleCodeList.containsKey(attribute)) {
			JSONObject tempObject = tempMultipleCodeList.get(attribute);
			tempObject.accumulate(Constants.VALUE, newValue);
			tempMultipleCodeList.replace(attribute, tempObject);
		} else {
			tempMultipleCodeList.put(attribute, rubric);
		}
		docRubrics.remove(i);
	}

	private void clearDocuments(String idSims, JSONObject rubric, String hasDocLg) throws RmesException {
		if (rubric.getBoolean(hasDocLg)) {
			JSONArray listDoc = docUtils.getListDocumentLink(idSims, rubric.getString(Constants.ID_ATTRIBUTE), hasDocLg.equals(Constants.HAS_DOC_LG1)? Config.LG1 : Config.LG2);
			rubric.put(hasDocLg.equals(Constants.HAS_DOC_LG1)?Constants.DOCUMENTS_LG1 : Constants.DOCUMENTS_LG2, listDoc);
		}
		rubric.remove(hasDocLg);
	}


	/**
	 * From Object to RDF
	 * Add all rubrics to the specified metadata report
	 * @param model
	 * @param simsId
	 * @param graph
	 * @param rubrics
	 * @throws RmesException
	 */
	public void addRubricsToModel(Model model, String simsId, Resource graph, List<DocumentationRubric> rubrics)
			throws RmesException {
		Map<String, String> attributesUriList = msdUtils.getMetadataAttributesUri();
		IRI simsUri = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, simsId);

		for (DocumentationRubric rubric : rubrics) {
			RangeType type = getRangeType(rubric);
			IRI predicateUri;
			IRI attributeUri;
			try {
				String predicate = attributesUriList.get(rubric.getIdAttribute());
				predicateUri = RdfUtils.toURI(predicate);
				attributeUri = getAttributeUri(simsId, predicate);
			} catch (Exception e) {
				throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idAttribute not found", rubric.getIdAttribute());
			}
			RdfUtils.addTripleUri(attributeUri, SDMX_MM.METADATA_REPORT_PREDICATE, simsUri, model, graph);
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
			IRI predicateUri, IRI attributeUri) throws RmesException {
		switch (type) {
		case DATE:
			RdfUtils.addTripleDate(attributeUri, predicateUri, rubric.getSimpleValue(), model, graph);
			break;
		case CODELIST:
			if (rubric.getValue() != null) {
				for (String code : rubric.getValue()) {
					getCodeUriAndAddToModel(model, graph, rubric, predicateUri, attributeUri, code);
				}
			}
			break;
		case RICHTEXT:
			if (!rubric.isEmpty()) {
				addRichTextToModel(model, graph, rubric, predicateUri, attributeUri);
			}
			break;
		case ORGANIZATION:
			String orgaUri = organizationUtils.getUri(rubric.getSimpleValue());
			if (orgaUri != null) {
				RdfUtils.addTripleUri(attributeUri, predicateUri, RdfUtils.toURI(orgaUri), model, graph);
			}
			break;
		case STRING:
			if (!rubric.isEmpty()) {
				addSimpleTextToModel(model, graph, rubric, predicateUri, attributeUri);
			}
			break;
		case GEOGRAPHY:
			String featureUri = rubric.getUri();
			if (StringUtils.isNotEmpty(featureUri)) {
				RdfUtils.addTripleUri(attributeUri, predicateUri, RdfUtils.toURI(featureUri), model, graph);
			}
			break;
		default:
			break;
		}
	}

	private void getCodeUriAndAddToModel(Model model, Resource graph, DocumentationRubric rubric, IRI predicateUri,
			IRI attributeUri, String code) throws RmesException {
		String codeUri = codeListService.getCodeUri(rubric.getCodeList(), code);
		if (codeUri != null) {
			RdfUtils.addTripleUri(attributeUri, predicateUri, RdfUtils.toURI(codeUri), model, graph);
		}
	}

	private void addSimpleTextToModel(Model model, Resource graph, DocumentationRubric rubric, IRI predicateUri,
			IRI attributeUri) {
		RdfUtils.addTripleUri(attributeUri, RDF.TYPE, SDMX_MM.REPORTED_ATTRIBUTE, model, graph);
		if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
			RdfUtils.addTripleString(attributeUri, predicateUri, rubric.getLabelLg1(), Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
			RdfUtils.addTripleString(attributeUri, predicateUri, rubric.getLabelLg2(), Config.LG2, model, graph);
		}
	}

	private void addRichTextToModel(Model model, Resource graph, DocumentationRubric rubric, IRI predicateUri,
			IRI attributeUri) throws RmesException {		
		if (rubric.hasRichTextLg1()) {
			IRI textUriLg1 = RdfUtils.toURI(attributeUri.stringValue().concat("/").concat(Constants.TEXT_LG1));
			RdfUtils.addTripleUri(attributeUri, predicateUri, textUriLg1, model, graph);
			RdfUtils.addTripleUri(textUriLg1, RDF.TYPE, DCMITYPE.TEXT, model, graph);
			RdfUtils.addTripleUri(textUriLg1, DCTERMS.LANGUAGE, langService.getLanguage1(), model, graph);

			if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
				RdfUtils.addTripleStringMdToXhtml(textUriLg1, RDF.VALUE, rubric.getLabelLg1(), Config.LG1, model, graph);
			}
			docUtils.addDocumentsToRubric(model, graph, rubric.getDocumentsLg1(), textUriLg1);
		}
		if (rubric.hasRichTextLg2()) {
			IRI textUriLg2 = RdfUtils.toURI(attributeUri.stringValue().concat("/").concat(Constants.TEXT_LG2));
			RdfUtils.addTripleUri(attributeUri, predicateUri, textUriLg2, model, graph);
			RdfUtils.addTripleUri(textUriLg2, RDF.TYPE, DCMITYPE.TEXT, model, graph);
			RdfUtils.addTripleUri(textUriLg2, DCTERMS.LANGUAGE, langService.getLanguage2(), model, graph);

			if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
				RdfUtils.addTripleStringMdToXhtml(textUriLg2, RDF.VALUE, rubric.getLabelLg2(), Config.LG2, model, graph);
			}
			docUtils.addDocumentsToRubric(model, graph, rubric.getDocumentsLg2(), textUriLg2);
		}
	}




	private RangeType getRangeType(DocumentationRubric rubric) throws RmesException {
		if (rubric.getRangeType() == null) {
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "At least one rubric doesn't have rangeType",
					"Rubric :" + rubric.getIdAttribute());
		}
		return RangeType.getEnumByJsonType(rubric.getRangeType());
	}

	/**
	 * Get attribute uri for a metadata report and the associated attribute definition
	 * @param simsId
	 * @param predicate
	 * @return
	 */
	private IRI getAttributeUri(String simsId, String predicate) {
		String newUri = predicate.replace("/simsv2fr/attribut/", "/attribut/" + simsId + "/");
		return RdfUtils.toURI(newUri);
	}

	/**
	 * From JSONObject to JAVA Object DocumentationRubric
	 * @param JsonRubric
	 * @return documentationRubric
	 * @throws RmesException
	 */

	public DocumentationRubric buildRubricFromJson(JSONObject jsonRubric, Boolean forXml) {
		DocumentationRubric documentationRubric = new DocumentationRubric();
		if (jsonRubric.has(Constants.ID_ATTRIBUTE)) {
			documentationRubric.setIdAttribute(jsonRubric.getString(Constants.ID_ATTRIBUTE));
		}
		if (jsonRubric.has(Constants.VALUE)) {
			try{
				documentationRubric.setValue(fr.insee.rmes.utils.StringUtils.stringToList(jsonRubric.getString(Constants.VALUE)));
			}
			catch(JSONException e) {
				/* value is not a string but an array */
				JSONArray jsonArrayValue =jsonRubric.getJSONArray(Constants.VALUE);
				documentationRubric.setValue(JSONUtils.jsonArrayToList(jsonArrayValue));
			}
		}
		if (jsonRubric.has(Constants.LABEL_LG1)) {
			if(forXml) {
				documentationRubric.setLabelLg1(XMLUtils.solveSpecialXmlcharacters(jsonRubric.getString(Constants.LABEL_LG1)));
			}
			else
			{
				documentationRubric.setLabelLg1(jsonRubric.getString(Constants.LABEL_LG1));
			}
		}
		if (jsonRubric.has(Constants.LABEL_LG2)) {
			if(forXml) {
				documentationRubric.setLabelLg2(XMLUtils.solveSpecialXmlcharacters(jsonRubric.getString(Constants.LABEL_LG2)));
			}
			else
			{
				documentationRubric.setLabelLg2(jsonRubric.getString(Constants.LABEL_LG2));
			}
		}
		if (jsonRubric.has("codeList")) {
			documentationRubric.setCodeList(jsonRubric.getString("codeList"));
		}
		if (jsonRubric.has(Constants.RANGE_TYPE)) {
			documentationRubric.setRangeType(jsonRubric.getString(Constants.RANGE_TYPE));
		}

		if (jsonRubric.has(Constants.DOCUMENTS_LG1)) {	
			addJsonDocumentToObjectRubric(jsonRubric, documentationRubric, Constants.DOCUMENTS_LG1);
		}
		if (jsonRubric.has(Constants.DOCUMENTS_LG2)) {	
			addJsonDocumentToObjectRubric(jsonRubric, documentationRubric, Constants.DOCUMENTS_LG2);
		}
		return documentationRubric;
	}
	
	private void addJsonDocumentToObjectRubric(JSONObject rubric, DocumentationRubric documentationRubric, String documentsWithRubricLang) {
		List<Document> docs = new ArrayList<>();

		JSONArray documents = rubric.getJSONArray(documentsWithRubricLang);
		Document currentDoc;

		for (int i = 0; i < documents.length(); i++) {
			JSONObject doc = documents.getJSONObject(i);
			currentDoc = docUtils.buildDocumentFromJson(doc);
			docs.add(currentDoc);
		}	
		if (documentsWithRubricLang.equals(Constants.DOCUMENTS_LG1)) {
			documentationRubric.setDocumentsLg1(docs);
		}else {
			documentationRubric.setDocumentsLg2(docs);
		}
	}

}
