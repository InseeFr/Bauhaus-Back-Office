package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.datasets.DatasetQueries;
import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.stamps.StampsRestrictionServiceImpl;
import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class StampAuthorizationChecker extends StampsRestrictionServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(StampAuthorizationChecker.class);
    public static final String CHECKING_AUTHORIZATION_ERROR_MESSAGE = "Error while checking authorization for user with stamp {} to modify or delete {}";
    public static final String ERROR_AUTHORIZATION = "Error while checking authorization for user with stamp {} to modify {}";

    @Autowired
    public StampAuthorizationChecker(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider) {
        super(repoGestion, authorizeMethodDecider, userProvider);
    }

    public boolean isSeriesManagerWithStamp(String seriesId, Stamp stamp) {
        try {
            return isSeriesManagerWithStamp(findIRI(requireNonNull(seriesId)), requireNonNull(stamp).stamp());
        } catch (RmesException e) {
            logger.error(ERROR_AUTHORIZATION, stamp, seriesId);
            return false;
        }
    }

    public boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp) {
        try {
            return isDatasetManagerWithStamp(findDatasetIRI(requireNonNull(datasetId)), requireNonNull(stamp).stamp());
        } catch (RmesException e) {
            logger.error(ERROR_AUTHORIZATION, stamp, datasetId);
            return false;
        }
    }

    private boolean isDatasetManagerWithStamp(IRI iri, String stamp) throws RmesException {
        return isManagerForModule(stamp, iri, DatasetQueries::getContributorsByDatasetUri, Constants.CONTRIBUTORS);
    }

    public boolean isDistributionManagerWithStamp(String datasetId, Stamp stamp) {
        try {
            return isDistributionManagerWithStamp(findDistributionIRI(requireNonNull(datasetId)), requireNonNull(stamp).stamp());
        } catch (RmesException e) {
            logger.error(ERROR_AUTHORIZATION, stamp, datasetId);
            return false;
        }
    }

    private boolean isDistributionManagerWithStamp(IRI iri, String stamp) throws RmesException {
        return isManagerForModule(stamp, iri, DistributionQueries::getContributorsByDistributionUri, Constants.CONTRIBUTORS);
    }

    public boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp) {
        try {
            return isCodesListManagerWithStamp(findCodesListIRI(requireNonNull(codesListId)), requireNonNull(stamp).stamp());
        } catch (RmesException e) {
            logger.error(CHECKING_AUTHORIZATION_ERROR_MESSAGE, stamp, codesListId);
            return false;
        }
    }

    public boolean isStructureManagerWithStamp(String structureId, Stamp stamp) {
        try {
            return isStructureManagerWithStamp(findStructureIRI(requireNonNull(structureId)), requireNonNull(stamp).stamp());
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

    public boolean isComponentManagerWithStamp(String componentId, Stamp stamp) {
        try {
            return isComponentManagerWithStamp(findComponentIRI(requireNonNull(componentId)), requireNonNull(stamp).stamp());
        } catch (RmesException e) {
            logger.error(CHECKING_AUTHORIZATION_ERROR_MESSAGE, stamp, componentId);
            return false;
        }
    }

    private IRI findIRI(String seriesId) {
        return RdfUtils.objectIRI(ObjectType.SERIES, seriesId);
    }

    private IRI findCodesListIRI(String codesListId) throws RmesException {
        JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getCodeListIRIByNotation(codesListId));
        String uriString = codeList.getString("iri");
        return RdfUtils.createIRI(uriString);
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

