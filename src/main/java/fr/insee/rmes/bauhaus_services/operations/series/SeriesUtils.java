package fr.insee.rmes.bauhaus_services.operations.series;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
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
	FamOpeSerIndUtils famOpeSerIndUtils;

	@Autowired
	SeriesPublication seriesPublication;

	private static final Logger logger = LogManager.getLogger(SeriesUtils.class);

	/*READ*/

	public IdLabelTwoLangs getSeriesLabelById(String id) throws RmesException {
		return famOpeSerIndUtils.buildIdLabelTwoLangsFromJson(getSeriesJsonById(id));	
	}

	public Series getSeriesById(String id) throws RmesException {
		return buildSeriesFromJson(getSeriesJsonById(id));	
	}


	private Series buildSeriesFromJson(JSONObject seriesJson) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);		

		String id;
		if (seriesJson.has("id") && !seriesJson.getString("id").isEmpty()) {
			id= seriesJson.getString("id");} else {
				id= famOpeSerIndUtils.createId();}
		Series series = new Series();
		try {
			series = mapper.readValue(seriesJson.toString(), Series.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		if(series.getId().isEmpty()) {
			series.id = id;
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
		addSeriesCreators(id, series);
		addGeneratedWith(id, series);
		return series;
	}


	public String getSeriesForSearch(String stamp) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(SeriesQueries.getSeriesForSearch(stamp));
		JSONArray result = new JSONArray();
		for (int i = 0; i < resQuery.length(); i++) {
			JSONObject series = resQuery.getJSONObject(i);
			String idSeries = series.get(Constants.ID).toString();
			addSeriesCreators(idSeries, series);
			addOneTypeOfLink(idSeries, series, DCTERMS.CONTRIBUTOR, Constants.ORGANIZATIONS);
			addOneTypeOfLink(idSeries, series, INSEE.DATA_COLLECTOR, Constants.ORGANIZATIONS);
			addOneTypeOfLink(idSeries, series, DCTERMS.PUBLISHER, Constants.ORGANIZATIONS);
			famOpeSerIndUtils.fixOrganizationsNames(series);			
			result.put(series);
		}
		return QueryUtils.correctEmptyGroupConcat(result.toString());
	}	

	private void addSeriesOperations(String idSeries, JSONObject series) throws RmesException {
		JSONArray operations = repoGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put(Constants.OPERATIONS, operations);
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
		series.put(Constants.FAMILY, family);
	}

	private void addSeriesLinks(String idSeries, JSONObject series) throws RmesException {
		addOneTypeOfLink(idSeries, series, DCTERMS.REPLACES, Constants.OPERATIONS);
		addOneTypeOfLink(idSeries, series, DCTERMS.IS_REPLACED_BY, Constants.OPERATIONS);
		addOneTypeOfLink(idSeries, series, RDFS.SEEALSO, Constants.OPERATIONS);
		addOneTypeOfLink(idSeries, series, DCTERMS.CONTRIBUTOR, Constants.ORGANIZATIONS);
		addOneTypeOfLink(idSeries, series, INSEE.DATA_COLLECTOR, Constants.ORGANIZATIONS);
		addOneTypeOfLink(idSeries, series, DCTERMS.PUBLISHER, Constants.ORGANIZATIONS);
		famOpeSerIndUtils.fixOrganizationsNames(series);
	}


	/**
	 * Add to series the link of type "predicate".
	 * Links can be multiple
	 * @param id
	 * @param series
	 * @param predicate
	 * @throws RmesException
	 */
	private void addOneTypeOfLink(String id, JSONObject series, IRI predicate, String resultType) throws RmesException {

		JSONArray links = repoGestion.getResponseAsArray(SeriesQueries.seriesLinks(id, predicate, resultType));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
		}
		series.put(predicate.getLocalName(), links);
	}

	private void addSeriesCreators(String id, JSONObject series) throws RmesException {
		JSONArray creators = repoGestion.getResponseAsJSONList(SeriesQueries.getCreatorsById(id));
		series.put(Constants.CREATORS, creators);
	}
	
	public JSONArray getSeriesCreators(String id) throws RmesException {
		return  repoGestion.getResponseAsJSONList(SeriesQueries.getCreatorsById(id));
	}
	
	public JSONArray getSeriesCreators(IRI iri) throws RmesException {
		return repoGestion.getResponseAsJSONList(SeriesQueries.getCreatorsBySeriesUri(((SimpleIRI)iri).toString()));
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

		List<String> creators=series.getCreators();
		if (creators!=null) {
			for (String creator : creators) {
				RdfUtils.addTripleString(seriesURI, DC.CREATOR, creator, model, RdfUtils.operationsGraph());
			}
		}

		//Organismes responsables
		addOperationLinksOrganization(series.getPublishers(),DCTERMS.PUBLISHER, model, seriesURI);

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

		List<OperationsLink> replaces = series.getReplaces();
		if (replaces != null) {
			for (OperationsLink replace : replaces) {
				if(!replace.isEmpty()) {
					String replUri = ObjectType.getCompleteUriGestion(replace.getType(), replace.getId());
					addReplacesAndReplacedBy(model,  RdfUtils.toURI(replUri), seriesURI);
				}
			}
		}

		List<OperationsLink> isReplacedBys = series.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				if(!isRepl.isEmpty()) {
					String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
					addReplacesAndReplacedBy(model, seriesURI, RdfUtils.toURI(isReplUri));
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
	
	private void addReplacesAndReplacedBy(Model model, IRI previous, IRI next) {
		RdfUtils.addTripleUri(previous, DCTERMS.IS_REPLACED_BY ,next, model, RdfUtils.operationsGraph());
		RdfUtils.addTripleUri(next, DCTERMS.REPLACES ,previous, model, RdfUtils.operationsGraph());
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
					RdfUtils.addTripleUri(seriesURI, predicate,
							//			d.getId(),
							organizationsService.getOrganizationUriById(d.getId()),
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
		Series series = buildSeriesFromJson(new JSONObject(body));
		checkSimsWithOperations(series);

		// Tester l'existence de la famille
		String idFamily = series.getFamily().getId();
		if (!famOpeSerIndUtils.checkIfObjectExists(ObjectType.FAMILY, idFamily)) {
			throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_FAMILY, "Unknown family: ", idFamily);
		}

		IRI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY, idFamily);
		createRdfSeries(series, familyURI, ValidationStatus.UNPUBLISHED);
		logger.info("Create series : {} - {}", series.getId(), series.getPrefLabelLg1());

		return series.getId();

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

		String status = famOpeSerIndUtils.getValidationStatus(id);
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
			idSims = series.getString(Constants.ID_SIMS);
		} catch (JSONException e) {
			return false;
		}
		return idSims != null && !idSims.isEmpty();
	}

	public boolean hasOperations(String seriesId) throws RmesException {
		JSONObject series = getSeriesJsonById(seriesId);
		JSONArray operations;
		try {
			operations = series.getJSONArray(Constants.OPERATIONS);
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
