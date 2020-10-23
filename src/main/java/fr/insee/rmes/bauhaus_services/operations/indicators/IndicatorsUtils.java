package fr.insee.rmes.bauhaus_services.operations.indicators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.PROV;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Component
public class IndicatorsUtils  extends RdfService {

	static final Logger logger = LogManager.getLogger(IndicatorsUtils.class);

	@Autowired
	CodeListService codeListService;

	@Autowired
	OrganizationsService organizationsService;

	@Autowired
	IndicatorPublication indicatorPublication;

	@Autowired
	FamOpeSerIndUtils famOpeSerIndUtils;

	public Indicator getIndicatorById(String id) throws RmesException{
		return buildIndicatorFromJson(getIndicatorJsonById(id));
	}

	private Indicator buildIndicatorFromJson2(JSONObject indicatorJson) {
		ObjectMapper mapper = new ObjectMapper();
		String id= indicatorJson.getString(Constants.ID);
		Indicator indicator = new Indicator(id);
		try {
			indicator = mapper.readValue(indicatorJson.toString(), Indicator.class);
		} catch (JsonProcessingException e) {
			logger.error("Json cannot be parsed: ".concat(e.getMessage()));
		}
		return indicator;
	}

	private Indicator buildIndicatorFromJson(JSONObject jsonIndicator) throws RmesException {
		String id= jsonIndicator.getString(Constants.ID);
		Indicator indicator = new Indicator(id);
		if(jsonIndicator.has(Constants.PREF_LABEL_LG1)) {
			indicator.setPrefLabelLg1(jsonIndicator.getString(Constants.PREF_LABEL_LG1));
		}
		if(jsonIndicator.has(Constants.PREF_LABEL_LG2)) {
			indicator.setPrefLabelLg2(jsonIndicator.getString(Constants.PREF_LABEL_LG2)); 
		}
		if(jsonIndicator.has(Constants.ALT_LABEL_LG1)) {
			indicator.setAltLabelLg1(jsonIndicator.getString(Constants.ALT_LABEL_LG1));
		}
		if(jsonIndicator.has(Constants.ALT_LABEL_LG2)) {
			indicator.setAltLabelLg2(jsonIndicator.getString(Constants.ALT_LABEL_LG2));
		}
		if(jsonIndicator.has("abstractLg1")) {
			indicator.setAbstractLg1(jsonIndicator.getString("abstractLg1"));
		}
		if(jsonIndicator.has("abstractLg2")) {
			indicator.setAbstractLg2(jsonIndicator.getString("abstractLg2"));
		}
		if(jsonIndicator.has("historyNoteLg1")) {
			indicator.setHistoryNoteLg1(jsonIndicator.getString("historyNoteLg1"));
		}
		if(jsonIndicator.has("historyNoteLg2")) {
			indicator.setHistoryNoteLg2(jsonIndicator.getString("historyNoteLg2"));
		}
		if(jsonIndicator.has("accrualPeriodicityCode")) {
			indicator.setAccrualPeriodicityCode(jsonIndicator.getString("accrualPeriodicityCode"));
		}
		if(jsonIndicator.has("accrualPeriodicityList")) {
			indicator.setAccrualPeriodicityList(jsonIndicator.getString("accrualPeriodicityList"));
		}
		if(jsonIndicator.has(Constants.CREATORS)) {
			indicator.setCreators(famOpeSerIndUtils.buildStringListFromJson(
					jsonIndicator.getJSONArray(Constants.CREATORS)));
		}
		if(jsonIndicator.has(Constants.PUBLISHERS)) {
			List<OperationsLink> publishers = new ArrayList<>();
			List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(
					jsonIndicator.getJSONArray(Constants.PUBLISHERS),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						publishers.add((OperationsLink) o);		
					}
					indicator.setPublishers((OperationsLink[]) publishers.toArray());
		}
		if(jsonIndicator.has(Constants.ID_SIMS)) {
			indicator.setIdSims(jsonIndicator.getString(Constants.ID_SIMS));
		}
		if(jsonIndicator.has(Constants.CONTRIBUTORS)) {
			List<OperationsLink> contributors = new ArrayList<>();
			List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(
					jsonIndicator.getJSONArray(Constants.CONTRIBUTORS),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						contributors.add((OperationsLink) o);		
					}
					indicator.setContributors(contributors);
		}
		if(jsonIndicator.has(Constants.SEEALSO)) {
			List<OperationsLink> seeAlsoes = new ArrayList<>();
			List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(
					jsonIndicator.getJSONArray(Constants.SEEALSO),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						seeAlsoes.add((OperationsLink) o);		
					}
					indicator.setSeeAlso(seeAlsoes);
		}
		if(jsonIndicator.has(Constants.REPLACES)) {
			List<OperationsLink> replacesList = new ArrayList<>();
			List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(
					jsonIndicator.getJSONArray(Constants.REPLACES),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						replacesList.add((OperationsLink) o);		
					}
					indicator.setReplaces(replacesList);
		}
		if(jsonIndicator.has(Constants.ISREPLACEDBY)) {
			List<OperationsLink> isReplacedByList = new ArrayList<>();
			List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(
					jsonIndicator.getJSONArray(Constants.ISREPLACEDBY),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						isReplacedByList.add((OperationsLink) o);		
					}
					indicator.setIsReplacedBy(isReplacedByList);
		}
		if(jsonIndicator.has(Constants.WASGENERATEDBY)) {
			List<OperationsLink> wasGeneratedByList = new ArrayList<>();
			List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(
					jsonIndicator.getJSONArray(Constants.WASGENERATEDBY),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						wasGeneratedByList.add((OperationsLink) o);		
					}
					indicator.setWasGeneratedBy(wasGeneratedByList);
		}
		return indicator;
	}


