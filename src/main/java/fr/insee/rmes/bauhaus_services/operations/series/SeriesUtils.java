package fr.insee.rmes.bauhaus_services.operations.series;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
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
public class SeriesUtils  extends RdfService {

	@Autowired
	CodeListService codeListService;

	@Autowired
	OrganizationsService organizationsService;
	
	@Autowired
	FamOpeSerUtils famOpeSerUtils;

	static final Logger logger = LogManager.getLogger(SeriesUtils.class);

	/*READ*/

	public JSONObject getSeriesById(String id) throws RmesException{
		JSONObject series = repoGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		//check that the series exist
		if (JSONUtils.isEmpty(series)) {
			throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID,"Series not found","The series "+id+" cannot be found.");
		}		
		XhtmlToMarkdownUtils.convertJSONObject(series);
		series.put(Constants.ID, id);
		addSeriesOperations(id, series);
		addSeriesFamily(id,series);
		addSeriesLinks(id, series);
		addSeriesGestionnaires(id, series);
		addGeneratedWith(id, series);
		return series;
	}

	public String getSeriesForSearch() throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(SeriesQueries.getSeriesForSearch());
		JSONArray result = new JSONArray();
		for (int i = 0; i < resQuery.length(); i++) {
			JSONObject series = resQuery.getJSONObject(i);
			addOneOrganizationLink(series.get(Constants.ID).toString(),series, INSEE.DATA_COLLECTOR);
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
		addOneTypeOfLink(idSeries,series,DCTERMS.REPLACES);
		addOneTypeOfLink(idSeries,series,DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idSeries,series,RDFS.SEEALSO);
		addOneOrganizationLink(idSeries,series, DCTERMS.CONTRIBUTOR);
		addOneOrganizationLink(idSeries,series, INSEE.DATA_COLLECTOR);
	}

	private void addOneTypeOfLink(String id, JSONObject series, URI predicate) throws RmesException {
		JSONArray links = repoGestion.getResponseAsArray(SeriesQueries.seriesLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			series.put(predicate.getLocalName(), links);
		}
	}

	private void addOneOrganizationLink(String id, JSONObject series, URI predicate) throws RmesException {
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
	
	/*WRITE*/

	/*
	 * CREATE OR UPDATE
	 */
	private void createRdfSeries(Series series, URI familyURI, ValidationStatus newStatus) throws RmesException {
	
		Model model = new LinkedHashModel();
		URI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES,series.getId());
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
	
		String creator=series.getCreator();
		if (!StringUtils.isEmpty(creator)) {
			RdfUtils.addTripleUri(seriesURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(creator), model, RdfUtils.operationsGraph());
		}
		
		List<String> gestionnaires=series.getGestionnaires();
		if (gestionnaires!=null) {
			for (String gestionnaire : gestionnaires) {
				RdfUtils.addTripleString(seriesURI, INSEE.GESTIONNAIRE, gestionnaire, model, RdfUtils.operationsGraph());
			}
		}
	
		//partenaires
		addOperationLinksOrganization(series.getContributor(),DCTERMS.CONTRIBUTOR, model, seriesURI);
		
		//Data_collector
		addOperationLinksOrganization(series.getDataCollector(),INSEE.DATA_COLLECTOR, model, seriesURI);
	
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
	
	private void addOperationLinks(List<OperationsLink> links, URI predicate, Model model, URI seriesURI) {
		if (links != null) {
			for (OperationsLink link : links) {
				if(!link.isEmpty()) {
					String linkUri = ObjectType.getCompleteUriGestion(link.getType(), link.getId());
					RdfUtils.addTripleUri(seriesURI, predicate ,linkUri, model, RdfUtils.operationsGraph());
				}
			}
		}
	}

	private void addCodeList(String list, String code, URI predicate, Model model, URI seriesURI) throws RmesException {
		if (!StringUtils.isEmpty(list) && !StringUtils.isEmpty(code)) {
			String uri = codeListService.getCodeUri(list, code);
			RdfUtils.addTripleUri(seriesURI, predicate, uri, model, RdfUtils.operationsGraph());
		}
	}

	private void addOperationLinksOrganization(List<OperationsLink> data, URI predicate, Model model, URI seriesURI) throws RmesException {
		if (data != null) {
			for (OperationsLink d : data) {
				if(!d.isEmpty()) {
					RdfUtils.addTripleUri(seriesURI, predicate,organizationsService.getOrganizationUriById(d.getId()),model, RdfUtils.operationsGraph());
				}		
			}
		}
	}

	public String createSeries(String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateSeries()) {
			throw new RmesUnauthorizedException(ErrorCodes.SERIES_CREATION_RIGHTS_DENIED, 
					"Only an admin can create a new series.");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String id = famOpeSerUtils.createId();
		Series series = new Series(id);
		try {
			series = mapper.readValue(body,Series.class);
			series.id = id;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		checkSimsWithOperations(series);
		
		// Tester l'existence de la famille
		String idFamily= series.getFamily().getId();
		if (! famOpeSerUtils.checkIfObjectExists(ObjectType.FAMILY,idFamily)) {
			throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_FAMILY,"Unknown family: ",idFamily);
		}

		URI familyURI = RdfUtils.objectIRI(ObjectType.FAMILY,idFamily);
		createRdfSeries(series, familyURI, ValidationStatus.UNPUBLISHED);
		logger.info("Create series : {} - {}",id, series.getPrefLabelLg1());

		return id;

	}

	/**
	 * Une série ne peut avoir un Sims que si elle n'a pas d'opération
	 * @param series
	 * @throws RmesNotAcceptableException
	 */
	private void checkSimsWithOperations(Series series) throws RmesNotAcceptableException {
		if (series.getIdSims()!=null && !series.getIdSims().isEmpty() && series.getOperations()!=null  && !series.getOperations().isEmpty()) {
				throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,"A series cannot have both a Sims and Operation(s)", 
						series.getPrefLabelLg1()+" "+series.getPrefLabelLg2());
		}
	}

	/* Update Series */
	public void setSeries(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Series series = new Series(id);
		try {
			series = mapper.readerForUpdating(series).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		URI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES,id);
		if(!stampsRestrictionsService.canModifySeries(seriesURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.SERIES_MODIFICATION_RIGHTS_DENIED, "Only authorized users can modify series.");
		}

		checkSimsWithOperations(series);

		String status=famOpeSerUtils.getValidationStatus(id);
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfSeries(series,null,ValidationStatus.UNPUBLISHED);
		} else {
			createRdfSeries(series,null,ValidationStatus.MODIFIED);
		}
		logger.info("Update series : {} - {}", series.getId(), series.getPrefLabelLg1());
	}

	public boolean hasSims(String seriesId) throws RmesException {
		JSONObject series = getSeriesById(seriesId); 
		String idSims;
		try {	
			idSims=series.getString("idSims"); 
		} catch (JSONException e) {
			return false;
		}
		return idSims!=null && !idSims.isEmpty();
	}

	public boolean hasOperations(String seriesId) throws RmesException {
		JSONObject series= getSeriesById(seriesId);
		JSONArray operations;
		try {	
			operations=series.getJSONArray("operations");
		} catch (JSONException e) {
			return false;
		}
		return operations!=null && operations.length()>0;
	}


	public String setSeriesValidation(String id)  throws RmesException  {
		Model model = new LinkedHashModel();

		SeriesPublication.publishSeries(id);

		URI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES, id);
		if(!stampsRestrictionsService.canValidateSeries(seriesURI)) {
			throw new RmesUnauthorizedException(ErrorCodes.SERIES_VALIDATION_RIGHTS_DENIED, 
					"Only authorized users can publish series.");
		}

		model.add(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.operationsGraph());
		model.remove(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.operationsGraph());
		model.remove(seriesURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.operationsGraph());
		logger.info("Validate series : {}", seriesURI);

		RepositoryGestion.objectValidation(seriesURI, model);

		return id;
	}

}


