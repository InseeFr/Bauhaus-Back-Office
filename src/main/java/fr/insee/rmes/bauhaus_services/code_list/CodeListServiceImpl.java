package fr.insee.rmes.bauhaus_services.code_list;

import javax.ws.rs.BadRequestException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.utils.DateUtils;

@Service
public class CodeListServiceImpl extends RdfService implements CodeListService  {

	static final Logger logger = LogManager.getLogger(CodeListServiceImpl.class);
	
	@Autowired	
	LangService codeListUtils;
	
	@Autowired
	FamOpeSerIndUtils famOpeSerIndUtils;
	
	
	@Override
	public String getCodeListJson(String notation) throws RmesException{
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getCodeListLabelByNotation(notation));
		codeList.put("notation",notation);
		JSONArray items = repoGestion.getResponseAsArray(CodeListQueries.getCodeListItemsByNotation(notation));
		if (items.length() != 0){
			codeList.put("codes", items);
		}
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
	
	public CodeList getCodeList(String notation) throws RmesException {
		return buildCodeListFromJson(getCodeListJson(notation));	
	}

	@Override
	public String getDetailedCodesList(String notation) throws RmesException {
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getDetailedCodeListByNotation(notation));
		JSONArray codes = repoGestion.getResponseAsArray(CodeListQueries.getDetailedCodes(notation));

		if(codes.length() > 0){
			JSONObject formattedCodes = new JSONObject();
			codes.forEach(c -> {
				JSONObject tempCode = (JSONObject) c;
				String code = tempCode.getString("code");

				if(!formattedCodes.has(code)){
					if(tempCode.has(Constants.PARENTS)){
						JSONArray parents = new JSONArray();
						parents.put(tempCode.getString(Constants.PARENTS));
						tempCode.put(Constants.PARENTS, parents);
					}

					formattedCodes.put(code, tempCode);
				} else {
					JSONObject previousCode = formattedCodes.getJSONObject(code);

					JSONArray parents = new JSONArray();
					if(previousCode.has(Constants.PARENTS)){
						parents = previousCode.getJSONArray(Constants.PARENTS);
					}
					parents.put(tempCode.getString(Constants.PARENTS));
					previousCode.put(Constants.PARENTS, parents);
					formattedCodes.put(code, previousCode);
				}
			});


			codeList.put("codes", formattedCodes);
		}