	public JSONObject getIndicatorJsonById(String id) throws RmesException{
		if (!checkIfIndicatorExists(id)) {
			throw new RmesNotFoundException(ErrorCodes.INDICATOR_UNKNOWN_ID,"Indicator not found: ", id);
		}
		JSONObject indicator = repoGestion.getResponseAsObject(IndicatorsQueries.indicatorQuery(id));
		XhtmlToMarkdownUtils.convertJSONObject(indicator);
		indicator.put(Constants.ID, id);
		addLinks(id, indicator);
		addIndicatorCreators(id, indicator);
		addIndicatorPublishers(id, indicator);

		return indicator;
	}


	private void addIndicatorCreators(String id, JSONObject indicator) throws RmesException {
		JSONArray creators = repoGestion.getResponseAsJSONList(IndicatorsQueries.getCreatorsById(id));
		indicator.put(Constants.CREATORS, creators);
	}

	private void addIndicatorPublishers(String id, JSONObject indicator) throws RmesException {
		JSONArray publishers = repoGestion.getResponseAsJSONList(IndicatorsQueries.getPublishersById(id));
		indicator.put(Constants.PUBLISHERS, publishers);
	}

	private void addLinks(String idIndic, JSONObject indicator) throws RmesException {
		addOneTypeOfLink(idIndic,indicator,DCTERMS.REPLACES);
		addOneTypeOfLink(idIndic,indicator,DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idIndic,indicator,RDFS.SEEALSO);
		addOneTypeOfLink(idIndic,indicator,PROV.WAS_GENERATED_BY);
		addOneOrganizationLink(idIndic,indicator, DCTERMS.CONTRIBUTOR);
		famOpeSerIndUtils.fixOrganizationsNames(indicator);
	}

	private void addOneTypeOfLink(String id, JSONObject object, IRI predicate) throws RmesException {
		JSONArray links = repoGestion.getResponseAsArray(IndicatorsQueries.indicatorLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			object.put(predicate.getLocalName(), links);
		}
	}

	private void addOneOrganizationLink(String id, JSONObject object, IRI predicate) throws RmesException {
		JSONArray organizations = repoGestion.getResponseAsArray(IndicatorsQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			for (int i = 0; i < organizations.length(); i++) {
				JSONObject orga = organizations.getJSONObject(i);
				orga.put("type", ObjectType.ORGANIZATION.getLabelType());
			}
			object.put(predicate.getLocalName(), organizations);
		}
	}

