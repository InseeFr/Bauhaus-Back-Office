package fr.insee.rmes.bauhaus_services.code_list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;

@Service
public class CodeListServiceImpl extends RdfService implements CodeListService  {

	private static final String LAST_CLASS_URI_SEGMENT = "lastClassUriSegment";

	private static final String CODE = "code";

	private static final String CODES = "codes";

	private static final String LAST_LIST_URI_SEGMENT = "lastListUriSegment";

	private static final String LAST_CODE_URI_SEGMENT = "lastCodeUriSegment";

	static final Logger logger = LoggerFactory.getLogger(CodeListServiceImpl.class);
	public static final String VALIDATION_STATE = "validationState";
	public static final String CONCEPT = "concept/";


	@Autowired
	FamOpeSerIndUtils famOpeSerIndUtils;

	@Autowired
	CodeListPublication codeListPublication;

	@Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseInternalURI}")
	String baseInternalURI;

	@Override
	public String getCodesJson(String notation, int page, Integer perPage) throws RmesException {
		JSONObject result = new JSONObject();

		JSONObject counter = repoGestion.getResponseAsObject(CodeListQueries.countCodesForCodeList(notation, null));
		JSONArray items = repoGestion.getResponseAsArray(CodeListQueries.getCodeListItemsByNotation(notation, page, perPage));

		result.put("total", counter.get("count"));
		result.put("page", page);
		result.put("items", items);

		return result.toString();
	}

	@Override
	public String getCodeListJson(String notation) throws RmesException{
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getCodeListLabelByNotation(notation));
		codeList.put(Constants.NOTATION,notation);
		return QueryUtils.correctEmptyGroupConcat(codeList.toString());
	}

	public CodeList buildCodeListFromJson(String codeListJson) {
		ObjectMapper mapper = new ObjectMapper();
		CodeList codeList = new CodeList();
		try {
			codeList = mapper.readValue(codeListJson, CodeList.class);
		} catch (JsonProcessingException e) {
			logger.error("Json cannot be parsed: ".concat(e.getMessage()));
		}
		return codeList;
	}

	@Override
	public CodeList getCodeList(String notation) throws RmesException {
		return buildCodeListFromJson(getCodeListJson(notation));
	}

	@Override
	public String getDetailedCodesList(String notation, boolean partial) throws RmesException {
		return getDetailedCodesListJson(notation, partial).toString();
	}

	public JSONObject getDetailedCodesListJson(String notation, boolean partial) throws RmesException {
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getDetailedCodeListByNotation(notation, baseInternalURI));
		getMultipleTripletsForObject(codeList, "contributor", CodeListQueries.getCodesListContributors(codeList.getString("iri")), "contributor");

		if(!partial){
			return codeList;
		}
		else {
			JSONArray codes = repoGestion.getResponseAsArray(CodeListQueries.getDetailedCodes(notation, true, null, 0, 0, null));
			formatCodesForPartialList(codeList, codes);
			return codeList;
		}

	}

	/**
	 * In order to avoid multiple loops, we group by the data by the code only once.
	 */
	private JSONObject groupByBroaderNarrowerCloseMatchByCode(String notation) throws RmesException {
		JSONObject broaderNarrowerCloseMatchByCode = new JSONObject();

		JSONArray broaderNarrowerCloseMatch = repoGestion.getResponseAsArray(CodeListQueries.getBroaderNarrowerCloseMatch(notation));
		for(var i = 0; i < broaderNarrowerCloseMatch.length(); i++){
			JSONObject broaderNarrowerCloseMatchCode = broaderNarrowerCloseMatch.getJSONObject(i);

			String mainCode = broaderNarrowerCloseMatchCode.getString("code");
			String linkCode = broaderNarrowerCloseMatchCode.getString("linkCode");
			String linkType = broaderNarrowerCloseMatchCode.getString("linkType");
			if(!broaderNarrowerCloseMatchByCode.has(mainCode)){
				broaderNarrowerCloseMatchByCode.put(mainCode,
						new JSONObject()
								.put("broader", new JSONArray())
								.put("narrower", new JSONArray())
								.put("closeMatch", new JSONArray()));
			}

			JSONObject codeToUpdate = broaderNarrowerCloseMatchByCode.getJSONObject(mainCode);
			codeToUpdate.getJSONArray(linkType).put(linkCode);
		}
		return broaderNarrowerCloseMatchByCode;
	}

	private void addLinkCodeToItem(JSONObject item, String key, JSONObject broaderNarrowerCloseMatchForCode){
		if(broaderNarrowerCloseMatchForCode.getJSONArray(key).length() > 0){
			item.put(key, broaderNarrowerCloseMatchForCode.getJSONArray(key));
		}
	}

	private void addBroaderNarrowerCloseMatchToItem(JSONObject item, JSONObject broaderNarrowerCloseMatchByCode){
		if(broaderNarrowerCloseMatchByCode.has(item.getString("code"))){
			JSONObject broaderNarrowerCloseMatchForCode = broaderNarrowerCloseMatchByCode.getJSONObject(item.getString("code"));
			addLinkCodeToItem(item, "broader", broaderNarrowerCloseMatchForCode);
			addLinkCodeToItem(item, "narrower", broaderNarrowerCloseMatchForCode);
			addLinkCodeToItem(item, "closeMatch", broaderNarrowerCloseMatchForCode);
		}
	}
	@Override
	public String getCodesForCodeList(String notation, List<String> search, int page, Integer perPage, String sort) throws RmesException {
		JSONObject result = new JSONObject();

		JSONObject counter = repoGestion.getResponseAsObject(CodeListQueries.countCodesForCodeList(notation, search));
		JSONArray items = repoGestion.getResponseAsArray(CodeListQueries.getDetailedCodes(notation, false, search, page, perPage, sort));
		JSONObject broaderNarrowerCloseMatchByCode = groupByBroaderNarrowerCloseMatchByCode(notation);


		for(var i = 0; i < items.length(); i++){
			JSONObject item = items.getJSONObject(i);
			addBroaderNarrowerCloseMatchToItem(item, broaderNarrowerCloseMatchByCode);
		}

		result.put("total", counter.get("count"));
		result.put("page", page);
		result.put("items", items);

		return result.toString();
	}


	/**
	 * Format the codes list for a partial code list.
	 * We just need to remove the parents property.
	 */
	private void formatCodesForPartialList(JSONObject codeList, JSONArray codes) {
		JSONObject formattedCodes = new JSONObject();
		codes.forEach(c -> {
			JSONObject tempCode = (JSONObject) c;
			String code = tempCode.getString(CODE);
			if (tempCode.has(Constants.PARENTS)) {
				tempCode.remove(Constants.PARENTS);
			}
			formattedCodes.put(code, tempCode);
		});
		codeList.put(CODES, formattedCodes);
	}

	@Override
	public String getDetailedCodesListForSearch(boolean partial) throws RmesException {
		JSONArray lists =  repoGestion.getResponseAsArray(CodeListQueries.getCodesListsForSearch(partial));
		JSONArray codes =  repoGestion.getResponseAsArray(CodeListQueries.getCodesForSearch(partial));

		for (int i = 0 ; i < lists.length(); i++) {
			JSONObject list = lists.getJSONObject(i);
			list.put(CODES, this.getCodesForList(codes, list));
		}

		return lists.toString();
	}


	public void validateCodeList(JSONObject codeList, boolean partial) throws RmesException {
		if (!codeList.has(Constants.ID)) {
			throw new RmesBadRequestException("The id of the list should be defined");
		}
		if (!codeList.has(Constants.LABEL_LG1)) {
			throw new RmesBadRequestException("The labelLg1 of the list should be defined");
		}
		if (!codeList.has(Constants.LABEL_LG2)) {
			throw new RmesBadRequestException("The labelLg2 of the list should be defined");
		}
		if (!partial && !codeList.has(LAST_CLASS_URI_SEGMENT)) {
			throw new RmesBadRequestException("The lastClassUriSegment of the list should be defined");
		}
		if (!partial && !codeList.has(LAST_LIST_URI_SEGMENT)) {
			throw new RmesBadRequestException("The lastListUriSegment of the list should be defined");
		}
		if(partial && (!codeList.has(CODES) || codeList.getJSONObject(CODES).keySet().isEmpty())){
			throw new RmesBadRequestException(ErrorCodes.CODE_LIST_AT_LEAST_ONE_CODE, "A code list should contain at least one code");
		}
	}

	private IRI generateIri(JSONObject codesList, boolean partial) {
		if(partial){
			return RdfUtils.codeListIRI(codesList.getString(Constants.ID));
		} else {
			return RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT));
		}
	}

	private boolean checkCodeListUnicity(boolean partial, JSONObject codeList, String iri) throws RmesException {
		String id = codeList.getString(Constants.ID);
		if(!partial) {
			IRI seeAlso = RdfUtils.codeListIRI(CONCEPT + codeList.getString(LAST_CLASS_URI_SEGMENT));
			return repoGestion.getResponseAsBoolean(CodeListQueries.checkCodeListUnicity(id, iri, RdfUtils.toString(seeAlso), false));
		}
		return repoGestion.getResponseAsBoolean(CodeListQueries.checkCodeListUnicity(id, iri, "", true));
	}

	@Override
	public String setCodesList(String body, boolean partial) throws RmesException {
		JSONObject codesList = new JSONObject(body);

		this.validateCodeList(codesList, partial);

		IRI codeListIri = this.generateIri(codesList, partial);

		if(this.checkCodeListUnicity(partial, codesList, RdfUtils.toString(codeListIri))){
			throw new RmesBadRequestException(ErrorCodes.CODE_LIST_UNICITY,
					"The identifier, IRI and OWL class should be unique", "");
		}

		repoGestion.clearStructureNodeAndComponents(codeListIri);
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.codesListGraph();
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, DateUtils.getCurrentDate(), model, graph);
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);
		return this.createOrUpdateCodeList(model, graph, codesList, codeListIri, partial);
	}

	@Override
	public String setCodesList(String id, String body, boolean partial) throws RmesException {
		JSONObject codesList = new JSONObject(body);

		this.validateCodeList(codesList, partial);

		IRI codeListIri = this.generateIri(codesList, partial);
		repoGestion.clearStructureNodeAndComponents(codeListIri);
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.codesListGraph();

		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, codesList.getString("created"), model, graph);
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);

		return this.createOrUpdateCodeList(model, graph, codesList, codeListIri, partial);
	}

	@Override
	public String getPartialCodeListByParent(String parentCode) throws RmesException {
		JSONObject parent = this.getDetailedCodesListJson(parentCode, false);
		String parentIRI = parent.getString("iri");
		JSONArray partials = repoGestion.getResponseAsArray(CodeListQueries.getPartialCodeListByParentUri(parentIRI));
		return partials.toString();
	}

	@Override
	public void deleteCodeList(String notation, boolean partial) throws RmesException {
		JSONObject codesList = getDetailedCodesListJson(notation, partial);
		String iri = codesList.getString("iri");

		if(!codesList.getString(VALIDATION_STATE).equalsIgnoreCase("Unpublished")){
			throw new RmesBadRequestException(ErrorCodes.CODE_LIST_DELETE_ONLY_UNPUBLISHED, "Only unpublished codelist can be deleted");
		}

		if(!partial){
			JSONArray partials = repoGestion.getResponseAsArray(CodeListQueries.getPartialCodeListByParentUri(iri));
			if(!partials.isEmpty()){
				throw new RmesBadRequestException(ErrorCodes.CODE_LIST_DELETE_CODELIST_WITHOUT_PARTIAL, "Only codelist with partial codelists can be deleted");
			}
			if(codesList.has(CODES)) {
				JSONObject codes = codesList.getJSONObject(CODES);
				for (String key : codes.keySet()) {
					String codeIri = codes.getJSONObject(key).getString("iri");
					repoGestion.deleteObject(RdfUtils.toURI(codeIri), null);
				}
			}
		}

		repoGestion.deleteObject(RdfUtils.toURI(iri), null);
	}

	@Override
	public String publishCodeList(String id, boolean partial) throws RmesException {

		JSONObject codesList = getDetailedCodesListJson(id, partial);
		String iri = codesList.getString("iri");
		IRI codelist = RdfUtils.createIRI(iri);

		codeListPublication.publishCodeListAndCodes(codelist);

		Model model = new LinkedHashModel();
		model.add(codelist, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.codesListGraph());
		model.remove(codelist, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.codesListGraph());
		model.remove(codelist, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.codesListGraph());

		repoGestion.objectValidation(codelist, model);

		return id;
	}



	private String createOrUpdateCodeList(Model model, Resource graph, JSONObject codesList, IRI codeListIri, boolean partial) throws RmesException {
		String codeListId = codesList.getString(Constants.ID);

		if(codesList.has(VALIDATION_STATE) && codesList.getString(VALIDATION_STATE).equalsIgnoreCase(ValidationStatus.VALIDATED.getValue())){
			model.add(codeListIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), graph);
		} else {
			model.add(codeListIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graph);
		}

		IRI type = partial ? SKOS.COLLECTION : SKOS.CONCEPT_SCHEME ;
		RdfUtils.addTripleUri(codeListIri, RDF.TYPE, type, model, graph);
		model.add(codeListIri, SKOS.NOTATION, RdfUtils.setLiteralString(codeListId), graph);


		if(codesList.has("disseminationStatus")){
			RdfUtils.addTripleUri(codeListIri, INSEE.DISSEMINATIONSTATUS, codesList.getString("disseminationStatus"), model, graph);
		}

		model.add(codeListIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(codesList.getString(Constants.LABEL_LG1), config.getLg1()), graph);
		model.add(codeListIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(codesList.getString(Constants.LABEL_LG2), config.getLg2()), graph);


		if(codesList.has(Constants.DESCRIPTION_LG1)){
			model.add(codeListIri, SKOS.DEFINITION, RdfUtils.setLiteralString(codesList.getString(Constants.DESCRIPTION_LG1), config.getLg1()), graph);
		}
		if(codesList.has(Constants.DESCRIPTION_LG2)){
			model.add(codeListIri, SKOS.DEFINITION, RdfUtils.setLiteralString(codesList.getString(Constants.DESCRIPTION_LG2), config.getLg2()), graph);
		}
		if(codesList.has(Constants.CREATOR)){
			RdfUtils.addTripleString(codeListIri, DC.CREATOR, codesList.getString(Constants.CREATOR), model, graph);
		}
		if(codesList.has(Constants.CONTRIBUTOR)){
			codesList.getJSONArray(Constants.CONTRIBUTOR).toList().forEach(c -> RdfUtils.addTripleString(codeListIri, DC.CONTRIBUTOR, (String) c, model, graph));
		}

		if(partial){
			if(codesList.has(CODES)) {
				JSONObject codes = codesList.getJSONObject(CODES);
				for (String key : codes.keySet()) {
					JSONObject code = codes.getJSONObject(key);
					RdfUtils.addTripleUri(codeListIri, SKOS.MEMBER, RdfUtils.createIRI(code.getString("iri")), model, graph);
				}
			}
			if(codesList.has("iriParent")){
				RdfUtils.addTripleUri(codeListIri, PROV.WAS_DERIVED_FROM, codesList.getString("iriParent"), model, graph);
			}
		} else {
			RdfUtils.addTripleString(codeListIri, RdfUtils.createIRI(baseInternalURI + LAST_CODE_URI_SEGMENT), codesList.getString(LAST_CODE_URI_SEGMENT), model, graph);
			IRI owlClassUri = RdfUtils.codeListIRI(CONCEPT + codesList.getString(LAST_CLASS_URI_SEGMENT));
			RdfUtils.addTripleUri(codeListIri, RDFS.SEEALSO, owlClassUri, model, graph);
			RdfUtils.addTripleUri(owlClassUri, RDF.TYPE, OWL.CLASS, model, graph);
			RdfUtils.addTripleUri(owlClassUri, RDFS.SEEALSO, codeListIri, model, graph);
		}
		repoGestion.loadSimpleObject(codeListIri, model, null);
		return codeListId;
	}


	private void createMainCodeTriplet(Resource graph, IRI codeListIri, JSONObject code, Model codeListModel, IRI codeIri, IRI uriOwlClass) {
		RdfUtils.addTripleUri(codeIri, SKOS.IN_SCHEME, codeListIri, codeListModel, graph);
		if(code.has(CODE)){
			RdfUtils.addTripleString(codeIri, SKOS.NOTATION, code.getString(CODE), codeListModel, graph);
		}
		RdfUtils.addTripleUri(codeIri, RDF.TYPE, SKOS.CONCEPT, codeListModel, graph);
		RdfUtils.addTripleUri(codeIri, RDF.TYPE, uriOwlClass, codeListModel, graph);

		if(code.has(Constants.LABEL_LG1)){
			codeListModel.add(codeIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(code.getString(Constants.LABEL_LG1), config.getLg1()), graph);
		}
		if(code.has(Constants.LABEL_LG2)){
			codeListModel.add(codeIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(code.getString(Constants.LABEL_LG2), config.getLg2()), graph);
		}

		if(code.has(Constants.DESCRIPTION_LG1)){
			codeListModel.add(codeIri, SKOS.DEFINITION, RdfUtils.setLiteralString(code.getString(Constants.DESCRIPTION_LG1), config.getLg1()), graph);
		}
		if(code.has(Constants.DESCRIPTION_LG2)){
			codeListModel.add(codeIri, SKOS.DEFINITION, RdfUtils.setLiteralString(code.getString(Constants.DESCRIPTION_LG2), config.getLg2()), graph);
		}
	}


	private JSONArray getCodesForList(JSONArray codes, JSONObject list) {
		JSONArray codesList = new JSONArray();
		for (int i = 0 ; i < codes.length(); i++) {
			JSONObject code = codes.getJSONObject(i);
			if(code.getString(Constants.ID).equalsIgnoreCase(list.getString(Constants.ID))){
				codesList.put(code);
			}
		}
		return codesList;
	}

	@Override
	public String getCode(String notationCodeList, String notationCode) throws RmesException{
		JSONObject code = repoGestion.getResponseAsObject(CodeListQueries.getCodeByNotation(notationCodeList,notationCode));
		code.put(CODE, notationCode);
		code.put("notationCodeList", notationCodeList);
		return QueryUtils.correctEmptyGroupConcat(code.toString());
	}

	@Override
	public String getCodeUri(String notationCodeList, String notationCode) throws RmesException{
		if (StringUtils.isEmpty(notationCodeList) || StringUtils.isEmpty(notationCode)) {return null;}
		JSONObject code = repoGestion.getResponseAsObject(CodeListQueries.getCodeUriByNotation(notationCodeList,notationCode));
		return QueryUtils.correctEmptyGroupConcat(code.getString(Constants.URI));
	}

	@Override
	public String getAllCodesLists(boolean partial) throws RmesException {
		return repoGestion.getResponseAsArray(CodeListQueries.getAllCodesLists(partial)).toString();
	}

	@Override
	public String geCodesListByIRI(String iri) throws RmesException {
		return repoGestion.getResponseAsArray(CodeListQueries.geCodesListByIRI(iri)).toString();
	}

	@Override
	public String updateCodeFromCodeList(String notation, String code, String body) throws RmesException {
		this.deleteCodeFromCodeList(notation, code);
		return this.addCodeFromCodeList(notation, body);
	}

	@Override
	public String addCodeFromCodeList(String notation, String body) throws RmesException {
		JSONObject code = new JSONObject(body);
		JSONObject codesList = this.getDetailedCodesListJson(notation, false);

		IRI owlClassUri = RdfUtils.codeListIRI(CONCEPT + codesList.getString(LAST_CLASS_URI_SEGMENT));
		String lastCodeUriSegment = codesList.getString(LAST_CODE_URI_SEGMENT);
		IRI codeIri = RdfUtils.codeListIRI(  lastCodeUriSegment + "/" + code.getString(CODE));
		IRI codeListIri = this.generateIri(codesList, false);

		Model codeModel = new LinkedHashModel();
		createMainCodeTriplet(RdfUtils.codesListGraph(), codeListIri, code, codeModel, codeIri, owlClassUri);

		repoGestion.loadSimpleObject(codeIri, codeModel, null);

		return code.getString(CODE);
	}

	@Override
	public String deleteCodeFromCodeList(String notation, String code) throws RmesException {
		JSONObject codesList = this.getDetailedCodesListJson(notation, false);
		String lastCodeUriSegment = codesList.getString(LAST_CODE_URI_SEGMENT);
		IRI codeIri = RdfUtils.codeListIRI(  lastCodeUriSegment + "/" + code);
		repoGestion.deleteObject(codeIri, null);
		return null;
	}
}