		return codeList.toString();
	}

	@Override
	public String getDetailedCodesListForSearch() throws RmesException {
		JSONArray lists =  repoGestion.getResponseAsArray(CodeListQueries.getCodesListsForSearch());
		JSONArray codes =  repoGestion.getResponseAsArray(CodeListQueries.getCodesForSearch());

		for (int i = 0 ; i < lists.length(); i++) {
			JSONObject list = lists.getJSONObject(i);
			list.put("codes", this.getCodesForList(codes, list));
		}

		return lists.toString();
	}

	public void validateCodeList(JSONObject codeList){
		if (!codeList.has("id")) {
			throw new BadRequestException("The id of the list should be defined");
		}
		if (!codeList.has("labelLg1")) {
			throw new BadRequestException("The labelLg1 of the list should be defined");
		}
		if (!codeList.has("labelLg2")) {
			throw new BadRequestException("The labelLg2 of the list should be defined");
		}
		if (!codeList.has("lastClassUriSegment")) {
			throw new BadRequestException("The lastClassUriSegment of the list should be defined");
		}
		if (!codeList.has("lastListUriSegment")) {
			throw new BadRequestException("The lastListUriSegment of the list should be defined");
		}
	}
	@Override
	public String setCodesList(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSONObject codesList = new JSONObject(body);

		this.validateCodeList(codesList);

		IRI codeListIri = RdfUtils.codeListIRI(codesList.getString("lastListUriSegment"));
		repoGestion.clearStructureNodeAndComponents(codeListIri);
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.codesListGraph();
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, DateUtils.getCurrentDate(), model, graph);
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);
		return this.createOrUpdateCodeList(model, graph, codesList, codeListIri);
	}

	@Override
	public String setCodesList(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSONObject codesList = new JSONObject(body);

		this.validateCodeList(codesList);

		IRI codeListIri = RdfUtils.codeListIRI(codesList.getString("lastListUriSegment"));
		repoGestion.clearStructureNodeAndComponents(codeListIri);
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.codesListGraph();

		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, codesList.getString("created"), model, graph);
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);

		return this.createOrUpdateCodeList(model, graph, codesList, codeListIri);
	}

	private String createOrUpdateCodeList(Model model, Resource graph, JSONObject codesList, IRI codeListIri) throws RmesException {

		model.add(codeListIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graph);

		RdfUtils.addTripleUri(codeListIri, RDF.TYPE, SKOS.CONCEPT_SCHEME, model, graph);
		model.add(codeListIri, SKOS.NOTATION, RdfUtils.setLiteralString(codesList.getString("id")), graph);

		IRI owlClassUri = RdfUtils.codeListIRI("concept/" + codesList.getString("lastClassUriSegment"));
		RdfUtils.addTripleUri(codeListIri, RDFS.SEEALSO, owlClassUri, model, graph);
		RdfUtils.addTripleUri(owlClassUri, RDF.TYPE, OWL.CLASS, model, graph);
		RdfUtils.addTripleUri(owlClassUri, RDFS.SEEALSO, codeListIri, model, graph);

		if(codesList.has("disseminationStatus")){
			RdfUtils.addTripleUri(codeListIri, INSEE.DISSEMINATIONSTATUS, codesList.getString("disseminationStatus"), model, graph);
		}

		model.add(codeListIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(codesList.getString("labelLg1"), Config.LG1), graph);
		model.add(codeListIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(codesList.getString("labelLg2"), Config.LG2), graph);


		if(codesList.has("descriptionLg1")){
			model.add(codeListIri, SKOS.DEFINITION, RdfUtils.setLiteralString(codesList.getString("descriptionLg1"), Config.LG1), graph);
		}
		if(codesList.has("descriptionLg2")){
			model.add(codeListIri, SKOS.DEFINITION, RdfUtils.setLiteralString(codesList.getString("descriptionLg2"), Config.LG2), graph);
		}
		if(codesList.has("creator")){
			RdfUtils.addTripleString(codeListIri, DC.CREATOR, codesList.getString("creator"), model, graph);
		}
		if(codesList.has("contributor")){
			RdfUtils.addTripleString(codeListIri, DC.CONTRIBUTOR, codesList.getString("contributor"), model, graph);
		}
		repoGestion.loadSimpleObject(codeListIri, model, null);
		return ((SimpleIRI)codeListIri).toString();
	}


	private JSONArray getCodesForList(JSONArray codes, JSONObject list) {
		JSONArray codesList = new JSONArray();
		for (int i = 0 ; i < codes.length(); i++) {
			JSONObject code = codes.getJSONObject(i);
			if(code.getString("id").equalsIgnoreCase(list.getString("id"))){
				codesList.put(code);
			}
		}
		return codesList;
	}

	@Override
	public String getCode(String notationCodeList, String notationCode) throws RmesException{
		JSONObject code = repoGestion.getResponseAsObject(CodeListQueries.getCodeByNotation(notationCodeList,notationCode));
		code.put("code", notationCode);
		code.put("notationCodeList", notationCodeList);
		return QueryUtils.correctEmptyGroupConcat(code.toString());
	}

	@Override
	public String getCodeUri(String notationCodeList, String notationCode) throws RmesException{
			if (StringUtils.isEmpty(notationCodeList) ||StringUtils.isEmpty(notationCode)) {return null;}
			JSONObject code = repoGestion.getResponseAsObject(CodeListQueries.getCodeUriByNotation(notationCodeList,notationCode));
			return QueryUtils.correctEmptyGroupConcat(code.getString(Constants.URI));
	}

	@Override
	public String getAllCodesLists() throws RmesException {
		return repoGestion.getResponseAsArray(CodeListQueries.getAllCodesLists()).toString();
	}

	@Override
	public String geCodesListByIRI(String IRI) throws RmesException {
		return repoGestion.getResponseAsArray(CodeListQueries.geCodesListByIRI(IRI)).toString();
	}
}
