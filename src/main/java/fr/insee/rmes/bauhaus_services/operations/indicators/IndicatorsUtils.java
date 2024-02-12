package fr.insee.rmes.bauhaus_services.operations.indicators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.*;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
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
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class IndicatorsUtils {

	static final Logger logger = LoggerFactory.getLogger(IndicatorsUtils.class);

	protected final RepositoryGestion repositoryGestion;

	final CodeListService codeListService;

	final OrganizationsService organizationsService;

	final IndicatorPublication indicatorPublication;

	final FamOpeSerIndUtils famOpeSerIndUtils;
	
	final ParentUtils ownersUtils;

	final StampsRestrictionsService stampsRestrictionsService;

	private final DocumentationsUtils documentationsUtils;
	private final UriUtils uriUtils;
	private final String lg1;
	private final String lg2;
	private final boolean indicatorsRichTextNexStructure;

	public IndicatorsUtils(
			@Value("${fr.insee.rmes.bauhaus.feature-flipping.operations.indicators-rich-text-new-structure}") boolean indicatorsRichTextNexStructure,
			RepositoryGestion repositoryGestion,
			CodeListService codeListService,
			OrganizationsService organizationsService,
			IndicatorPublication indicatorPublication,
			FamOpeSerIndUtils famOpeSerIndUtils,
			ParentUtils ownersUtils,
			StampsRestrictionsService stampsRestrictionsService, DocumentationsUtils documentationsUtils,
			UriUtils uriUtils,
			@Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
			@Value("${fr.insee.rmes.bauhaus.lg2}") String lg2) {
		this.indicatorsRichTextNexStructure = indicatorsRichTextNexStructure;
		this.repositoryGestion = repositoryGestion;
		this.codeListService = codeListService;
		this.organizationsService = organizationsService;
		this.indicatorPublication = indicatorPublication;
		this.famOpeSerIndUtils = famOpeSerIndUtils;
		this.ownersUtils = ownersUtils;
		this.stampsRestrictionsService = stampsRestrictionsService;
		this.documentationsUtils = documentationsUtils;
		this.uriUtils = uriUtils;
		this.lg1 = lg1;
		this.lg2 = lg2;
	}

	private void validate(Indicator indicator) throws RmesException {
		if(repositoryGestion.getResponseAsBoolean(IndicatorsQueries.checkPrefLabelUnicity(indicator.getId(), indicator.getPrefLabelLg1(), lg1))){
			throw new RmesUnauthorizedException(ErrorCodes.OPERATION_INDICATOR_EXISTING_PREF_LABEL_LG1, "This prefLabelLg1 is already used by another indicator.");
		}
		if(repositoryGestion.getResponseAsBoolean(IndicatorsQueries.checkPrefLabelUnicity(indicator.getId(), indicator.getPrefLabelLg2(), lg2))){
			throw new RmesUnauthorizedException(ErrorCodes.OPERATION_INDICATOR_EXISTING_PREF_LABEL_LG2, "This prefLabelLg2 is already used by another indicator.");
		}
	}

	public Indicator getIndicatorById(String id) throws RmesException{
		return buildIndicatorFromJson(getIndicatorJsonById(id), false);
	}
	
	public Indicator getIndicatorById(String id, boolean forXML) throws RmesException{
		return buildIndicatorFromJson(getIndicatorJsonById(id), forXML);
	}

	/**
	 * From json issued of the database to Java Object
	 * @param indicatorJson
	 * @return
	 */
	public Indicator buildIndicatorFromJson(JSONObject indicatorJson) {
		return buildIndicatorFromJson(indicatorJson,false);
	}
	
	public Indicator buildIndicatorFromJson(JSONObject indicatorJson, boolean forXML) {
		ObjectMapper mapper = new ObjectMapper();
		String id= indicatorJson.getString(Constants.ID);
		Indicator indicator = new Indicator(id);
		try {
			if(forXML) indicator = mapper.readValue(XMLUtils.solveSpecialXmlcharacters(indicatorJson.toString()), Indicator.class);
			else indicator = mapper.readValue(indicatorJson.toString(), Indicator.class);
		} catch (JsonProcessingException e) {
			logger.error("Json cannot be parsed: ".concat(e.getMessage()));
		}
		if (indicatorJson.has(Constants.CONTRIBUTORS)) {
			List<OperationsLink> contributors = buildListFromJsonToArray(indicatorJson, Constants.CONTRIBUTORS);
			indicator.setContributors(contributors);
		}
		if (indicatorJson.has(Constants.SEEALSO)) {
			List<OperationsLink> seeAlsoes = buildListFromJsonToArray(indicatorJson, Constants.SEEALSO);
			indicator.setSeeAlso(seeAlsoes);
		}
		if (indicatorJson.has(Constants.REPLACES)) {
			List<OperationsLink> replacesList = buildListFromJsonToArray(indicatorJson, Constants.REPLACES);
			indicator.setReplaces(replacesList);
		}
		if (indicatorJson.has(Constants.ISREPLACEDBY)) {
			List<OperationsLink> isReplacedByList = buildListFromJsonToArray(indicatorJson, Constants.ISREPLACEDBY);
			indicator.setIsReplacedBy(isReplacedByList);
		}
		if (indicatorJson.has(Constants.WASGENERATEDBY)) {
			List<OperationsLink> wasGeneratedByList = buildListFromJsonToArray(indicatorJson, Constants.WASGENERATEDBY);
			indicator.setWasGeneratedBy(wasGeneratedByList);
		}
		return indicator;
	}


	private List<OperationsLink> buildListFromJsonToArray(JSONObject jsonIndicator, String constant) {
		List<OperationsLink> list = new ArrayList<>();
		List<Object> objects = famOpeSerIndUtils.buildObjectListFromJson(jsonIndicator.getJSONArray(constant),
				OperationsLink.getClassOperationsLink());
		for (Object o : objects) {
			list.add((OperationsLink) o);
		}
		return list;
	}

	/**
	 * From database
	 * @param id
	 * @return
	 * @throws RmesException
	 */
	public JSONObject getIndicatorJsonById(String id) throws RmesException {
		if (!checkIfIndicatorExists(id)) {
			throw new RmesNotFoundException(ErrorCodes.INDICATOR_UNKNOWN_ID,"Indicator not found: ", id);
		}
		JSONObject indicator = repositoryGestion.getResponseAsObject(IndicatorsQueries.indicatorQuery(id, this.indicatorsRichTextNexStructure));
		XhtmlToMarkdownUtils.convertJSONObject(indicator);
		indicator.put(Constants.ID, id);
		addLinks(id, indicator);
		addIndicatorCreators(id, indicator);
		return indicator;
	}


	private void addIndicatorCreators(String id, JSONObject indicator) throws RmesException {
		indicator.put(Constants.CREATORS, ownersUtils.getIndicatorCreators(id));
	}


	private void addIndicatorPublishers(String id, JSONObject indicator) throws RmesException {
		JSONArray publishers = repositoryGestion.getResponseAsJSONList(IndicatorsQueries.getPublishersById(id));
		indicator.put(Constants.PUBLISHERS, publishers);
	}

	/**
	 * From database
	 * @param idIndic
	 * @param indicator
	 * @throws RmesException
	 */
	private void addLinks(String idIndic, JSONObject indicator) throws RmesException {
		addOneTypeOfLink(idIndic,indicator,DCTERMS.REPLACES);
		addOneTypeOfLink(idIndic,indicator,DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idIndic,indicator,RDFS.SEEALSO);
		addOneTypeOfLink(idIndic,indicator,PROV.WAS_GENERATED_BY);
		addOneOrganizationLink(idIndic,indicator, DCTERMS.CONTRIBUTOR);
		addOneOrganizationLink(idIndic,indicator, DCTERMS.PUBLISHER);
		famOpeSerIndUtils.fixOrganizationsNames(indicator);
	}

	private void addOneTypeOfLink(String id, JSONObject object, IRI predicate) throws RmesException {
		JSONArray links = repositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			object.put(predicate.getLocalName(), links);
		}
	}

	private void addOneOrganizationLink(String id, JSONObject object, IRI predicate) throws RmesException {
		JSONArray organizations = repositoryGestion.getResponseAsArray(IndicatorsQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			for (int i = 0; i < organizations.length(); i++) {
				JSONObject orga = organizations.getJSONObject(i);
				orga.put("type", ObjectType.ORGANIZATION.labelType());
			}
		}
		object.put(predicate.getLocalName(), organizations);
	}

	/**
	 * Create
	 * @param body
	 * @return
	 * @throws RmesException 
	 */
	public String setIndicator(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Indicator indicator = new Indicator();
		String id=createID();
		if (id == null) {
			logger.error("Create indicator cancelled - no id");
			return null;
		}
		try {
			indicator = mapper.readValue(body, Indicator.class);
			indicator.setId(id);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		indicator.setCreated(DateUtils.getCurrentDate());
		indicator.setUpdated(DateUtils.getCurrentDate());
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

		indicator.setUpdated(DateUtils.getCurrentDate());

		String status= ownersUtils.getIndicatorsValidationStatus(id);

		documentationsUtils.updateDocumentationTitle(indicator.getIdSims(), indicator.getPrefLabelLg1(), indicator.getPrefLabelLg2());
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfIndicator(indicator,ValidationStatus.UNPUBLISHED);
		} else {
			createRdfIndicator(indicator,ValidationStatus.MODIFIED);
		}

		logger.info("Update indicator : {} - {}" , indicator.getId() , indicator.getPrefLabelLg1());

	}

	public String getIndicatorsForSearch() throws RmesException {
		logger.info("Starting to get indicators list");

		JSONArray resQuery = repositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorsQueryForSearch());

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

	private void addNewSyntaxToMultiLangValues(IRI indicatorIRI, String value, String lang, Model model, Resource graph, IRI predicate) throws RmesException {
		IRI iri = RdfUtils.addTripleStringMdToXhtml2(indicatorIRI, predicate, value, lang, "resume", model, graph);
		if (iri != null) {
			repositoryGestion.deleteObject(iri, null);
		}
	}

	public void addMulltiLangValues(Model model, IRI indicatorIRI, Resource graph, String valueLg1, String valueLg2, IRI predicate) throws RmesException {
		RdfUtils.addTripleStringMdToXhtml(indicatorIRI, predicate, valueLg1, lg1, model, graph);
		RdfUtils.addTripleStringMdToXhtml(indicatorIRI, predicate, valueLg2, lg2, model, graph);

		if(indicatorsRichTextNexStructure){
			addNewSyntaxToMultiLangValues(indicatorIRI, valueLg1, lg1, model, graph, predicate);
			addNewSyntaxToMultiLangValues(indicatorIRI, valueLg2, lg2, model, graph, predicate);
		}
	}

	private void createRdfIndicator(Indicator indicator, ValidationStatus newStatus) throws RmesException {
		validate(indicator);

		Model model = new LinkedHashModel();
		IRI indicURI = RdfUtils.objectIRI(ObjectType.INDICATOR,indicator.getId());
		/*Const*/
		model.add(indicURI, RDF.TYPE, INSEE.INDICATOR, RdfUtils.productsGraph());
		/*Required*/
		model.add(indicURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(indicator.getPrefLabelLg1(), lg1), RdfUtils.productsGraph());
		model.add(indicURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.productsGraph());
		/*Optional*/
		RdfUtils.addTripleString(indicURI, SKOS.PREF_LABEL, indicator.getPrefLabelLg2(), lg2, model, RdfUtils.productsGraph());
		RdfUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg1(), lg1, model, RdfUtils.productsGraph());
		RdfUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg2(), lg2, model, RdfUtils.productsGraph());
		RdfUtils.addTripleDateTime(indicURI, DCTERMS.CREATED, indicator.getCreated(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleDateTime(indicURI, DCTERMS.MODIFIED, indicator.getUpdated(), model, RdfUtils.operationsGraph());

		addMulltiLangValues(model, indicURI, RdfUtils.productsGraph(), indicator.getAbstractLg1(), indicator.getAbstractLg2(), DCTERMS.ABSTRACT);
		addMulltiLangValues(model, indicURI, RdfUtils.productsGraph(), indicator.getHistoryNoteLg1(), indicator.getHistoryNoteLg2(), SKOS.HISTORY_NOTE);

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
		addOneWayLink(model, indicURI, indicator.getWasGeneratedBy(), PROV.WAS_GENERATED_BY);

		List<OperationsLink> replaces = indicator.getReplaces();
		if (replaces != null) {
			for (OperationsLink replace : replaces) {
				String replaceUri = this.uriUtils.getCompleteUriGestion(replace.getType(), replace.getId());
				addReplacesAndReplacedBy(model, RdfUtils.toURI(replaceUri), indicURI);
			}
		}		
		
		List<OperationsLink> isReplacedBys = indicator.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				String isReplUri = this.uriUtils.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
				addReplacesAndReplacedBy(model, indicURI, RdfUtils.toURI(isReplUri));
			}
		}

		repositoryGestion.loadObjectWithReplaceLinks(indicURI, model);
	}

	public String setIndicatorValidation(String id)  throws RmesException  {
		if(!stampsRestrictionsService.canValidateIndicator(RdfUtils.objectIRI(ObjectType.INDICATOR, id))) {
			throw new RmesUnauthorizedException(ErrorCodes.INDICATOR_VALIDATION_RIGHTS_DENIED, "Only authorized users can publish indicators.");
		}

		indicatorPublication.publishIndicator(id);

		IRI indicatorURI = RdfUtils.objectIRI(ObjectType.INDICATOR, id);

		Model model = new LinkedHashModel();
		model.add(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.productsGraph());
		model.remove(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.productsGraph());
		model.remove(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.productsGraph());
		logger.info("Validate indicator : {}" , indicatorURI);

		repositoryGestion.objectValidation(indicatorURI, model);

		return id;
	}

	private void addOneWayLink(Model model, IRI indicURI, List<OperationsLink> links, IRI linkPredicate) {
		if (links != null) {
			for (OperationsLink oneLink : links) {
				String linkedObjectUri = this.uriUtils.getCompleteUriGestion(oneLink.getType(), oneLink.getId());
				RdfUtils.addTripleUri(indicURI, linkPredicate ,linkedObjectUri, model, RdfUtils.productsGraph());
			}
		}
	}
	
	private void addReplacesAndReplacedBy(Model model, IRI previous, IRI next) {
		RdfUtils.addTripleUri(previous, DCTERMS.IS_REPLACED_BY ,next, model, RdfUtils.productsGraph());
		RdfUtils.addTripleUri(next, DCTERMS.REPLACES ,previous, model, RdfUtils.productsGraph());
	}

	public String createID() throws RmesException {
		logger.info("Generate indicator id");
		JSONObject json = repositoryGestion.getResponseAsObject(IndicatorsQueries.lastID());
		logger.debug("JSON for indicator id : {}" , json);
		if (json.length()==0) {return null;}
		String id = json.getString(Constants.ID);
		if (id.equals(Constants.UNDEFINED)) {return null;}
		int idInt = Integer.parseInt(id.substring(1))+1;
		return "p" + idInt;
	}

	public boolean checkIfIndicatorExists(String id) throws RmesException {
		return repositoryGestion.getResponseAsBoolean(IndicatorsQueries.checkIfExists(id));
	}



}
