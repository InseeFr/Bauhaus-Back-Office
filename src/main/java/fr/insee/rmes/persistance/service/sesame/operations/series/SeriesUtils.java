package fr.insee.rmes.persistance.service.sesame.operations.series;

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

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.OrganizationsService;
import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ValidationStatus;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Component
public class SeriesUtils {

	@Autowired
	CodeListService codeListService;

	@Autowired
	OrganizationsService organizationsService;

	@Autowired
	StampsRestrictionsService stampsRestrictionsService;

	final static Logger logger = LogManager.getLogger(SeriesUtils.class);

	/*READ*/

	public JSONObject getSeriesById(String id) throws RmesException{
		JSONObject series = RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		//check that the series exist
		if (JSONUtils.isEmpty(series)) {
			throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID,"Series not found","The series "+id+" cannot be found.");
		}		
		XhtmlToMarkdownUtils.convertJSONObject(series);
		series.put("id", id);
		addSeriesOperations(id, series);
		addSeriesFamily(id,series);
		addSeriesLinks(id, series);
		addGeneratedWith(id, series);
		return series;
	}

	public String getSeriesForSearch() throws RmesException {
		JSONArray resQuery = RepositoryGestion.getResponseAsArray(SeriesQueries.getSeriesForSearch());
		JSONArray result = new JSONArray();
		for (int i = 0; i < resQuery.length(); i++) {
			JSONObject series = resQuery.getJSONObject(i);
			addOneOrganizationLink(series.get("id").toString(),series, INSEE.DATA_COLLECTOR);
			result.put(series);
		}
		return QueryUtils.correctEmptyGroupConcat(result.toString());
	}

	private void addSeriesOperations(String idSeries, JSONObject series) throws RmesException {
		JSONArray operations = RepositoryGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}

	private void addGeneratedWith(String idSeries, JSONObject series) throws RmesException {
		JSONArray generated = RepositoryGestion.getResponseAsArray(SeriesQueries.getGeneratedWith(idSeries));
		if (generated.length() != 0) {
			generated = QueryUtils.transformRdfTypeInString(generated);
			series.put("generate", generated);
		}
	}

	private void addSeriesFamily(String idSeries, JSONObject series) throws RmesException {
		JSONObject family = RepositoryGestion.getResponseAsObject(SeriesQueries.getFamily(idSeries));
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
		JSONArray links = RepositoryGestion.getResponseAsArray(SeriesQueries.seriesLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			series.put(predicate.getLocalName(), links);
		}
	}

	private void addOneOrganizationLink(String id, JSONObject series, URI predicate) throws RmesException {
		JSONArray organizations = RepositoryGestion.getResponseAsArray(SeriesQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			for (int i = 0; i < organizations.length(); i++) {
				JSONObject orga = organizations.getJSONObject(i);
				orga.put("type", ObjectType.ORGANIZATION.getLabelType());
			}
			series.put(predicate.getLocalName(), organizations);
		}
	}


	/*WRITE*/


	public String createSeries(String body) throws RmesException {
		if(!stampsRestrictionsService.canCreateSeries()) throw new RmesUnauthorizedException(ErrorCodes.SERIES_CREATION_RIGHTS_DENIED, "Only an admin can create a new series.");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Series series = new Series();
		String id = series.getId();
		try {
			series = mapper.readValue(body,Series.class);
			series.id = id;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		//Une série ne peut avoir un Sims que si elle n'a pas d'opération
		if ((series.getIdSims()!=null) & (series.getOperations()!=null) ) {
			if (!series.getIdSims().isEmpty() & series.getOperations().size()>0) {
				throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,"A series cannot have both a Sims and Operation(s)", 
						series.getPrefLabelLg1()+" "+series.getPrefLabelLg2());
			}
		}
		// Tester l'existence de la famille
		String idFamily= series.getFamily().getId();
		if (! FamOpeSerUtils.checkIfObjectExists(ObjectType.FAMILY,idFamily)) throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_FAMILY,"Unknown family: ",idFamily);

		URI familyURI = SesameUtils.objectIRI(ObjectType.FAMILY,idFamily);
		createRdfSeries(series, familyURI, ValidationStatus.UNPUBLISHED);
		logger.info("Create series : " + id + " - " + series.getPrefLabelLg1());

		return id;

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

		URI seriesURI = SesameUtils.objectIRI(ObjectType.SERIES,id);
		if(!stampsRestrictionsService.canModifySeries(seriesURI)) throw new RmesUnauthorizedException(ErrorCodes.SERIES_MODIFICATION_RIGHTS_DENIED, "Only authorized users can modify series.");

		//Une série ne peut avoir un Sims que si elle n'a pas d'opération
		if (series.getIdSims() != null & series.getOperations()!= null )
		if (!series.getIdSims().isEmpty() & series.getOperations().size()>0) {
			throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,"A series cannot have both a Sims and Operation(s)", 
					series.getPrefLabelLg1()+" "+series.getPrefLabelLg2());
		}
		
		String status=FamOpeSerUtils.getValidationStatus(id);
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) | status.equals("UNDEFINED")) {
			createRdfSeries(series,null,ValidationStatus.UNPUBLISHED);
		}
		else 	createRdfSeries(series,null,ValidationStatus.MODIFIED);
		logger.info("Update series : " + series.getId() + " - " + series.getPrefLabelLg1());
	}

	/*
	 * CREATE OR UPDATE
	 */
	private void createRdfSeries(Series series, URI familyURI, ValidationStatus newStatus) throws RmesException {

		Model model = new LinkedHashModel();
		URI seriesURI = SesameUtils.objectIRI(ObjectType.SERIES,series.getId());
		/*Const*/
		model.add(seriesURI, RDF.TYPE, INSEE.SERIES, SesameUtils.operationsGraph());
		/*Required*/
		model.add(seriesURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(series.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
		model.add(seriesURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(newStatus.toString()), SesameUtils.operationsGraph());
		/*Optional*/
		SesameUtils.addTripleString(seriesURI, SKOS.PREF_LABEL, series.getPrefLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		SesameUtils.addTripleStringMdToXhtml(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleStringMdToXhtml(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		SesameUtils.addTripleStringMdToXhtml(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleStringMdToXhtml(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		String creator=series.getCreator();
		if (!StringUtils.isEmpty(creator)) {
			SesameUtils.addTripleUri(seriesURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(creator), model, SesameUtils.operationsGraph());
		}
		String gestionnaire=series.getGestionnaire();
		if (!StringUtils.isEmpty(gestionnaire)) {
			SesameUtils.addTripleUri(seriesURI, INSEE.GESTIONNAIRE, organizationsService.getOrganizationUriById(gestionnaire), model, SesameUtils.operationsGraph());
		}

		//partenaires
		List<OperationsLink> contributors = series.getContributor();
		if (contributors != null){
			for (OperationsLink contributor : contributors) {
				if(!contributor.isEmpty()) {
					SesameUtils.addTripleUri(seriesURI, DCTERMS.CONTRIBUTOR,organizationsService.getOrganizationUriById(contributor.getId()),model, SesameUtils.operationsGraph());		
				}
			}
		}

		List<OperationsLink> dataCollectors = series.getDataCollector();
		if (dataCollectors != null) {
			for (OperationsLink dataCollector : dataCollectors) {
				if(!dataCollector.isEmpty()) {
					SesameUtils.addTripleUri(seriesURI, INSEE.DATA_COLLECTOR,organizationsService.getOrganizationUriById(dataCollector.getId()),model, SesameUtils.operationsGraph());
				}		
			}
		}

		String typeList=series.getTypeList();
		String typeCode=series.getTypeCode();
		if (!StringUtils.isEmpty(typeList) & !StringUtils.isEmpty(typeCode)) {
			String typeUri = codeListService.getCodeUri(typeList, typeCode);
			SesameUtils.addTripleUri(seriesURI, DCTERMS.TYPE, typeUri,model, SesameUtils.operationsGraph());
		}

		String accrualPeriodicityList=series.getAccrualPeriodicityList();
		String accrualPeriodicityCode=series.getAccrualPeriodicityCode();
		if (!StringUtils.isEmpty(accrualPeriodicityList) & !StringUtils.isEmpty(accrualPeriodicityCode)) {
			String accPeriodicityUri = codeListService.getCodeUri(accrualPeriodicityList, accrualPeriodicityCode);
			SesameUtils.addTripleUri(seriesURI, DCTERMS.ACCRUAL_PERIODICITY, accPeriodicityUri, model, SesameUtils.operationsGraph());
		}

		List<OperationsLink> seeAlsos = series.getSeeAlso();
		if (seeAlsos != null) {
			for (OperationsLink seeAlso : seeAlsos) {
				if(!seeAlso.isEmpty()) {
					String seeAlsoUri = ObjectType.getCompleteUriGestion(seeAlso.getType(), seeAlso.getId());
					SesameUtils.addTripleUri(seriesURI, RDFS.SEEALSO ,seeAlsoUri, model, SesameUtils.operationsGraph());
				}
			}
		}

		List<OperationsLink> replaces = series.getReplaces();
		if (replaces != null) {
			for (OperationsLink repl : replaces) {
				if(!repl.isEmpty()) {
					String replUri = ObjectType.getCompleteUriGestion(repl.getType(), repl.getId());
					SesameUtils.addTripleUri(seriesURI, DCTERMS.REPLACES ,replUri, model, SesameUtils.operationsGraph());
				}
			}
		}

		List<OperationsLink> isReplacedBys = series.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				if(!isRepl.isEmpty()) {
					String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
					SesameUtils.addTripleUri(seriesURI, DCTERMS.IS_REPLACED_BY ,isReplUri, model, SesameUtils.operationsGraph());
					SesameUtils.addTripleUri(SesameUtils.toURI(isReplUri), DCTERMS.REPLACES ,seriesURI, model, SesameUtils.operationsGraph());
				}
			}
		}

		if (familyURI != null) {
			//case CREATION : link series to family
			SesameUtils.addTripleUri(seriesURI, DCTERMS.IS_PART_OF, familyURI, model, SesameUtils.operationsGraph());
			SesameUtils.addTripleUri(familyURI, DCTERMS.HAS_PART, seriesURI, model, SesameUtils.operationsGraph());
		}

		RepositoryGestion.keepHierarchicalOperationLinks(seriesURI,model);

		RepositoryGestion.loadObjectWithReplaceLinks(seriesURI, model);
	}

	public boolean hasSims(String seriesId) throws RmesException {
		JSONObject series;
		String idSims;
		series = getSeriesById(seriesId);
		try {	idSims=series.getString("idSims");} 
		catch (JSONException e) {
			return false;
		}
		if (idSims==null | idSims.isEmpty()) {
			return false;
		}
		return true;
	}

	public String setSeriesValidation(String id)  throws RmesUnauthorizedException, RmesException  {
		Model model = new LinkedHashModel();
		
		//TODO Check autorisation
			SeriesPublication.publishSeries(id);
		
			URI seriesURI = SesameUtils.objectIRI(ObjectType.SERIES, id);
			if(!stampsRestrictionsService.canValidateSeries(seriesURI)) throw new RmesUnauthorizedException(ErrorCodes.SERIES_VALIDATION_RIGHTS_DENIED, "Only authorized users can publish series.");

			model.add(seriesURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(ValidationStatus.VALIDATED), SesameUtils.operationsGraph());
			model.remove(seriesURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(ValidationStatus.UNPUBLISHED), SesameUtils.operationsGraph());
			model.remove(seriesURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(ValidationStatus.MODIFIED), SesameUtils.operationsGraph());
			logger.info("Validate series : " + seriesURI);

			RepositoryGestion.objectsValidation(seriesURI, model);
			
		return id;
	}

}


