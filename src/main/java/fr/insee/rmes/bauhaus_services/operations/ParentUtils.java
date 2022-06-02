package fr.insee.rmes.bauhaus_services.operations;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.persistance.sparql_queries.operations.ParentQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils.FamOpeSerQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;

@Component
public class ParentUtils extends RdfService{

	
	static final Logger logger = LogManager.getLogger(ParentUtils.class);

	
	public String getDocumentationOwnersByIdSims(String idSims) throws RmesException {
		logger.info("Search Sims Owners' Stamps");
		String stamps = null;
		JSONObject target = repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));		
		if (target != null) {
			String idOperation = target.getString(Constants.ID_OPERATION);
			String idSerie = target.getString(Constants.ID_SERIES);
			String idIndicator = target.getString(Constants.ID_INDICATOR);

			if (idOperation != null && !idOperation.isEmpty()) {
				IRI seriesUri = getSeriesUriByOperationId(idOperation);
				stamps = getSeriesCreators(seriesUri).toString();
			} else if (idSerie != null && !idSerie.isEmpty()) {
				stamps = getSeriesCreators(idSerie).toString();
			} else if (idIndicator != null && !idIndicator.isEmpty()) {
				stamps = getIndicatorCreators(idIndicator).toString();
			} else {
				throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Documentation has no target",
						"Check your documentation creation");
			}
		}
		return stamps;
	}
	
	public IRI getSeriesUriByOperationId(String idOperation) throws RmesException{
		JSONObject series = repoGestion.getResponseAsObject(OperationsQueries.seriesQuery(idOperation));
		if (series != null && series.has(Constants.ID))		
			return RdfUtils.objectIRI(ObjectType.SERIES, series.getString(Constants.ID));
		return null;
	}
	

	public boolean checkIfSeriesHasSims(String uriSeries) throws RmesException {
		return repoGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasSims(uriSeries));
	}
	
	public void checkIfParentIsASeriesWithOperations(String idParent) throws RmesException {
		String uriParent = RdfUtils.toString(RdfUtils.objectIRI(ObjectType.SERIES, idParent));
		if (checkIfParentExists(uriParent) && checkIfSeriesHasOperation(uriParent)) throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,
				"Cannot create Sims for a series which already has operations", idParent);
	}
	
	public boolean checkIfParentExists(String uriParent) throws RmesException {
		return repoGestion.getResponseAsBoolean(ParentQueries.checkIfExists(uriParent));
	}
	
	public boolean checkIfSeriesHasOperation(String uriParent) throws RmesException {
		return repoGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasOperation(uriParent));
	}
	
	
	public String getValidationStatus(String targetId) throws RmesException {
		String status = getFamOpSerValidationStatus(targetId);
		if (status.equals(Constants.UNDEFINED)) {
			status = getIndicatorsValidationStatus(targetId);
		}
		return status;
	}
	
	public String getIndicatorsValidationStatus(String id) throws RmesException{
		try {
			return repoGestion.getResponseAsObject(IndicatorsQueries.getPublicationState(id)).getString("state"); 
		}
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}
	
	public String getFamOpSerValidationStatus(String id) throws RmesException {
		try {		
			return repoGestion.getResponseAsObject(FamOpeSerQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}
	
	
	public JSONArray getIndicatorCreators(String id) throws RmesException {
		return  repoGestion.getResponseAsJSONList(IndicatorsQueries.getCreatorsById(id));
	}
	
	
	public JSONArray getSeriesCreators(String id) throws RmesException {
		return  repoGestion.getResponseAsJSONList(OpSeriesQueries.getCreatorsById(id));
	}
	
	public JSONArray getSeriesCreators(IRI iri) throws RmesException {
		return repoGestion.getResponseAsJSONList(OpSeriesQueries.getCreatorsBySeriesUri(RdfUtils.toString(iri)));
	}
	
	public String[] getDocumentationTargetTypeAndId(String idSims) throws RmesException {
		logger.info("Search Sims Target Type and id");

		JSONObject existingIdTarget =  repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));
		String idDatabase = null;
		String targetType = null;
		if (existingIdTarget != null ) {
			idDatabase = (String) existingIdTarget.get(Constants.ID_OPERATION);

			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get(Constants.ID_SERIES);

				if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
					idDatabase = (String) existingIdTarget.get(Constants.ID_INDICATOR);
					targetType = Constants.INDICATOR_UP;
				} else {
					targetType = Constants.SERIES_UP;
				}
			} else {
				targetType = Constants.OPERATION_UP;
			}
		}
		return new String[] { targetType, idDatabase };	
	}
}
