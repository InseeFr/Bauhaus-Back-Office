package fr.insee.rmes.bauhaus_services.accesscontrol;

import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ResourceOwnershipByStampVerifier {
    boolean isSeriesManagerWithStamp(String seriesId, Stamp stamp);

    boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp);

    boolean isDistributionManagerWithStamp(String distributionId, Stamp stamp);

    boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp);

    boolean isStructureManagerWithStamp(String structureId, Stamp stamp);

    boolean isComponentManagerWithStamp(String componentId, Stamp stamp);

    boolean isManagerForModule(String stamp, IRI uri, StampsRestrictionsService.QueryGenerator queryGenerator, String stampKey) throws RmesException;

    boolean checkResponsabilityForModule(String stamp, List<IRI> uris, StampsRestrictionsService.QueryGenerator queryGenerator, String stampKey, BiPredicate<Stream<Object>, Predicate<Object>> predicateMatcher) throws RmesException;
}
