package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.DocumentationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.ParentQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParentUtils extends RdfService{

	
	static final Logger logger = LoggerFactory.getLogger(ParentUtils.class);

	
	public String getDocumentationOwnersByIdSims(String idSims) throws RmesException {
		logger.info("Search Sims Owners' Stamps");
		String stamps = null;
		JSONObject target = repoGestion.getResponseAsObject(DocumentationQueries.getTargetByIdSims(idSims));
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
		JSONObject series = repoGestion.getResponseAsObject(OperationsOperationQueries.seriesQuery(idOperation));
		if (series != null && series.has(Constants.ID))		
			return RdfUtils.objectIRI(ObjectType.SERIES, series.getString(Constants.ID));
		return null;
	}
	

	public boolean checkIfSeriesHasSims(String uriSeries) throws RmesException {
		return repoGestion.getResponseAsBoolean(OperationSeriesQueries.checkIfSeriesHasSims(uriSeries));
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
		return repoGestion.getResponseAsBoolean(OperationSeriesQueries.checkIfSeriesHasOperation(uriParent));
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
			return repoGestion.getResponseAsObject(OperationIndicatorsQueries.getPublicationState(id)).getString("state");
		}
		catch (JSONException _) {
			return Constants.UNDEFINED;
		}
	}
	
	public String getFamOpSerValidationStatus(String id) throws RmesException {
		try {		
			return repoGestion.getResponseAsObject(OperationQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException _) {
			return Constants.UNDEFINED;
		}
	}
	
	
	public JSONArray getIndicatorCreators(String id) throws RmesException {
		return  repoGestion.getResponseAsJSONList(OperationIndicatorsQueries.getCreatorsById(id));
	}
	
	
	public JSONArray getSeriesCreators(String id) throws RmesException {
		return  repoGestion.getResponseAsJSONList(OperationSeriesQueries.getCreatorsById(id));
	}
	
	public JSONArray getSeriesCreators(IRI iri) throws RmesException {
		return repoGestion.getResponseAsJSONList(OperationSeriesQueries.getCreatorsBySeriesUri(RdfUtils.toString(iri)));
	}
	
	public String[] getDocumentationTargetTypeAndId(String idSims) throws RmesException {
		logger.info("Search Sims Target Type and id");

		JSONObject existingIdTarget =  repoGestion.getResponseAsObject(DocumentationQueries.getTargetByIdSims(idSims));
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
