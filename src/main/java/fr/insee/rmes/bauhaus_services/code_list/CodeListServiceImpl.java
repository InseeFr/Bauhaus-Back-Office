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

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CodeListServiceImpl extends RdfService implements CodeListService  {

	private static final String CODES = "codes";

	private static final String LAST_LIST_URI_SEGMENT = "lastListUriSegment";

	static final Logger logger = LogManager.getLogger(CodeListServiceImpl.class);
	
	@Autowired	
	LangService codeListUtils;
	
	@Autowired
	FamOpeSerIndUtils famOpeSerIndUtils;


	@Override
	public String getCodeListJson(String notation) throws RmesException{
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getCodeListLabelByNotation(notation));
		codeList.put(Constants.NOTATION,notation);
		JSONArray items = repoGestion.getResponseAsArray(CodeListQueries.getCodeListItemsByNotation(notation));
		if (items.length() != 0){
			codeList.put(CODES, items);
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
	public String getDetailedCodesList(String notation, boolean partial) throws RmesException {
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getDetailedCodeListByNotation(notation));
		JSONArray codes = repoGestion.getResponseAsArray(CodeListQueries.getDetailedCodes(notation, partial));

		if(!partial){
			JSONObject parents = new JSONObject();

			if(codes.length() > 0){
				JSONObject formattedCodes = new JSONObject();
				codes.forEach(c -> {
					JSONObject tempCode = (JSONObject) c;
					String code = tempCode.getString("code");
					if(tempCode.has(Constants.PARENTS)){
						String parentCode = tempCode.getString(Constants.PARENTS);
						if(!parents.has(parentCode)){
							parents.put(parentCode, new JSONArray().put(code));
							formattedCodes.put(code, tempCode);
						} else {
							parents.put(parentCode, parents.getJSONArray(parentCode).put(code));
						}
						tempCode.remove(Constants.PARENTS);
					}
					formattedCodes.put(code, tempCode);
				});


				if(parents.length() > 0){
					JSONArray seq =  repoGestion.getResponseAsArray(CodeListQueries.getCodesSeq(notation));
					if(seq.length() > 0){
						int startPosition = 0;
						for(int i = 0; i < seq.length(); i++){
							JSONObject code = seq.getJSONObject(i);
							if(parents.has(code.getString("code"))){
								for(int j = startPosition; j < i; j++){
									String childCode = seq.getJSONObject(j).getString("code");

									JSONObject child = formattedCodes.getJSONObject(childCode);

									if(!child.has(Constants.PARENTS)){
										child.put(Constants.PARENTS, new JSONArray());
									}
									child.getJSONArray(Constants.PARENTS).put(new JSONObject().put("code", code.getString("code")).put("position", j - startPosition + 1));
									formattedCodes.put(childCode, child);
								}
								startPosition = i + 1;
							}
						}
					} else {
						// If we do not have a Seq, we have to sort alphabetically.
						parents.keySet().forEach(key -> {
							List<Object> children = parents.getJSONArray(key).toList();
							children.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));

							for(int i = 0; i < children.size(); i++){
								String child = children.get(i).toString();
								JSONObject codeObject = formattedCodes.getJSONObject(child);
								if(!codeObject.has(Constants.PARENTS)){
									codeObject.put(Constants.PARENTS, new JSONArray());
								}
								codeObject.getJSONArray(Constants.PARENTS).put(new JSONObject().put("code", key).put("position", i + 1));
								formattedCodes.put(child, codeObject);
							}
						});
					}

				}

				// Here will order all root codes. Codes without parents
				orderRootCodes(formattedCodes);

				codeList.put(CODES, formattedCodes);
			}
		}
		else {
			formatCodesForPartialList(codeList, codes);
		}

		return codeList.toString();
	}

	/**
	 * Format the codes list for a partial code list.
	 * We just need to remove the parents property.
	 *
	 * @param codeList
	 * @param codes
	 */
	private void formatCodesForPartialList(JSONObject codeList, JSONArray codes) {
		JSONObject formattedCodes = new JSONObject();
		codes.forEach(c -> {
			JSONObject tempCode = (JSONObject) c;
			String code = tempCode.getString("code");
			if (tempCode.has(Constants.PARENTS)) {
				tempCode.remove(Constants.PARENTS);
			}
			formattedCodes.put(code, tempCode);
		});
		codeList.put(CODES, formattedCodes);
	}

	private void orderRootCodes(JSONObject formattedCodes) {
		List<String> rootCodes = formattedCodes.keySet().stream().filter(key -> {
			return !formattedCodes.getJSONObject(key).has(Constants.PARENTS);
		}).sorted(Comparator.comparing(code -> code)).collect(Collectors.toList());
		if(rootCodes.size() > 0) {
			for(int i = 0; i < rootCodes.size(); i++) {
				JSONObject parent = new JSONObject().put("code", "").put("position", i + 1);
				formattedCodes.getJSONObject(rootCodes.get(i)).put(Constants.PARENTS, new JSONArray().put(parent));
			}
		}
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

	public void validateCodeList(JSONObject codeList, boolean partial){
		if (!codeList.has(Constants.ID)) {
			throw new BadRequestException("The id of the list should be defined");
		}
		if (!codeList.has(Constants.LABEL_LG1)) {
			throw new BadRequestException("The labelLg1 of the list should be defined");
		}
		if (!codeList.has(Constants.LABEL_LG2)) {
			throw new BadRequestException("The labelLg2 of the list should be defined");
		}
		if (!partial && !codeList.has("lastClassUriSegment")) {
			throw new BadRequestException("The lastClassUriSegment of the list should be defined");
		}
		if (!codeList.has(LAST_LIST_URI_SEGMENT)) {
			throw new BadRequestException("The lastListUriSegment of the list should be defined");
		}
	}
	@Override
	public String setCodesList(String body, boolean partial) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSONObject codesList = new JSONObject(body);

		this.validateCodeList(codesList, partial);

		IRI codeListIri = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT));
		repoGestion.clearStructureNodeAndComponents(codeListIri);
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.codesListGraph();
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, DateUtils.getCurrentDate(), model, graph);
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);
		return this.createOrUpdateCodeList(model, graph, codesList, codeListIri, partial);
	}

	@Override
	public String setCodesList(String id, String body, boolean partial) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSONObject codesList = new JSONObject(body);

		this.validateCodeList(codesList, partial);

		IRI codeListIri = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT));
		repoGestion.clearStructureNodeAndComponents(codeListIri);
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.codesListGraph();

		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.CREATED, codesList.getString("created"), model, graph);
		RdfUtils.addTripleDateTime(codeListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);

		return this.createOrUpdateCodeList(model, graph, codesList, codeListIri, partial);
	}

	private String createOrUpdateCodeList(Model model, Resource graph, JSONObject codesList, IRI codeListIri, boolean partial) throws RmesException {

		String codeListId = codesList.getString(Constants.ID);

		model.add(codeListIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graph);

		if(partial){
			RdfUtils.addTripleUri(codeListIri, RDF.TYPE, SKOS.COLLECTION, model, graph);
		} else {
			RdfUtils.addTripleUri(codeListIri, RDF.TYPE, SKOS.CONCEPT_SCHEME, model, graph);
		}
		model.add(codeListIri, SKOS.NOTATION, RdfUtils.setLiteralString(codeListId), graph);

		if(!partial){
			IRI owlClassUri = RdfUtils.codeListIRI("concept/" + codesList.getString("lastClassUriSegment"));
			RdfUtils.addTripleUri(codeListIri, RDFS.SEEALSO, owlClassUri, model, graph);
			RdfUtils.addTripleUri(owlClassUri, RDF.TYPE, OWL.CLASS, model, graph);
			RdfUtils.addTripleUri(owlClassUri, RDFS.SEEALSO, codeListIri, model, graph);
		}


		if(codesList.has("disseminationStatus")){
			RdfUtils.addTripleUri(codeListIri, INSEE.DISSEMINATIONSTATUS, codesList.getString("disseminationStatus"), model, graph);
		}

		model.add(codeListIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(codesList.getString(Constants.LABEL_LG1), Config.LG1), graph);
		model.add(codeListIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(codesList.getString(Constants.LABEL_LG2), Config.LG2), graph);


		if(codesList.has(Constants.DESCRIPTION_LG1)){
			model.add(codeListIri, SKOS.DEFINITION, RdfUtils.setLiteralString(codesList.getString(Constants.DESCRIPTION_LG1), Config.LG1), graph);
		}
		if(codesList.has(Constants.DESCRIPTION_LG2)){
			model.add(codeListIri, SKOS.DEFINITION, RdfUtils.setLiteralString(codesList.getString(Constants.DESCRIPTION_LG2), Config.LG2), graph);
		}
		if(codesList.has(Constants.CREATOR)){
			RdfUtils.addTripleString(codeListIri, DC.CREATOR, codesList.getString(Constants.CREATOR), model, graph);
		}
		if(codesList.has(Constants.CONTRIBUTOR)){
			RdfUtils.addTripleString(codeListIri, DC.CONTRIBUTOR, codesList.getString(Constants.CONTRIBUTOR), model, graph);
		}

		if(partial){
			if(codesList.has(CODES)) {
				JSONObject codes = codesList.getJSONObject(CODES);
				for (String key : codes.keySet()) {
					JSONObject code = codes.getJSONObject(key);
					RdfUtils.addTripleUri(codeListIri, SKOS.MEMBER, RdfUtils.createIRI(code.getString("codeUri")), model, graph);
				}
			}
		} else {
			CodeList original = getCodeList(codeListId);
			if(original.getCodes() != null) {
				original.getCodes().forEach(code -> {
					IRI codeIri = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT) + "/" + code.getCode());
					try {
						repoGestion.deleteObject(codeIri, null);
					} catch (RmesException e) {
						logger.error(e.getMessage());
					}
				});
			}
			createCodeTriplet(graph, codesList, codeListIri, model);
		}


		repoGestion.loadSimpleObject(codeListIri, model, null);
		return ((SimpleIRI)codeListIri).toString();
	}

	private void createCodeTriplet(Resource graph, JSONObject codesList, IRI codeListIri, Model codeListModel) {
		if(codesList.has(CODES)){
			JSONObject parentsModel = new JSONObject();

			JSONObject codes = codesList.getJSONObject(CODES);
			for (String key : codes.keySet()) {
				try {
					JSONObject code = codes.getJSONObject(key);

					Model codeModel = new LinkedHashModel();
					IRI codeIri = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT) + "/" + code.get("code"));

					createMainCodeTriplet(graph, codeListIri, code, codeModel, codeIri);

					if (code.has("parents")) {
						JSONArray parentsWithPosition = code.getJSONArray(Constants.PARENTS);
						parentsWithPosition.forEach(parentWithPosition -> {
							String parentCode = ((JSONObject) parentWithPosition).getString("code");
							if(!parentCode.equalsIgnoreCase("")){
								IRI parentIRI = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT) + "/" + parentCode);
								RdfUtils.addTripleUri(codeIri, SKOS.BROADER, parentIRI, codeModel, graph);

								if (parentsModel.has(parentCode)) {
									parentsModel.getJSONArray(parentCode).put(codeIri.toString());
								} else {
									parentsModel.put(parentCode, new JSONArray().put(codeIri.toString()));
								}
							}

						});
					}
					repoGestion.loadSimpleObject(codeIri, codeModel, null);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
			}

			createCodesSeq(graph, codeListIri, parentsModel, codeListModel, codesList);
			createParentChildRelationForCodes(graph, codesList, parentsModel);

		}
	}

	/**
	 * We will create triplets to define the Sequence of codes
	 * @param graph
	 * @param codeListIri
	 * @param parentsModel
	 * @param codeListModel
	 * @param codesList
	 */
	private void createCodesSeq(Resource graph, IRI codeListIri, JSONObject parentsModel, Model codeListModel, JSONObject codesList) {
		RdfUtils.addTripleUri(codeListIri, RDF.TYPE, RDF.SEQ, codeListModel, graph);
		JSONObject codes = codesList.getJSONObject("codes");

		AtomicInteger i = new AtomicInteger();
		parentsModel.keySet().forEach(key -> {
			JSONArray children = parentsModel.getJSONArray(key);
			List<Object> childrenList = children.toList();
			childrenList.sort((child1, child2) -> {
				JSONObject parent1 = findParentPositionForCode(child1, key, codes, codeListIri);
				JSONObject parent2 = findParentPositionForCode(child2, key, codes, codeListIri)	;	;
				return parent1.getInt("position") - parent2.getInt("position");
			});
			childrenList.forEach(child -> {
				IRI childIri = RdfUtils.createIRI((String) child);
				RdfUtils.addTripleUri(codeListIri, RdfUtils.createIRI(RDF.NAMESPACE + "_" + i), childIri, codeListModel, graph);
				i.getAndIncrement();
			});
			IRI parentIRI = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT) + "/" + key);
			RdfUtils.addTripleUri(codeListIri, RdfUtils.createIRI(RDF.NAMESPACE + "_" + i), parentIRI, codeListModel, graph);

			i.getAndIncrement();
		});
	}

	private JSONObject findParentPositionForCode(Object child, String parentCode, JSONObject codes, IRI codeListIri) {
		JSONArray parents = codes
								.getJSONObject(((String) child).replace(codeListIri.toString() + "/", ""))
								.getJSONArray(Constants.PARENTS);
		JSONObject parentWithPosition = new JSONObject();
		for (int i = 0; i < parents.length(); i++){
			if(parents.getJSONObject(i).getString("code").equalsIgnoreCase(parentCode)){
				parentWithPosition = parents.getJSONObject(i);
				break;
			}
		}
		return parentWithPosition;
	}

	private void createParentChildRelationForCodes(Resource graph, JSONObject codesList, JSONObject parentsModel) {
		parentsModel.keySet().forEach(key -> {
			Model parentModel = new LinkedHashModel();
			IRI parentIRI = RdfUtils.codeListIRI(codesList.getString(LAST_LIST_URI_SEGMENT) + "/" + key);
			JSONArray children = parentsModel.getJSONArray(key);
			children.forEach(child -> 
				RdfUtils.addTripleUri(parentIRI, SKOS.NARROWER, (String) child, parentModel, graph)
			);
			try {
				repoGestion.getConnection().add(parentModel);
			} catch (RmesException e) {
				logger.debug(e.getMessage());
			}
		});
	}

	private void createMainCodeTriplet(Resource graph, IRI codeListIri, JSONObject code, Model codeListModel, IRI codeIri) {
		RdfUtils.addTripleUri(codeIri, SKOS.IN_SCHEME, codeListIri, codeListModel, graph);
		if(code.has("code")){
			RdfUtils.addTripleString(codeIri, SKOS.NOTATION, code.getString("code"), codeListModel, graph);
		}

		if(code.has(Constants.LABEL_LG1)){
			codeListModel.add(codeIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(code.getString(Constants.LABEL_LG1), Config.LG1), graph);
		}
		if(code.has(Constants.LABEL_LG2)){
			codeListModel.add(codeIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(code.getString(Constants.LABEL_LG2), Config.LG2), graph);
		}

		if(code.has(Constants.DESCRIPTION_LG1)){
			codeListModel.add(codeIri, SKOS.DEFINITION, RdfUtils.setLiteralString(code.getString(Constants.DESCRIPTION_LG1), Config.LG1), graph);
		}
		if(code.has(Constants.DESCRIPTION_LG2)){
			codeListModel.add(codeIri, SKOS.DEFINITION, RdfUtils.setLiteralString(code.getString(Constants.DESCRIPTION_LG2), Config.LG2), graph);
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
		code.put("code", notationCode);
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
}
