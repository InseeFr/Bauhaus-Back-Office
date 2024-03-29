package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.code_list.CodeListServiceImpl;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.stamps.StampsRestrictionServiceImpl;
import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Component
public class StampAuthorizationChecker extends StampsRestrictionServiceImpl {
	private static final Logger logger = LoggerFactory.getLogger(StampAuthorizationChecker.class);
	public static final String CHECKING_AUTHORIZATION_ERROR_MESSAGE = "Error while checking authorization for user with stamp {} to modify or delete {}";
	private final String baseInternalUri;

	@Autowired
	public StampAuthorizationChecker(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider,@Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseInternalURI}") String baseInternalUri) {
		super(repoGestion, authorizeMethodDecider, userProvider);
		this.baseInternalUri=baseInternalUri;
	}

	public boolean isSeriesManagerWithStamp(String seriesId, String stamp) {
		try {
			return isSeriesManagerWithStamp(findIRI(requireNonNull(seriesId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error("Error while checking authorization for user with stamp {} to modify {}", stamp, seriesId);
			return false;
		}
	}

	public boolean isDatasetManagerWithStamp(String datasetId, String stamp) {
		try {
			return isDatasetManagerWithStamp(findDatasetIRI(requireNonNull(datasetId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error("Error while checking authorization for user with stamp {} to modify {}", stamp, datasetId);
			return false;
		}
	}

	public boolean isDistributionManagerWithStamp(String datasetId, String stamp) {
		try {
			return isDistributionManagerWithStamp(findDistributionIRI(requireNonNull(datasetId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error("Error while checking authorization for user with stamp {} to modify {}", stamp, datasetId);
			return false;
		}
	}

	public boolean isCodesListManagerWithStamp(String codesListId, String stamp) {
		try {
			return isCodesListManagerWithStamp(findCodesListIRI(requireNonNull(codesListId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error(CHECKING_AUTHORIZATION_ERROR_MESSAGE, stamp, codesListId);
			return false;
		}
	}

	public boolean isStructureManagerWithStamp(String structureId, String stamp) {
		try {
			return isStructureManagerWithStamp(findStructureIRI(requireNonNull(structureId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error(CHECKING_AUTHORIZATION_ERROR_MESSAGE, stamp, structureId);
			return false;
		}
	}



	public boolean isCodesListManagerWithStamp(IRI iri, String stamp) throws RmesException {
		return isManagerForModule(stamp, iri, CodeListQueries::getContributorsByCodesListUri, Constants.CONTRIBUTORS);
	}
	public boolean isComponentManagerWithStamp(IRI iri, String stamp) throws RmesException {
		return isManagerForModule(stamp, iri, StructureQueries::getContributorsByComponentUri, Constants.CONTRIBUTORS);
	}
	public boolean isStructureManagerWithStamp(IRI iri, String stamp) throws RmesException {
		return isManagerForModule(stamp, iri, StructureQueries::getContributorsByStructureUri, Constants.CONTRIBUTORS);
	}

	public boolean isComponentManagerWithStamp(String componentId, String stamp) {
		try {
			return isComponentManagerWithStamp(findComponentIRI(requireNonNull(componentId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error(CHECKING_AUTHORIZATION_ERROR_MESSAGE, stamp, componentId);
			return false;
		}
	}

	private IRI findIRI(String seriesId) {
		return RdfUtils.objectIRI(ObjectType.SERIES, seriesId);
	}

	private IRI findCodesListIRI(String codesListId) throws RmesException {
		JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getCodeListIRIByNotation(codesListId, baseInternalUri));
		String uriString = codeList.getString("iri");
		IRI uriCodesList = RdfUtils.createIRI(uriString);
		return uriCodesList;
	}

	private IRI findStructureIRI(String structureId) {
		return RdfUtils.objectIRI(ObjectType.STRUCTURE, structureId);
	}

	private IRI findDatasetIRI(String datasetId) {
		return RdfUtils.objectIRI(ObjectType.DATASET, datasetId);
	}

	private IRI findDistributionIRI(String distributionId) {
		return RdfUtils.objectIRI(ObjectType.DISTRIBUTION, distributionId);
	}
}

	private IRI findComponentIRI(String componentId) throws RmesException {
		JSONObject type = repoGestion.getResponseAsObject(StructureQueries.getComponentType(componentId));
		String componentType = type.getString("type");
		if (componentType.equals(RdfUtils.toString(QB.ATTRIBUTE_PROPERTY))) {
			return RdfUtils.structureComponentAttributeIRI(componentId);
		} else if (componentType.equals(RdfUtils.toString(QB.DIMENSION_PROPERTY))) {
			return RdfUtils.structureComponentDimensionIRI(componentId);
		} else {
			return RdfUtils.structureComponentMeasureIRI(componentId);
		}
	}

}

