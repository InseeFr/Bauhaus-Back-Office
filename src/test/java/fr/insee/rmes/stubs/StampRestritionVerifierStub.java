package fr.insee.rmes.stubs;

import fr.insee.rmes.bauhaus_services.accesscontrol.ResourceOwnershipByStampVerifier;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StampRestritionVerifierStub implements ResourceOwnershipByStampVerifier {

    public static final String DATASET_STUB_ID = "datasetID";
    public static final String SERIES_STUB_ID = "seriesID";
    public static final String DISTRIBUTION_STUB_ID = "distributionID";
    public static final String CODES_LISTES_STUB_ID = "codesListesID";
    public static final String STRUCTURE_STUB_ID = "structureID";
    public static final String COMPONENT_STUB_ID = "componentID";



    @Override
    public boolean isSeriesManagerWithStamp(String seriesId, Stamp stamp) {
        return SERIES_STUB_ID.equals(seriesId);
    }

    @Override
    public boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp) {
        return DATASET_STUB_ID.equals(datasetId);
    }

    @Override
    public boolean isDistributionManagerWithStamp(String distributionId, Stamp stamp) {
        return DISTRIBUTION_STUB_ID.equals(distributionId);
    }

    @Override
    public boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp) {
        return CODES_LISTES_STUB_ID.equals(codesListId);
    }

    @Override
    public boolean isStructureManagerWithStamp(String structureId, Stamp stamp) {
        return STRUCTURE_STUB_ID.equals(structureId);
    }

    @Override
    public boolean isComponentManagerWithStamp(String componentId, Stamp stamp) {
        return COMPONENT_STUB_ID.equals(componentId);
    }

    @Override
    public boolean isManagerForModule(String stamp, IRI uri, StampsRestrictionsService.QueryGenerator queryGenerator, String stampKey) throws RmesException {
        return false;
    }

    @Override
    public boolean checkResponsabilityForModule(String stamp, List<IRI> uris, StampsRestrictionsService.QueryGenerator queryGenerator, String stampKey, BiPredicate<Stream<Object>, Predicate<Object>> predicateMatcher) throws RmesException {
        return false;
    }
}
