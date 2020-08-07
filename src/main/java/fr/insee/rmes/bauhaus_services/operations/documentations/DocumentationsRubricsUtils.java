package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListUtils;
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

@Component
public class DocumentationsRubricsUtils extends RdfService {

	private static final String VALUE = "value";

	private static final String HAS_DOC = "hasDoc";

	static final Logger logger = LogManager.getLogger(DocumentationsRubricsUtils.class);

	@Autowired
	private MetadataStructureDefUtils msdUtils;

	@Autowired
	private DocumentsUtils docUtils;

	@Autowired
	private OrganizationUtils organizationUtils;

	@Autowired
	private CodeListUtils codeListUtils;
	
	@Autowired
	private GeographyService geoService;


	

	/**
	 * GETTER
	 * @param idSims, jsonObject containing the sims
	 * @return void : the jsonObject is updated
	 * @throws RmesException
	 */
	public void getAllRubricsJson(String idSims, JSONObject jsonObject) throws RmesException {
		JSONArray docRubrics = repoGestion
				.getResponseAsArray(DocumentationsQueries.getDocumentationRubricsQuery(idSims));
		if (docRubrics.length() != 0) {
			clearRubrics(idSims, docRubrics);
			jsonObject.put("rubrics", docRubrics);
		}
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
			if (rubric.has(HAS_DOC)) {
				if (rubric.getBoolean(HAS_DOC)) {
					JSONArray listDoc = docUtils.getListDocumentLink(idSims, rubric.getString("idAttribute"));
					rubric.put("documents", listDoc);
				}
				rubric.remove(HAS_DOC);
			}

			// Format date
			else if (rubric.get("rangeType").equals(RangeType.DATE)) {
				rubric.put(VALUE, DateUtils.getDate(rubric.getString(VALUE)));
			}

			// Format codelist with multiple value
			else if (rubric.has("maxOccurs")) {
				String newValue = rubric.getString(VALUE);
				String attribute = rubric.getString("idAttribute");

				if (tempMultipleCodeList.containsKey(attribute)) {
					JSONObject tempObject = tempMultipleCodeList.get(attribute);
					tempObject.accumulate(VALUE, newValue);
					tempMultipleCodeList.replace(attribute, tempObject);
				} else {
					tempMultipleCodeList.put(attribute, rubric);
				}
				docRubrics.remove(i);
			}
			else if (rubric.get("rangeType").equals(RangeType.GEOGRAPHY)) {
				String geoUri = rubric.getString(VALUE);
				JSONObject feature = geoService.getGeoFeature(geoUri);
				feature.keys().forEachRemaining(key -> rubric.put(key,feature.get(key)));
			}
		}
		if (tempMultipleCodeList.size() != 0) {
			tempMultipleCodeList.forEach((k, v) -> docRubrics.put(v));
		}
		return docRubrics;
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
				String featureUri = rubric.getSimpleValue();
				if (featureUri != null) {
					RdfUtils.addTripleUri(attributeUri, predicateUri, RdfUtils.toURI(featureUri), model, graph);
				}
				break;
			default:
				break;
		}
	}

	private void getCodeUriAndAddToModel(Model model, Resource graph, DocumentationRubric rubric, IRI predicateUri,
			IRI attributeUri, String code) throws RmesException {
		String codeUri = codeListUtils.getCodeUri(rubric.getCodeList(), code);
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
		IRI textUri = RdfUtils.toURI(attributeUri.stringValue().concat("/texte"));
		RdfUtils.addTripleUri(attributeUri, predicateUri, textUri, model, graph);
		RdfUtils.addTripleUri(textUri, RDF.TYPE, DCMITYPE.TEXT, model, graph);
		if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
			RdfUtils.addTripleStringMdToXhtml(textUri, RDF.VALUE, rubric.getLabelLg1(), Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
			RdfUtils.addTripleStringMdToXhtml(textUri, RDF.VALUE, rubric.getLabelLg2(), Config.LG2, model, graph);
		}
		docUtils.addDocumentsToRubric(model, graph, rubric, textUri);
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
	 * Java Object	Builder
	 * @param JsonRubric
	 * @return documentationRubric
	 * @throws RmesException
	 */

	private DocumentationRubric buildRubricFromJson(JSONObject rubric) throws RmesException {
		DocumentationRubric documentationRubric = new DocumentationRubric();
		if (rubric.has("idAttribute"))		documentationRubric.setIdAttribute(rubric.getString("idAttribute"));
		if (rubric.has("value")) {
			try{
				documentationRubric.setValue(rubric.getString("value"));
			}
			catch(JSONException e) {
				/* value is not a string but an array */
				JSONArray JsonArrayValue =rubric.getJSONArray("value");
				documentationRubric.setValue(JSONUtils.jsonArrayOfStringToString(JsonArrayValue));
			}
		}
		if (rubric.has("labelLg1"))		documentationRubric.setLabelLg1(rubric.getString("labelLg1"));
		if (rubric.has("labelLg2"))		documentationRubric.setLabelLg2(rubric.getString("labelLg2"));
		if (rubric.has("codeList"))		documentationRubric.setCodeList(rubric.getString("codeList"));
		if (rubric.has("rangeType"))		documentationRubric.setRangeType(rubric.getString("rangeType"));


		if (rubric.has("documents")) {	
			List<Document> docs = new ArrayList<Document>();

			JSONArray documents = rubric.getJSONArray("documents");
			Document currentDoc = new Document();

			for (int i = 0; i < documents.length(); i++) {
				JSONObject doc = documents.getJSONObject(i);
				currentDoc = buildDocumentFromJson(doc);
				docs.add(currentDoc);
			}	
			documentationRubric.setDocuments(docs);
		}
		return documentationRubric;
	}
	
}
