package fr.insee.rmes.bauhaus_services.operations.series;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.famopeser_utils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.operations.series.SeriesQueries;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Component
public class SeriesUtils extends RdfService {

	@Autowired
	CodeListService codeListService;

	@Autowired
	OrganizationsService organizationsService;

	@Autowired
	FamOpeSerUtils famOpeSerUtils;

	@Autowired
	SeriesPublication seriesPublication;

	static final Logger logger = LogManager.getLogger(SeriesUtils.class);

	/*READ*/

	public IdLabelTwoLangs getSeriesLabelById(String id) throws RmesException {
		return famOpeSerUtils.buildIdLabelTwoLangsFromJson(getSeriesJsonById(id));	
	}

	public Series getSeriesById(String id) throws RmesException {
		return buildSeriesFromJson(getSeriesJsonById(id));	
	}

	private Series buildSeriesFromJson(JSONObject seriesJson) throws JSONException, RmesException {
		Series series=new Series();
		series.setId(seriesJson.getString("id"));
		if(seriesJson.has("prefLabelLg1")) {
			series.setPrefLabelLg1(seriesJson.getString("prefLabelLg1"));
		}
		if(seriesJson.has("prefLabelLg2")) {
			series.setPrefLabelLg2(seriesJson.getString("prefLabelLg2"));
		}
		if(seriesJson.has("altLabelLg1")) {
			series.setAltLabelLg1(seriesJson.getString("altLabelLg1"));
		}
		if(seriesJson.has("altLabelLg2")) {
			series.setAltLabelLg2(seriesJson.getString("altLabelLg2"));
		}
		if(seriesJson.has("abstractLg1")) {
			series.setAbstractLg1(seriesJson.getString("abstractLg1"));
		}
		if(seriesJson.has("abstractLg2")) {
			series.setAbstractLg2(seriesJson.getString("abstractLg2"));
		}
		if(seriesJson.has("historyNoteLg1")) {
			series.setHistoryNoteLg1(seriesJson.getString("historyNoteLg1"));
		}
		if(seriesJson.has("historyNoteLg2")) {
			series.setHistoryNoteLg2(seriesJson.getString("historyNoteLg2"));
		}
		if(seriesJson.has("typeCode")) {
			series.setTypeCode(seriesJson.getString("typeCode"));
		}
		if(seriesJson.has("typeList")) {
			series.setTypeList(seriesJson.getString("typeList"));
		}
		if(seriesJson.has("accrualPeriodicityCode")) {
			series.setAccrualPeriodicityCode(seriesJson.getString("accrualPeriodicityCode"));
		}
		if(seriesJson.has("accrualPeriodicityList")) {
			series.setAccrualPeriodicityList(seriesJson.getString("accrualPeriodicityList"));
		}
		if(seriesJson.has("gestionnaires")) {
			series.setGestionnaires(famOpeSerUtils.buildStringListFromJson(
					seriesJson.getJSONArray("gestionnaires")));
		}
		if(seriesJson.has("idSims")) {
			series.setIdSims(seriesJson.getString("idSims"));
		}
		if(seriesJson.has("family")) {
			series.setFamily(famOpeSerUtils.buildIdLabelTwoLangsFromJson(seriesJson.getJSONObject("family")));
		}

		if(seriesJson.has("contributors")) {
			List<OperationsLink> contributors = new ArrayList<OperationsLink>();
			List<Object> objects = famOpeSerUtils.buildObjectListFromJson(
					seriesJson.getJSONArray("contributors"),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						contributors.add((OperationsLink) o);		
					}
					series.setContributors(contributors);
		}
		if(seriesJson.has("seeAlso")) {
			List<OperationsLink> seeAlsoes = new ArrayList<OperationsLink>();
			List<Object> objects = famOpeSerUtils.buildObjectListFromJson(
					seriesJson.getJSONArray("seeAlso"),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						seeAlsoes.add((OperationsLink) o);		
					}
					series.setSeeAlso(seeAlsoes);
		}
		if(seriesJson.has("replaces")) {
			List<OperationsLink> replacesList = new ArrayList<OperationsLink>();
			List<Object> objects = famOpeSerUtils.buildObjectListFromJson(
					seriesJson.getJSONArray("replaces"),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						replacesList.add((OperationsLink) o);		
					}
					series.setReplaces(replacesList);
		}
		if(seriesJson.has("isReplacedBy")) {
			List<OperationsLink> isReplacedByList = new ArrayList<OperationsLink>();
			List<Object> objects = famOpeSerUtils.buildObjectListFromJson(
					seriesJson.getJSONArray("isReplacedBy"),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						isReplacedByList.add((OperationsLink) o);		
					}
					series.setIsReplacedBy(isReplacedByList);
		}
		if(seriesJson.has("dataCollectors")) {
			List<OperationsLink> dataCollectors = new ArrayList<OperationsLink>();
			List<Object> objects = famOpeSerUtils.buildObjectListFromJson(
					seriesJson.getJSONArray("dataCollectors"),
					OperationsLink.getClassOperationsLink());
					for (Object o:objects){
						dataCollectors.add((OperationsLink) o);		
					}
					series.setDataCollectors(dataCollectors);
		}
		return series;
	}

