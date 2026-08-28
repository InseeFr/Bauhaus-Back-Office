package fr.insee.rmes.bauhaus_services.operations.indicators;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.errors.IndicatorErrorCode;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class IndicatorsRepository {

	static final Logger logger = LoggerFactory.getLogger(IndicatorsRepository.class);

	protected final RepositoryGestion repositoryGestion;

	final CodeListService codeListService;

	final OrganizationsService organizationsService;

	final IndicatorPublication indicatorPublication;

	final OperationsObjectMapper operationsObjectMapper;
	
	final OperationsParentRepository operationsParentRepository;

	private final DocumentationsUtils documentationsUtils;
	private final BauhausUriBuilder bauhausUriBuilder;
	private final BauhausLanguagesProperties languages;
	private final OperationIndicatorsQueries operationIndicatorsQueries;
	private final OrganisationLookup organisationLookup;

	public IndicatorsRepository(
			RepositoryGestion repositoryGestion,
			CodeListService codeListService,
			OrganizationsService organizationsService,
			IndicatorPublication indicatorPublication,
			OperationsObjectMapper operationsObjectMapper,
			OperationsParentRepository operationsParentRepository,
			DocumentationsUtils documentationsUtils,
			BauhausUriBuilder bauhausUriBuilder,
			BauhausLanguagesProperties languages,
			OperationIndicatorsQueries operationIndicatorsQueries,
			OrganisationLookup organisationLookup) {
		this.repositoryGestion = repositoryGestion;
		this.codeListService = codeListService;
		this.organizationsService = organizationsService;
		this.indicatorPublication = indicatorPublication;
		this.operationsObjectMapper = operationsObjectMapper;
		this.operationsParentRepository = operationsParentRepository;
		this.documentationsUtils = documentationsUtils;
		this.bauhausUriBuilder = bauhausUriBuilder;
		this.languages = languages;
		this.operationIndicatorsQueries = operationIndicatorsQueries;
		this.organisationLookup = organisationLookup;
	}

	void validate(Indicator indicator) throws RmesException {
		if(indicator.isWasGeneratedByEmpty()){
			throw new RmesBadRequestException(IndicatorErrorCode.EMPTY_WAS_GENERATED_BY, "An indicator should be linked to a series.");
		}
		if(repositoryGestion.getResponseAsBoolean(operationIndicatorsQueries.checkPrefLabelUnicity(indicator.getId(), indicator.getPrefLabelLg1(), languages.lg1()))){
			throw new RmesBadRequestException(IndicatorErrorCode.EXISTING_PREF_LABEL_LG1, "This prefLabelLg1 is already used by another indicator.");
		}
		if(repositoryGestion.getResponseAsBoolean(operationIndicatorsQueries.checkPrefLabelUnicity(indicator.getId(), indicator.getPrefLabelLg2(), languages.lg2()))){
			throw new RmesBadRequestException(IndicatorErrorCode.EXISTING_PREF_LABEL_LG2, "This prefLabelLg2 is already used by another indicator.");
		}
		validateOrganisations(indicator);
	}

	private void validateOrganisations(Indicator indicator) throws RmesException {
		if (organisationLookup == null) {
			return;
		}
		List<String> values = new ArrayList<>();
		if (indicator.getCreators() != null) {
			values.addAll(indicator.getCreators());
		}
		if (indicator.getContributors() != null) {
			values.addAll(indicator.getContributors());
		}
		if (indicator.getPublishers() != null) {
			values.addAll(indicator.getPublishers());
		}
		if (values.isEmpty()) {
			return;
		}
		List<String> unknown = organisationLookup.findUnknown(values);
		if (!unknown.isEmpty()) {
			throw new RmesBadRequestException("Unknown organisation references: " + unknown);
		}
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
		String id= indicatorJson.getString(Constants.ID);
		Indicator indicator = Indicator.of(id);
		try {
			if(forXML) indicator = Deserializer.deserializeJsonString(XMLUtils.solveSpecialXmlcharacters(indicatorJson.toString()), Indicator.class);
			else indicator = Deserializer.deserializeJsonString(indicatorJson.toString(), Indicator.class);
		} catch (RmesException e) {
			logger.error("Json cannot be parsed: ".concat(e.getMessage()));
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
		List<Object> objects = operationsObjectMapper.buildObjectListFromJson(jsonIndicator.getJSONArray(constant),
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
		JSONObject indicator = repositoryGestion.getResponseAsObject(operationIndicatorsQueries.indicatorQuery(id));
		XhtmlToMarkdownUtils.convertJSONObject(indicator);
		indicator.put(Constants.ID, id);
		addLinks(id, indicator);
		addIndicatorCreators(id, indicator);
		addIndicatorPublishers(id, indicator);
		addIndicatorContributors(id, indicator);
		return indicator;
	}


	private void addIndicatorCreators(String id, JSONObject indicator) throws RmesException {
		// URI des organisations (objets de dc:creator), pas des stamps.
		// NB : OperationsParentRepository.getIndicatorCreators (canonicalize -> stamps) reste utilisé
		// pour le contrôle d'accès par stamp.
		indicator.put(Constants.CREATORS, repositoryGestion.getResponseAsJSONList(operationIndicatorsQueries.getCreatorsById(id)));
	}


	private void addIndicatorPublishers(String id, JSONObject indicator) throws RmesException {
		// Renvoie les URI des organisations (objets du triplet dcterms:publisher), pas des stamps.
		indicator.put(Constants.PUBLISHERS, repositoryGestion.getResponseAsJSONList(operationIndicatorsQueries.getPublishersById(id)));
	}

	private void addIndicatorContributors(String id, JSONObject indicator) throws RmesException {
		// Renvoie les URI des organisations (objets du triplet dcterms:contributor), pas des stamps.
		indicator.put(Constants.CONTRIBUTORS, repositoryGestion.getResponseAsJSONList(operationIndicatorsQueries.getContributorsById(id)));
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
		operationsObjectMapper.fixOrganizationsNames(indicator);
	}

	private void addOneTypeOfLink(String id, JSONObject object, IRI predicate) throws RmesException {
		JSONArray links = repositoryGestion.getResponseAsArray(operationIndicatorsQueries.indicatorLinks(id, predicate));
		if (!links.isEmpty()) {
			links = QueryUtils.transformRdfTypeInString(links);
			object.put(predicate.getLocalName(), links);
		}
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



		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Indicator indicator = Indicator.of(id);
		try {
			indicator = mapper.readerForUpdating(indicator).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		indicator.setUpdated(DateUtils.getCurrentDate());

		String status= operationsParentRepository.getIndicatorsValidationStatus(id);

		documentationsUtils.updateDocumentationTitle(indicator.getIdSims(), indicator.getPrefLabelLg1(), indicator.getPrefLabelLg2());
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfIndicator(indicator,ValidationStatus.UNPUBLISHED);
		} else {
			createRdfIndicator(indicator,ValidationStatus.MODIFIED);
		}

		logger.info("Update indicator : {} - {}" , indicator.getId() , indicator.getPrefLabelLg1());

	}

	public void addMulltiLangValues(Model model, IRI indicatorIRI, Resource graph, String valueLg1, String valueLg2, IRI predicate) {
		RdfUtils.addTripleStringMdToXhtml(indicatorIRI, predicate, valueLg1, languages.lg1(), model, graph);
		RdfUtils.addTripleStringMdToXhtml(indicatorIRI, predicate, valueLg2, languages.lg2(), model, graph);
	}

	void createRdfIndicator(Indicator indicator, ValidationStatus newStatus) throws RmesException {
		validate(indicator);

		Model model = new LinkedHashModel();
		IRI indicURI = RdfUtils.objectIRI(ObjectType.INDICATOR,indicator.getId());
		/*Const*/
		model.add(indicURI, RDF.TYPE, INSEE.INDICATOR, RdfUtils.productsGraph());
		model.add(indicURI, ADMS.HAS_IDENTIFIER, RdfUtils.setLiteralString(indicator.getId()), RdfUtils.productsGraph());
		/*Required*/
		model.add(indicURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(indicator.getPrefLabelLg1(), languages.lg1()), RdfUtils.productsGraph());
		model.add(indicURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.productsGraph());
		/*Optional*/
		RdfUtils.addTripleString(indicURI, SKOS.PREF_LABEL, indicator.getPrefLabelLg2(), languages.lg2(), model, RdfUtils.productsGraph());
		RdfUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg1(), languages.lg1(), model, RdfUtils.productsGraph());
		RdfUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg2(), languages.lg2(), model, RdfUtils.productsGraph());
		RdfUtils.addTripleDateTime(indicURI, DCTERMS.CREATED, indicator.getCreated(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleDateTime(indicURI, DCTERMS.MODIFIED, indicator.getUpdated(), model, RdfUtils.operationsGraph());

		addMulltiLangValues(model, indicURI, RdfUtils.productsGraph(), indicator.getAbstractLg1(), indicator.getAbstractLg2(), DCTERMS.ABSTRACT);
		addMulltiLangValues(model, indicURI, RdfUtils.productsGraph(), indicator.getHistoryNoteLg1(), indicator.getHistoryNoteLg2(), SKOS.HISTORY_NOTE);

		addOrganisationLinks(indicator.getContributors(), DCTERMS.CONTRIBUTOR, model, indicURI);
		addCreators(model, indicURI, indicator.getCreators());
		addOrganisationLinks(indicator.getPublishers(), DCTERMS.PUBLISHER, model, indicURI);
		
		String accPeriodicityUri = codeListService.getCodeUri(indicator.getAccrualPeriodicityList(), indicator.getAccrualPeriodicityCode());
		RdfUtils.addTripleUri(indicURI, DCTERMS.ACCRUAL_PERIODICITY, accPeriodicityUri, model, RdfUtils.productsGraph());

		addOneWayLink(model, indicURI, indicator.getSeeAlso(), RDFS.SEEALSO);
		addOneWayLink(model, indicURI, indicator.getWasGeneratedBy(), PROV.WAS_GENERATED_BY);

		List<OperationsLink> replaces = indicator.getReplaces();
		if (replaces != null) {
			for (OperationsLink replace : replaces) {
				String replaceUri = this.bauhausUriBuilder.getCompleteUriGestion(replace.getType(), replace.getId());
				addReplacesAndReplacedBy(model, RdfUtils.toURI(replaceUri), indicURI);
			}
		}		
		
		List<OperationsLink> isReplacedBys = indicator.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				String isReplUri = this.bauhausUriBuilder.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
				addReplacesAndReplacedBy(model, indicURI, RdfUtils.toURI(isReplUri));
			}
		}

		repositoryGestion.loadObjectWithReplaceLinks(indicURI, model);
	}

	public void validateIndicator(String id)  throws RmesException  {

		Indicator indicator = getIndicatorById(id, false);

		indicatorPublication.validate(indicator);
		indicatorPublication.publish(id);

		IRI indicatorURI = RdfUtils.objectIRI(ObjectType.INDICATOR, id);

		Model model = new LinkedHashModel();
		model.add(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.productsGraph());
		model.remove(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.productsGraph());
		model.remove(indicatorURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.productsGraph());
		logger.info("Validate indicator : {}" , indicatorURI);

		repositoryGestion.objectValidation(indicatorURI, model);
	}

	private void addOneWayLink(Model model, IRI indicURI, List<OperationsLink> links, IRI linkPredicate) {
		if (links != null) {
			for (OperationsLink oneLink : links) {
				String linkedObjectUri = this.bauhausUriBuilder.getCompleteUriGestion(oneLink.getType(), oneLink.getId());
				RdfUtils.addTripleUri(indicURI, linkPredicate ,linkedObjectUri, model, RdfUtils.productsGraph());
			}
		}
	}
	
	private void addReplacesAndReplacedBy(Model model, IRI previous, IRI next) {
		RdfUtils.addTripleUri(previous, DCTERMS.IS_REPLACED_BY ,next, model, RdfUtils.productsGraph());
		RdfUtils.addTripleUri(next, DCTERMS.REPLACES ,previous, model, RdfUtils.productsGraph());
	}

	void addCreators(Model model, IRI indicURI, List<String> creators) {
		addCreators(model, indicURI, creators, RdfUtils.productsGraph());
	}

	void addCreators(Model model, IRI indicURI, List<String> creators, Resource graph) {
		if (creators == null) {
			return;
		}
		for (String creatorIri : creators) {
			RdfUtils.addTripleUri(indicURI, DC.CREATOR, creatorIri, model, graph);
		}
	}

	void addOrganisationLinks(List<String> orgRefs, IRI predicate, Model model, IRI indicURI) throws RmesException {
		addOrganisationLinks(orgRefs, predicate, model, indicURI, RdfUtils.productsGraph());
	}

	void addOrganisationLinks(List<String> orgRefs, IRI predicate, Model model, IRI indicURI, Resource graph) throws RmesException {
		if (orgRefs == null) {
			return;
		}
		for (String orgRef : orgRefs) {
			if (orgRef != null && !orgRef.isEmpty()) {
				Optional<String> resolved = organisationLookup.resolve(orgRef);
				if (resolved.isPresent()) {
					RdfUtils.addTripleUri(indicURI, predicate, resolved.get(), model, graph);
				}
			}
		}
	}

	public String createID() throws RmesException {
		logger.info("Generate indicator id");
		JSONObject json = repositoryGestion.getResponseAsObject(operationIndicatorsQueries.lastID());
		logger.debug("JSON for indicator id : {}" , json);
		if (json.isEmpty()) {return "p1";}
		String id = json.getString(Constants.ID);
		if (id.equals(Constants.UNDEFINED)) {return "p1";}
		int idInt = Integer.parseInt(id.substring(1))+1;
		return "p" + idInt;
	}

	public boolean checkIfIndicatorExists(String id) throws RmesException {
		return repositoryGestion.getResponseAsBoolean(operationIndicatorsQueries.checkIfExists(id));
	}



}