	/**
	 * Create
	 * @param body
	 * @return
	 * @throws RmesException 
	 */
	public String setIndicator(String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateIndicator()) {
			throw new RmesUnauthorizedException(ErrorCodes.INDICATOR_CREATION_RIGHTS_DENIED, "Only an admin can create a new indicator.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Indicator indicator = new Indicator();
		String id=createID();
		indicator.setId(id);
		if (indicator.getId() == null) {
			logger.error("Create indicator cancelled - no id");
			return null;
		}
		try {
			indicator = mapper.readValue(body, Indicator.class);
			indicator.setId(id);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfIndicator(indicator,ValidationStatus.UNPUBLISHED);
		logger.info("Create indicator : {} - {}" , indicator.getId() , indicator.getPrefLabelLg1());
		return indicator.getId();
	}


	/**
	 * Update
	 * @param id
	 * @param body
	 * @throws RmesException 
	 */
	public void setIndicator(String id, String body) throws RmesException {

		if(!stampsRestrictionsService.canModifyIndicator(RdfUtils.objectIRI(ObjectType.INDICATOR, id))) {
			throw new RmesUnauthorizedException(ErrorCodes.INDICATOR_MODIFICATION_RIGHTS_DENIED, "Only authorized users can modify indicators.");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Indicator indicator = new Indicator(id);
		try {
			indicator = mapper.readerForUpdating(indicator).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		String status=getValidationStatus(id);
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfIndicator(indicator,ValidationStatus.UNPUBLISHED);
		} else {
			createRdfIndicator(indicator,ValidationStatus.MODIFIED);
		}

		logger.info("Update indicator : {} - {}" , indicator.getId() , indicator.getPrefLabelLg1());

	}

	public String getIndicatorsForSearch() throws RmesException {
		logger.info("Starting to get indicators list");

		JSONArray resQuery = repoGestion.getResponseAsArray(IndicatorsQueries.indicatorsQueryForSearch());

		JSONArray result = new JSONArray();
		for (int i = 0; i < resQuery.length(); i++) {
			JSONObject indicator = resQuery.getJSONObject(i);
			addOneOrganizationLink(indicator.get(Constants.ID).toString(),indicator, INSEE.DATA_COLLECTOR);
			addIndicatorCreators(indicator.get(Constants.ID).toString(),indicator);
			addIndicatorPublishers(indicator.get(Constants.ID).toString(),indicator);
			result.put(indicator);
		}
		return QueryUtils.correctEmptyGroupConcat(result.toString());
	}

	private void createRdfIndicator(Indicator indicator, ValidationStatus newStatus) throws RmesException {
		Model model = new LinkedHashModel();
		IRI indicURI = RdfUtils.objectIRI(ObjectType.INDICATOR,indicator.getId());
		/*Const*/
		model.add(indicURI, RDF.TYPE, INSEE.INDICATOR, RdfUtils.productsGraph());
		/*Required*/
		model.add(indicURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(indicator.getPrefLabelLg1(), Config.LG1), RdfUtils.productsGraph());
		model.add(indicURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.productsGraph());
		/*Optional*/
		RdfUtils.addTripleString(indicURI, SKOS.PREF_LABEL, indicator.getPrefLabelLg2(), Config.LG2, model, RdfUtils.productsGraph());
		RdfUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg1(), Config.LG1, model, RdfUtils.productsGraph());
		RdfUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg2(), Config.LG2, model, RdfUtils.productsGraph());

		RdfUtils.addTripleStringMdToXhtml(indicURI, DCTERMS.ABSTRACT, indicator.getAbstractLg1(), Config.LG1, model, RdfUtils.productsGraph());
		RdfUtils.addTripleStringMdToXhtml(indicURI, DCTERMS.ABSTRACT, indicator.getAbstractLg2(), Config.LG2, model, RdfUtils.productsGraph());

		RdfUtils.addTripleStringMdToXhtml(indicURI, SKOS.HISTORY_NOTE, indicator.getHistoryNoteLg1(), Config.LG1, model, RdfUtils.productsGraph());
		RdfUtils.addTripleStringMdToXhtml(indicURI, SKOS.HISTORY_NOTE, indicator.getHistoryNoteLg2(), Config.LG2, model, RdfUtils.productsGraph());

		List<OperationsLink> contributors = indicator.getContributors();
		if (contributors != null){//partenaires
			for (OperationsLink contributor : contributors) {
				RdfUtils.addTripleUri(indicURI, DCTERMS.CONTRIBUTOR,organizationsService.getOrganizationUriById(contributor.getId()),model, RdfUtils.productsGraph());
			}
		}

		List<String> creators=indicator.getCreators();
		if (creators!=null) {
			for (String creator : creators) {
				RdfUtils.addTripleString(indicURI, DC.CREATOR, creator, model, RdfUtils.productsGraph());
			}
		}

		List<OperationsLink> publishers=indicator.getPublishers();
		if (publishers!=null) {
			for (OperationsLink publisher : publishers) {
				RdfUtils.addTripleUri(indicURI, DCTERMS.PUBLISHER, organizationsService.getOrganizationUriById(publisher.getId()), model, RdfUtils.productsGraph());
			}
		}
		
		String accPeriodicityUri = codeListService.getCodeUri(indicator.getAccrualPeriodicityList(), indicator.getAccrualPeriodicityCode());
		RdfUtils.addTripleUri(indicURI, DCTERMS.ACCRUAL_PERIODICITY, accPeriodicityUri, model, RdfUtils.productsGraph());

		addOneWayLink(model, indicURI, indicator.getSeeAlso(), RDFS.SEEALSO);
		addOneWayLink(model, indicURI,  indicator.getReplaces(), DCTERMS.REPLACES);
		addOneWayLink(model, indicURI, indicator.getWasGeneratedBy(), PROV.WAS_GENERATED_BY);

		List<OperationsLink> isReplacedBys = indicator.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
				RdfUtils.addTripleUri(indicURI, DCTERMS.IS_REPLACED_BY ,isReplUri, model, RdfUtils.productsGraph());
				RdfUtils.addTripleUri(RdfUtils.toURI(isReplUri), DCTERMS.REPLACES ,indicURI, model, RdfUtils.productsGraph());
			}
		}

