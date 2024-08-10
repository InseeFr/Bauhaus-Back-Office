package fr.insee.rmes.bauhaus_services.accesscontrol;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetQueries;
import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.utils.IRIUtils;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static fr.insee.rmes.utils.StringUtils.urisAsString;
import static java.util.Objects.requireNonNull;

@Component
public record StampsRestrictionsVerifierImpl(IRIUtils iriUtils, RepositoryGestion repoGestion, StructureComponentUtils structureComponentUtils) implements StampsRestrictionsVerifier {

    private static final Logger logger = LoggerFactory.getLogger(StampsRestrictionsVerifierImpl.class);

    @Override
    public boolean isSeriesManagerWithStamp(String seriesId, Stamp stamp) {
        try {
            return isManagerForModule(requireNonNull(stamp).stamp(), iriUtils.findIRI(requireNonNull(seriesId)), OpSeriesQueries::getCreatorsBySeriesUri, Constants.CREATORS);
        } catch (RmesException e) {
            logger.error(errorMessage(stamp, seriesId), e);
            return false;
        }
    }

    @Override
    public boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp) {
        try {
            return this.isManagerForModule(requireNonNull(stamp).stamp(), iriUtils.findDatasetIRI(requireNonNull(datasetId)), DatasetQueries::getContributorsByDatasetUri, Constants.CONTRIBUTORS);
        } catch (RmesException e) {
            logger.error(errorMessage(stamp, datasetId), e);
            return false;
        }
    }

    @Override
    public boolean isDistributionManagerWithStamp(String datasetId, Stamp stamp){
        try {
            return isManagerForModule(requireNonNull(stamp).stamp(), iriUtils.findDistributionIRI(requireNonNull(datasetId)), DistributionQueries::getContributorsByDistributionUri, Constants.CONTRIBUTORS);
        } catch (RmesException e) {
            logger.error(errorMessage(stamp, datasetId), e);
            return false;
        }
    }

    @Override
    public boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp) {
        try {
            return isManagerForModule(requireNonNull(stamp).stamp(), this.structureComponentUtils.findCodesListIRI(requireNonNull(codesListId)), CodeListQueries::getContributorsByCodesListUri, Constants.CONTRIBUTORS);
        } catch (RmesException e) {
            logger.error(errorMessage(stamp, codesListId), e);
            return false;
        }
    }

    @Override
    public boolean isStructureManagerWithStamp(String structureId, Stamp stamp) {
        try {
            return isManagerForModule(requireNonNull(stamp).stamp(), iriUtils.findStructureIRI(requireNonNull(structureId)), StructureQueries::getContributorsByStructureUri, Constants.CONTRIBUTORS);
        } catch (RmesException e) {
            logger.error(errorMessage(stamp, structureId), e);
            return false;
        }
    }

    @Override
    public boolean isComponentManagerWithStamp(String componentId, Stamp stamp) {
        try {
            return isManagerForModule(requireNonNull(stamp).stamp(), this.structureComponentUtils.findComponentIRI(requireNonNull(componentId)), StructureQueries::getContributorsByComponentUri, Constants.CONTRIBUTORS);
        } catch (RmesException e) {
            logger.error(errorMessage(stamp, componentId), e);
            return false;
        }
    }

    private String errorMessage(Stamp stamp, String datasetId) {
        return  "Error while checking authorization for user with stamp "+stamp+" for resource "+datasetId;
    }

    @Override
    public boolean isManagerForModule(String stamp, IRI uri, StampsRestrictionsService.QueryGenerator queryGenerator, String stampKey) throws RmesException {
        logger.trace("Check management access for {} with stamp {}",uri, stampKey);
        return checkResponsabilityForModule(stamp, List.of(uri), queryGenerator, stampKey, Stream::anyMatch);
    }

    @Override
    public boolean checkResponsabilityForModule(String stamp, List<IRI> uris, StampsRestrictionsService.QueryGenerator queryGenerator, String stampKey, BiPredicate<Stream<Object>, Predicate<Object>> predicateMatcher) throws RmesException {
        JSONArray owners = repoGestion.getResponseAsArray(queryGenerator.generate(urisAsString(uris)));
        return StringUtils.hasLength(stamp) &&
                predicateMatcher.test(
                        owners.toList().stream()
                                .map(o -> findStamp(o, stampKey)),
                        stamp::equals // apply predicate `stamp::equals` to the stream of stamps returned at the previous line
                );
    }


    private Object findStamp(Object o, String stampKey) {
        if (o instanceof JSONObject jsonObject) {
            return jsonObject.get(stampKey);
        }
        if (o instanceof Map<?, ?> map) {
            return map.get(stampKey);
        }
        return null;
    }

}