	public JSONObject getSeriesJsonById(String id) throws RmesException {
		JSONObject series = repoGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		// check that the series exist
		if (JSONUtils.isEmpty(series)) {
			throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID, "Series not found",
					"The series " + id + " cannot be found.");
		}
		XhtmlToMarkdownUtils.convertJSONObject(series);
		series.put(Constants.ID, id);
		addSeriesOperations(id, series);
		addSeriesFamily(id, series);
		addSeriesLinks(id, series);
		addSeriesGestionnaires(id, series);
		addSeriesCreators(id, series);

		addGeneratedWith(id, series);
		return series;
	}

	public String getSeriesForSearch() throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(SeriesQueries.getSeriesForSearch());
		JSONArray result = new JSONArray();
		for (int i = 0; i < resQuery.length(); i++) {
			JSONObject series = resQuery.getJSONObject(i);
			addOneOrganizationLink(series.get(Constants.ID).toString(), series, INSEE.DATA_COLLECTOR);
			addOneOrganizationLink(series.get(Constants.ID).toString(), series, DCTERMS.CREATOR);
			result.put(series);
		}
		return QueryUtils.correctEmptyGroupConcat(result.toString());
	}

	private void addSeriesOperations(String idSeries, JSONObject series) throws RmesException {
		JSONArray operations = repoGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}

	private void addGeneratedWith(String idSeries, JSONObject series) throws RmesException {
		JSONArray generated = repoGestion.getResponseAsArray(SeriesQueries.getGeneratedWith(idSeries));
		if (generated.length() != 0) {
			generated = QueryUtils.transformRdfTypeInString(generated);
			series.put("generate", generated);
		}
	}

	private void addSeriesFamily(String idSeries, JSONObject series) throws RmesException {
		JSONObject family = repoGestion.getResponseAsObject(SeriesQueries.getFamily(idSeries));
		series.put("family", family);
	}

	private void addSeriesLinks(String idSeries, JSONObject series) throws RmesException {
		addOneTypeOfLink(idSeries, series, DCTERMS.REPLACES);
		addOneTypeOfLink(idSeries, series, DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idSeries, series, RDFS.SEEALSO);
		addOneOrganizationLink(idSeries, series, DCTERMS.CONTRIBUTOR);
		addOneOrganizationLink(idSeries, series, INSEE.DATA_COLLECTOR);
	}

	private void addOneTypeOfLink(String id, JSONObject series, IRI predicate) throws RmesException {
		JSONArray links = repoGestion.getResponseAsArray(SeriesQueries.seriesLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			series.put(predicate.getLocalName(), links);
		}
	}

	private void addOneOrganizationLink(String id, JSONObject series, IRI predicate) throws RmesException {
		JSONArray organizations = repoGestion.getResponseAsArray(SeriesQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			for (int i = 0; i < organizations.length(); i++) {
				JSONObject orga = organizations.getJSONObject(i);
				orga.put("type", ObjectType.ORGANIZATION.getLabelType());
			}
			series.put(predicate.getLocalName(), organizations);
		}
	}

	private void addSeriesGestionnaires(String id, JSONObject series) throws RmesException {
		JSONArray gestionnaires = repoGestion.getResponseAsJSONList(SeriesQueries.getGestionnaires(id));
		series.put("gestionnaires", gestionnaires);
	}

	private void addSeriesCreators(String id, JSONObject series) throws RmesException {
		JSONArray creators = repoGestion.getResponseAsJSONList(SeriesQueries.getCreators(id));
		if (creators.length()==1) {
			series.put("creator", creators.get(0));
		}else {
			series.put("creator", creators);
		}
	}

	/*WRITE*/

	/*
	 * CREATE OR UPDATE
	 */
	private void createRdfSeries(Series series, IRI familyURI, ValidationStatus newStatus) throws RmesException {

		Model model = new LinkedHashModel();
		IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES,series.getId());
		/*Const*/
		model.add(seriesURI, RDF.TYPE, INSEE.SERIES, RdfUtils.operationsGraph());
		/*Required*/
		model.add(seriesURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(series.getPrefLabelLg1(), Config.LG1), RdfUtils.operationsGraph());
		model.add(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.operationsGraph());
		/*Optional*/
		RdfUtils.addTripleString(seriesURI, SKOS.PREF_LABEL, series.getPrefLabelLg2(), Config.LG2, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg1(), Config.LG1, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg2(), Config.LG2, model, RdfUtils.operationsGraph());

		RdfUtils.addTripleStringMdToXhtml(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg1(), Config.LG1, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleStringMdToXhtml(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg2(), Config.LG2, model, RdfUtils.operationsGraph());

		RdfUtils.addTripleStringMdToXhtml(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg1(), Config.LG1, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleStringMdToXhtml(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg2(), Config.LG2, model, RdfUtils.operationsGraph());

		List<String> creator=series.getCreator();
		if (creator!= null) {
			for(String creat : creator) {
				RdfUtils.addTripleUri(seriesURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(creat), model, RdfUtils.operationsGraph());
			}
		}

		List<String> gestionnaires=series.getGestionnaires();
		if (gestionnaires!=null) {
			for (String gestionnaire : gestionnaires) {
				RdfUtils.addTripleString(seriesURI, INSEE.GESTIONNAIRE, gestionnaire, model, RdfUtils.operationsGraph());
			}
		}

		//partenaires
		addOperationLinksOrganization(series.getContributors(),DCTERMS.CONTRIBUTOR, model, seriesURI);

		//Data_collector
		addOperationLinksOrganization(series.getDataCollectors(),INSEE.DATA_COLLECTOR, model, seriesURI);

		//Type
		addCodeList(series.getTypeList(), series.getTypeCode(), DCTERMS.TYPE, model, seriesURI);		
		//PERIODICITY
		addCodeList(series.getAccrualPeriodicityList(), series.getAccrualPeriodicityCode(), DCTERMS.ACCRUAL_PERIODICITY, model, seriesURI);		

		addOperationLinks(series.getSeeAlso(), RDFS.SEEALSO, model, seriesURI); 
		addOperationLinks(series.getReplaces(), DCTERMS.REPLACES, model, seriesURI); 


		List<OperationsLink> isReplacedBys = series.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				if(!isRepl.isEmpty()) {
					String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
					RdfUtils.addTripleUri(seriesURI, DCTERMS.IS_REPLACED_BY ,isReplUri, model, RdfUtils.operationsGraph());
					RdfUtils.addTripleUri(RdfUtils.toURI(isReplUri), DCTERMS.REPLACES ,seriesURI, model, RdfUtils.operationsGraph());
				}
			}
		}

		if (familyURI != null) {
			//case CREATION : link series to family
			RdfUtils.addTripleUri(seriesURI, DCTERMS.IS_PART_OF, familyURI, model, RdfUtils.operationsGraph());
			RdfUtils.addTripleUri(familyURI, DCTERMS.HAS_PART, seriesURI, model, RdfUtils.operationsGraph());
		}

		repoGestion.keepHierarchicalOperationLinks(seriesURI,model);

		repoGestion.loadObjectWithReplaceLinks(seriesURI, model);
	}

	private void addOperationLinks(List<OperationsLink> links, IRI predicate, Model model, IRI seriesURI) {
		if (links != null) {
			for (OperationsLink link : links) {
				if (!link.isEmpty()) {
					String linkUri = ObjectType.getCompleteUriGestion(link.getType(), link.getId());
					RdfUtils.addTripleUri(seriesURI, predicate, linkUri, model, RdfUtils.operationsGraph());
				}
			}
		}
	}

	private void addCodeList(String list, String code, IRI predicate, Model model, IRI seriesURI) throws RmesException {
		if (!StringUtils.isEmpty(list) && !StringUtils.isEmpty(code)) {
			String uri = codeListService.getCodeUri(list, code);
			RdfUtils.addTripleUri(seriesURI, predicate, uri, model, RdfUtils.operationsGraph());
		}
	}

	private void addOperationLinksOrganization(List<OperationsLink> data, IRI predicate, Model model, IRI seriesURI)
			throws RmesException {
		if (data != null) {
			for (OperationsLink d : data) {
				if (!d.isEmpty()) {
					RdfUtils.addTripleUri(seriesURI, predicate, organizationsService.getOrganizationUriById(d.getId()),
							model, RdfUtils.operationsGraph());
				}
			}
		}
	}

	public String createSeries(String body) throws RmesException {
		if (!stampsRestrictionsService.canCreateSeries()) {
			throw new RmesUnauthorizedException(ErrorCodes.SERIES_CREATION_RIGHTS_DENIED,
					"Only an admin can create a new series.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		String id = famOpeSerUtils.createId();
		Series series = new Series();
		try {
			series = mapper.readValue(body, Series.class);
			series.id = id;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		checkSimsWithOperations(series);

		// Tester l'existence de la famille
		String idFamily = series.getFamily().getId();
		if (!famOpeSerUtils.checkIfObjectExists(ObjectType.FAMILY, idFamily)) {
			throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_FAMILY, "Unknown family: ", idFamily);
		}

		IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, idFamily);
		createRdfSeries(series, familyURI, ValidationStatus.UNPUBLISHED);
		logger.info("Create series : {} - {}", id, series.getPrefLabelLg1());

		return id;

	}

	/**
	 * Une série ne peut avoir un Sims que si elle n'a pas d'opération
	 * @param series
	 * @throws RmesNotAcceptableException
	 */
	private void checkSimsWithOperations(Series series) throws RmesNotAcceptableException {
		if (series.getIdSims() != null && !series.getIdSims().isEmpty() && series.getOperations() != null
				&& !series.getOperations().isEmpty()) {
			throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,
					"A series cannot have both a Sims and Operation(s)",
					series.getPrefLabelLg1() + " " + series.getPrefLabelLg2());
		}
	}

	/* Update Series */
	public void setSeries(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		Series series = new Series();
		try {
			series = mapper.readerForUpdating(series).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Can't parse series", e.getMessage());
		}

		IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, id);
		if (!stampsRestrictionsService.canModifySeries(seriesURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.SERIES_MODIFICATION_RIGHTS_DENIED,
					"Only authorized users can modify series.");
		}

		checkSimsWithOperations(series);

		String status = famOpeSerUtils.getValidationStatus(id);
		if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfSeries(series, null, ValidationStatus.UNPUBLISHED);
		} else {
			createRdfSeries(series, null, ValidationStatus.MODIFIED);
		}
		logger.info("Update series : {} - {}", series.getId(), series.getPrefLabelLg1());
	}

	public boolean hasSims(String seriesId) throws RmesException {
		JSONObject series = getSeriesJsonById(seriesId);
		String idSims;
		try {
			idSims = series.getString("idSims");
		} catch (JSONException e) {
			return false;
		}
		return idSims != null && !idSims.isEmpty();
	}

	public boolean hasOperations(String seriesId) throws RmesException {
		JSONObject series = getSeriesJsonById(seriesId);
		JSONArray operations;
		try {
			operations = series.getJSONArray("operations");
		} catch (JSONException e) {
			return false;
		}
		return operations != null && operations.length() > 0;
	}

	public String setSeriesValidation(String id) throws RmesException {
		Model model = new LinkedHashModel();
		seriesPublication.publishSeries(id);

		IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, id);
		if (!stampsRestrictionsService.canValidateSeries(seriesURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.SERIES_VALIDATION_RIGHTS_DENIED,
					"Only authorized users can publish series.");
		}

		model.add(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED),
				RdfUtils.operationsGraph());
		model.remove(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
				RdfUtils.operationsGraph());
		model.remove(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED),
				RdfUtils.operationsGraph());
		logger.info("Validate series : {}", seriesURI);

		repoGestion.objectValidation(seriesURI, model);

		return id;
	}

}