		repoGestion.keepHierarchicalOperationLinks(indicURI,model);

		repoGestion.loadObjectWithReplaceLinks(indicURI, model);
	}

	public String setIndicatorValidation(String id)  throws RmesException  {
		Model model = new LinkedHashModel();

		if(!stampsRestrictionsService.canValidateIndicator(RdfUtils.objectIRI(ObjectType.INDICATOR, id))) {
			throw new RmesUnauthorizedException(ErrorCodes.INDICATOR_VALIDATION_RIGHTS_DENIED, "Only authorized users can publish indicators.");
		}

		indicatorPublication.publishIndicator(id);

		IRI indicatorURI = RdfUtils.objectIRI(ObjectType.INDICATOR, id);
		model.add(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.productsGraph());
		model.remove(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.productsGraph());
		model.remove(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.productsGraph());
		logger.info("Validate indicator : {}" , indicatorURI);

		repoGestion.objectValidation(indicatorURI, model);

		return id;
	}

	private void addOneWayLink(Model model, IRI indicURI, List<OperationsLink> links, IRI linkPredicate) {
		if (links != null) {
			for (OperationsLink oneLink : links) {
				String linkedObjectUri = ObjectType.getCompleteUriGestion(oneLink.getType(), oneLink.getId());
				RdfUtils.addTripleUri(indicURI, linkPredicate ,linkedObjectUri, model, RdfUtils.productsGraph());
			}
		}
	}

	public String createID() throws RmesException {
		logger.info("Generate indicator id");
		JSONObject json = repoGestion.getResponseAsObject(IndicatorsQueries.lastID());
		logger.debug("JSON for indicator id : {}" , json);
		if (json.length()==0) {return null;}
		String id = json.getString(Constants.ID);
		if (id.equals(Constants.UNDEFINED)) {return null;}
		int idInt = Integer.parseInt(id.substring(1))+1;
		return "p" + idInt;
	}

	public boolean checkIfIndicatorExists(String id) throws RmesException {
		return repoGestion.getResponseAsBoolean(IndicatorsQueries.checkIfExists(id));
	}

	public String getValidationStatus(String id) throws RmesException{
		try {
			return repoGestion.getResponseAsObject(IndicatorsQueries.getPublicationState(id)).getString("state"); 
		}
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}

}
