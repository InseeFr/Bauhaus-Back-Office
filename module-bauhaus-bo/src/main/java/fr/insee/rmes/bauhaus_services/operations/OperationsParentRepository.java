package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.DocumentationQueries;
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
import org.springframework.stereotype.Repository;

@Repository
public class OperationsParentRepository extends RdfService{

	static final Logger logger = LoggerFactory.getLogger(OperationsParentRepository.class);

	private final OperationIndicatorsQueries operationIndicatorsQueries;

	private final OperationsOperationQueries operationsOperationQueries;

	private final DocumentationQueries documentationQueries;

	private final ParentQueries parentQueries;

	private final OperationQueries operationQueries;

	private final OperationSeriesQueries operationSeriesQueries;

	private final OrganisationLookup organisationLookup;

	public OperationsParentRepository(RepositoryGestion repoGestion, IdGenerator idGenerator,
					   RepositoryPublication repositoryPublication,
					   PublicationUtils publicationUtils,
					   OperationIndicatorsQueries operationIndicatorsQueries,
					   OperationsOperationQueries operationsOperationQueries,
					   DocumentationQueries documentationQueries,
					   ParentQueries parentQueries,
					   OperationQueries operationQueries,
					   OperationSeriesQueries operationSeriesQueries,
					   OrganisationLookup organisationLookup) {
		super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
		this.operationIndicatorsQueries = operationIndicatorsQueries;
		this.operationsOperationQueries = operationsOperationQueries;
		this.documentationQueries = documentationQueries;
		this.parentQueries = parentQueries;
		this.operationQueries = operationQueries;
		this.operationSeriesQueries = operationSeriesQueries;
		this.organisationLookup = organisationLookup;
	}


	public String getDocumentationOwnersByIdSims(String idSims) throws RmesException {
		logger.info("Search Sims Owners' Stamps");
		String stamps = null;
		JSONObject target = repoGestion.getResponseAsObject(documentationQueries.getTargetByIdSims(idSims));
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
		JSONObject series = repoGestion.getResponseAsObject(operationsOperationQueries.seriesQuery(idOperation));
		if (series != null && series.has(Constants.ID))
			return RdfUtils.objectIRI(ObjectType.SERIES, series.getString(Constants.ID));
		return null;
	}


	public boolean checkIfParentExists(String uriParent) throws RmesException {
		return repoGestion.getResponseAsBoolean(parentQueries.checkIfExists(uriParent));
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
			return repoGestion.getResponseAsObject(operationIndicatorsQueries.getPublicationState(id)).getString("state");
		}
		catch (JSONException _) {
			return Constants.UNDEFINED;
		}
	}

	public String getFamOpSerValidationStatus(String id) throws RmesException {
		try {
			return repoGestion.getResponseAsObject(operationQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException _) {
			return Constants.UNDEFINED;
		}
	}


	public JSONArray getIndicatorCreators(String id) throws RmesException {
		JSONArray raw = repoGestion.getResponseAsJSONList(operationIndicatorsQueries.getCreatorsById(id));
		return organisationLookup.canonicalize(raw);
	}


	public JSONArray getSeriesCreators(String id) throws RmesException {
		JSONArray raw = repoGestion.getResponseAsJSONList(operationSeriesQueries.getCreatorsById(id));
		return organisationLookup.canonicalize(raw);
	}

	public JSONArray getSeriesCreators(IRI iri) throws RmesException {
		JSONArray raw = repoGestion.getResponseAsJSONList(operationSeriesQueries.getCreatorsBySeriesUri(RdfUtils.toString(iri)));
		return organisationLookup.canonicalize(raw);
	}

	public String[] getDocumentationTargetTypeAndId(String idSims) throws RmesException {
		logger.info("Search Sims Target Type and id");

		JSONObject existingIdTarget = repoGestion.getResponseAsObject(documentationQueries.getTargetByIdSims(idSims));
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