package fr.insee.rmes.bauhaus_services.accesscontrol;

import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.external.services.rbac.AuthorizationChecker;
import fr.insee.rmes.model.rbac.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationCheckerWithResourceOwnershipByStamp implements AuthorizationChecker {

    private final ResourceOwnershipByStampVerifier resourceOwnershipByStampVerifier;

    @Autowired
    public AuthorizationCheckerWithResourceOwnershipByStamp(ResourceOwnershipByStampVerifier resourceOwnershipByStampVerifier) {
        this.resourceOwnershipByStampVerifier = resourceOwnershipByStampVerifier;
    }

    public boolean isSeriesManagerWithStamp(String seriesId, Stamp stamp) {
        return resourceOwnershipByStampVerifier.isSeriesManagerWithStamp(seriesId, stamp);
    }

    public boolean isDatasetManagerWithStamp(String datasetId, Stamp stamp) {
        return resourceOwnershipByStampVerifier.isDatasetManagerWithStamp(datasetId, stamp);
    }

    public boolean isDistributionManagerWithStamp(String datasetId, Stamp stamp) {
        return resourceOwnershipByStampVerifier.isDistributionManagerWithStamp(datasetId, stamp);
    }

    public boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp) {
        return resourceOwnershipByStampVerifier.isCodesListManagerWithStamp(codesListId, stamp);
    }

    public boolean isStructureManagerWithStamp(String structureId, Stamp stamp) {
       return  resourceOwnershipByStampVerifier.isStructureManagerWithStamp(structureId, stamp);
    }

    public boolean isComponentManagerWithStamp(String componentId, Stamp stamp) {
        return resourceOwnershipByStampVerifier.isComponentManagerWithStamp(componentId,stamp);
    }

    @Override
    public boolean userStampIsAuthorizedForResource(Module module, String id, Stamp stamp) {
        return switch (module){
            case CONCEPT -> false;
            case COLLECTION -> false;
            case FAMILY -> false;
            case SERIE -> isSeriesManagerWithStamp(id, stamp);
            case OPERATION -> false;
            case INDICATOR -> false;
            case SIMS -> false;
            case CLASSIFICATION -> false;
            case DATASET -> isDatasetManagerWithStamp(id, stamp);
            case DISTRIBUTION -> isDistributionManagerWithStamp(id, stamp);
            case COMPONENT -> isComponentManagerWithStamp(id, stamp);
            case STRUCTURE -> isStructureManagerWithStamp(id, stamp);
            case CODE_LIST -> isCodesListManagerWithStamp(id, stamp);
            case null -> false;
        };
    }


}

